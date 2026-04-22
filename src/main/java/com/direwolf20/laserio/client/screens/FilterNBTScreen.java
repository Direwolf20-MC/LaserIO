package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.IconButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.FilterNBTContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.network.data.GhostSlotPayload;
import com.direwolf20.laserio.common.network.data.UpdateFilterTagPayload;
import com.direwolf20.laserio.util.MagicHelpers;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.*;

public class FilterNBTScreen extends AbstractContainerScreen<FilterNBTContainer> {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/filtertag.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 256;

    protected final FilterNBTContainer container;
    private ItemStack filter;
    private boolean isAllowList;
    private EditBox tagField;
    private int page = 0;
    private int maxPages = 0;
    private int overSlot = -1;
    private int selectedSlot = -1;
    List<String> displayTags;
    List<String> tags = new ArrayList<>();
    List<String> stackInSlotTags = new ArrayList<>();
    int cycleRenders = 0;


    public FilterNBTScreen(FilterNBTContainer container, Inventory inv, Component name) {
        super(container, inv, name, 200, 254);
        this.container = container;
        this.filter = container.filterItem;
        this.tags = FilterTag.getTags(filter);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (MiscTools.inBounds(leftPos + 5, topPos + 10, 16, 16, mouseX, mouseY)) {
            if (isAllowList)
                guiGraphics.setTooltipForNextFrame(font, Component.translatable("screen.laserio.allowlist"), mouseX, mouseY);
            else
                guiGraphics.setTooltipForNextFrame(font, Component.translatable("screen.laserio.denylist"), mouseX, mouseY);
        }
        cycleRenders++;
        int availableItemsstartX = leftPos + 7;
        int availableItemstartY = topPos + 47;
        int color = 0x885B5B5B;
        guiGraphics.fillGradient(availableItemsstartX - 2, availableItemstartY - 4, availableItemsstartX + 162, availableItemstartY + 110, color, color);

        this.displayTags = new ArrayList<>();

        populateStackInSlotTags();

        int tagsPerPage = 11;

        stackInSlotTags.sort(Comparator.naturalOrder());
        tags.sort(Comparator.naturalOrder());

        List<String> tempTags = new ArrayList<>();
        tempTags.addAll(0, tags);
        tempTags.addAll(0, stackInSlotTags);

        maxPages = Math.max((int) Math.ceil((double) tempTags.size() / tagsPerPage) - 1, 0);
        if (page > maxPages) page = maxPages;
        String pagesLabel = MagicHelpers.withSuffix(page + 1) + " / " + MagicHelpers.withSuffix(maxPages + 1);
        guiGraphics.text(font, pagesLabel, (availableItemsstartX - 2) / 2 + (availableItemsstartX + 162) / 2 - font.width(pagesLabel) / 2, topPos + 160, 0xFF404040, false);

        int itemStackMin = (page * tagsPerPage);
        int itemStackMax = Math.min((page * tagsPerPage) + tagsPerPage, tempTags.size());

        displayTags = tempTags.subList(itemStackMin, itemStackMax);

        int tagStartY = availableItemstartY;

        int slot = 0;
        overSlot = -1;

        for (String tag : displayTags) {
            int fontColor = stackInSlotTags.contains(tag) ? 0xFF0000FF : 0xFF404040;
            renderScrollingString(guiGraphics, font, Component.literal(tag), availableItemsstartX, tagStartY, availableItemsstartX + 152, fontColor);

            if (MiscTools.inBounds(availableItemsstartX, tagStartY - 2, 160, 8, mouseX, mouseY)) {
                overSlot = slot;
                color = -2130706433;// : 0xFF5B5B5B;
                guiGraphics.fillGradient(availableItemsstartX - 1, tagStartY - 2, availableItemsstartX + 160, tagStartY + 8, color, color);
                String tagValue = getTagValueFor(displayTags.get(overSlot));
                if (!tagValue.isEmpty()) {
                    if (tagValue.length() > 60) tagValue = tagValue.substring(0, 60) + "...";
                    guiGraphics.setTooltipForNextFrame(font, Component.literal(tagValue), mouseX, mouseY);
                }
            }

            if (slot == selectedSlot) {
                color = 0xFFFF0000;

                int x1 = availableItemsstartX + 160;
                int y1 = tagStartY + 10;
                guiGraphics.outline(availableItemsstartX - 2, tagStartY - 2, x1 - (availableItemsstartX - 2), y1 - 2 - (tagStartY - 2), color);
            }

            tagStartY += 10;
            slot++;
        }
    }

