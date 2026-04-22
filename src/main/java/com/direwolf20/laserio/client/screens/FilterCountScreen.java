package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.data.GhostSlotPayload;
import com.direwolf20.laserio.common.network.data.UpdateFilterPayload;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

public class FilterCountScreen extends AbstractContainerScreen<FilterCountContainer> {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/filtercount.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 256;

    protected final FilterCountContainer container;
    private ItemStack filter;
    private boolean isAllowList;
    private boolean isCompareNBT;

    public FilterCountScreen(FilterCountContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.filter = container.filterItem;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // TODO(port, stage-12): re-enable LaserGuiGraphics slot-rendering substitution
        // when LaserGuiGraphics is ported to the new GuiGraphicsExtractor + ItemRenderer pipeline.
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (MiscTools.inBounds(leftPos + 5, topPos + 25, 16, 16, mouseX, mouseY)) {
            if (isCompareNBT)
                guiGraphics.setTooltipForNextFrame(font, Component.translatable("screen.laserio.nbttrue"), mouseX, mouseY);
            else
                guiGraphics.setTooltipForNextFrame(font, Component.translatable("screen.laserio.nbtfalse"), mouseX, mouseY);
        }
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        this.isAllowList = FilterCount.getAllowList(filter);
        this.isCompareNBT = FilterCount.getCompareNBT(filter);

        Identifier[] nbtTextures = new Identifier[2];
        nbtTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/matchnbtfalse.png");
        nbtTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/matchnbttrue.png");

        leftWidgets.add(new ToggleButton(leftPos + 5, topPos + 25, 16, 16, nbtTextures, isCompareNBT ? 1 : 0, (button) -> {
            isCompareNBT = !isCompareNBT;
            ((ToggleButton) button).setTexturePosition(isCompareNBT ? 1 : 0);
        }));

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
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ClientPacketDistributor.sendToServer(new UpdateFilterPayload(isAllowList, isCompareNBT));
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (event.isEscape() || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int btn = event.button();
        if (hoveredSlot == null || !(hoveredSlot instanceof FilterBasicSlot))
            return super.mouseClicked(event, doubleClick);

        // By splitting the stack we can get air easily :) perfect removal basically
        ItemStack stack = this.menu.getCarried();// getMinecraft().player.inventoryMenu.getCarried();
        if (!stack.isEmpty()) {
            stack = stack.copy();
            hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
            if (ItemStack.isSameItemSameComponents(stack, container.filterItem)) return true;
            ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, stack, stack.getCount(), -1));
            container.handler.set(hoveredSlot.index, ItemResource.of(stack), stack.getCount()); //We do this for continuity between client/server -- not needed in cardItemScreen
        } else {
            ItemStack slotStack = hoveredSlot.getItem();
            if (slotStack.isEmpty()) return true;
            if (btn == 2) {
                slotStack.setCount(0);
                ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, slotStack.getCount(), -1));
                return true;
            }
            int amt = (btn == 0) ? 1 : -1;
            if (Minecraft.getInstance().hasShiftDown()) amt *= 10;
            if (Minecraft.getInstance().hasControlDown()) amt *= 64;
            if (amt + slotStack.getCount() > 4096) amt = 4096 - slotStack.getCount();
            slotStack.grow(amt);

            ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, slotStack.getCount(), -1));
            container.handler.set(hoveredSlot.index, ItemResource.of(slotStack), slotStack.getCount()); //We do this for continuity between client/server -- not needed in cardItemScreen
        }


        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        if (hoveredSlot == null || !(hoveredSlot instanceof FilterBasicSlot))
            return super.mouseScrolled(mouseX, mouseY, delta, deltaY);

        ItemStack slotStack = hoveredSlot.getItem();
        if (slotStack.isEmpty()) return true;
        int amt = (int) delta;
        if (Minecraft.getInstance().hasShiftDown()) amt *= 10;
        if (Minecraft.getInstance().hasControlDown()) amt *= 64;
        if (amt + slotStack.getCount() > 4096) amt = 4096 - slotStack.getCount();
        if (slotStack.getCount() + amt <= 0)
            amt = (slotStack.getCount() * -1) + 1;
        slotStack.grow(amt);

        ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, slotStack.getCount(), -1));
        return true;
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }

}