    protected static void renderScrollingString(GuiGraphicsExtractor graphics, Font fontRenderer, Component text, int xStart, int yStart, int xEnd, int textColor) {
        int textWidth = fontRenderer.width(text);
        int yEnd = yStart + fontRenderer.lineHeight;
        int maxRenderWidth = xEnd - xStart;

        if (textWidth > maxRenderWidth) {
            int textOverflow = textWidth - maxRenderWidth;
            double currentTime = (double) Util.getMillis() / 1000.0D;
            double scrollDuration = Math.max((double) textOverflow * 0.5D, 3.0D);
            double oscillation = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * currentTime / scrollDuration)) / 2.0D + 0.5D;
            double scrollOffset = Mth.lerp(oscillation, 0.0D, (double) textOverflow);

            graphics.enableScissor(xStart, yStart, xEnd, yEnd);
            graphics.text(fontRenderer, text, xStart - (int) scrollOffset, yStart, textColor, false);
            graphics.disableScissor();
        } else {
            graphics.text(fontRenderer, text, xStart, yStart, textColor, false);
        }
    }

    protected void populateStackInSlotTags() {
        stackInSlotTags = new ArrayList<>();
        ItemStack stackInSlot = container.handler.getResource(0).toStack(container.handler.getAmountAsInt(0));
        if (!stackInSlot.isEmpty()) {
            stackInSlot.getComponentsPatch().entrySet().forEach(t -> {
                if (!stackInSlotTags.contains(t.getKey().toString()) && !tags.contains(t.getKey().toString()))
                    stackInSlotTags.add(t.getKey().toString());
            });
        }
    }

    protected String getTagValueFor(String name) {
        ItemStack stackInSlot = container.handler.getResource(0).toStack(container.handler.getAmountAsInt(0));
        if (!stackInSlot.isEmpty()) {
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : stackInSlot.getComponentsPatch().entrySet()) {
                if (entry.getKey().toString().equals(name)) {
                    // Check if the value is present and return its string representation
                    Optional<?> value = entry.getValue();
                    if (value.isPresent()) {
                        return value.get().toString();
                    }
                }
            }
        }
        return "";
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        this.isAllowList = FilterTag.getAllowList(filter);

        Identifier[] allowListTextures = new Identifier[2];
        allowListTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/allowlistfalse.png");
        allowListTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/allowlisttrue.png");

        leftWidgets.add(new ToggleButton(leftPos + 5, topPos + 5, 16, 16, allowListTextures, isAllowList ? 1 : 0, (button) -> {
            isAllowList = !isAllowList;
            ((ToggleButton) button).setTexturePosition(isAllowList ? 1 : 0);
        }));

        Identifier add = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/add.png");
        Button addButton = new IconButton(leftPos + 155, topPos + 5, 16, 16, add, (button) -> {
            if (!tagField.getValue().isEmpty()) {
                String tag = tagField.getValue().toLowerCase(Locale.ROOT);
                tag = tag.replaceAll("[^a-z0-9/._-]", "");
                if (!tags.contains(tag))
                    tags.add(tag);
                tagField.setValue("");
            } else {
                ItemStack stack = container.handler.getResource(0).toStack(container.handler.getAmountAsInt(0));
                if (!stack.isEmpty()) {
                    if (Minecraft.getInstance().hasShiftDown()) {
                        stack.getItem().builtInRegistryHolder().tags().forEach(t -> {
                            String tag = t.location().toString().toLowerCase(Locale.ROOT);
                            if (!tags.contains(tag))
                                tags.add(tag);
                        });
                        container.handler.set(0, ItemResource.EMPTY, 0);
                    } else {
                        if (selectedSlot != -1) {
                            String tag = displayTags.get(selectedSlot);
                            if (!tags.contains(tag)) {
                                tags.add(tag);
                                selectedSlot = -1;
                            }
                        }
                    }
                }
            }
        });
        leftWidgets.add(addButton);

        Identifier remove = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/remove.png");
        Button removeButton = new IconButton(leftPos + 135, topPos + 5, 16, 16, remove, (button) -> {
            if (selectedSlot != -1 && !tags.isEmpty()) {
                tags.remove(displayTags.get(selectedSlot));
                selectedSlot = -1;
            }
        });
        leftWidgets.add(removeButton);

        Identifier clear = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/clear.png");
        Button clearButton = new IconButton(leftPos + 115, topPos + 5, 16, 16, clear, (button) -> {
            tags.clear();
        });
        leftWidgets.add(clearButton);

        Identifier pageup = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/pageup.png");
        Button pageUp = new IconButton(leftPos + 100, topPos + 157, 12, 12, pageup, (button) -> {
            if (page < maxPages) page++;
        });
        leftWidgets.add(pageUp);

        Identifier pagedown = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/pagedown.png");
        Button pageDown = new IconButton(leftPos + 58, topPos + 157, 12, 12, pagedown, (button) -> {
            if (page > 0) page--;
        });
        leftWidgets.add(pageDown);

        tagField = new EditBox(font, leftPos + 7, topPos + 25, 160, 15, Component.empty());
        leftWidgets.add(tagField);


        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Intentionally empty — original screen suppressed the default title labels.
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ClientPacketDistributor.sendToServer(new UpdateFilterTagPayload(isAllowList, tags));
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (event.isEscape()) {
            if (tagField.isFocused()) {
                tagField.setFocused(false);
                return true;
            } else {
                onClose();
                return true;
            }
        }
        if (minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            if (tagField.isFocused()) {
                return true;
            } else {
                onClose();
                return true;
            }
        }

        if (tagField.isFocused() && (event.key() == 257 || event.key() == 335)) { //enter key
            if (!tagField.getValue().isEmpty()) {
                String tag = tagField.getValue().toLowerCase(Locale.ROOT);
                tag = tag.replaceAll("[^a-z0-9/._-]", "");
                if (!tags.contains(tag))
                    tags.add(tag);
                tagField.setValue("");
                tagField.setValue("");
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        int btn = event.button();
        if (MiscTools.inBounds(tagField.getX(), tagField.getY(), tagField.getWidth(), tagField.getHeight(), x, y) && btn == 1)
            tagField.setValue("");

        if (overSlot >= 0) {
            selectedSlot = overSlot;
            if (Minecraft.getInstance().hasShiftDown()) {
                if (selectedSlot != -1) {
                    if (selectedSlot >= stackInSlotTags.size()) {
                        tags.remove(displayTags.get(selectedSlot));
                        selectedSlot = -1;
                        return true;
                    }
                    if (selectedSlot < stackInSlotTags.size()) {
                        String tag = displayTags.get(selectedSlot);
                        if (!tags.contains(tag)) {
                            tags.add(tag);
                            selectedSlot = -1;
                            return true;
                        }
                    }
                }
            }
            //tagField.setText(displayTags.get(selectedSlot));
            return true;
        }

        if (hoveredSlot instanceof FilterBasicSlot) {


            // By splitting the stack we can get air easily :) perfect removal basically
            ItemStack stack = this.menu.getCarried();// getMinecraft().player.inventoryMenu.getCarried();
            stack = stack.copy().split(hoveredSlot.getMaxStackSize()); // Limit to slot limit
            hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
            ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, stack, stack.getCount(), -1));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        if (hoveredSlot == null) {
            if (delta == -1.0) {
                if (page < maxPages) page++;
            } else {
                if (page > 0) page--;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, delta, deltaY);
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }
}
