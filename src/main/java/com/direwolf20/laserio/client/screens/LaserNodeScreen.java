package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.IconButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardHolderContainer;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.CardCloner;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.network.data.CopyPasteCardPayload;
import com.direwolf20.laserio.common.network.data.OpenCardPayload;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import com.direwolf20.laserio.common.network.data.ToggleParticlesPayload;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LaserNodeScreen extends AbstractContainerScreen<LaserNodeContainer> {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/laser_node.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 256;
    protected final LaserNodeContainer container;
    private boolean showCardHolderUI;
    private boolean currentParticles;
    Button settingsButton;
    Button particlesButton;
    private final MutableComponent[] sides = {
            Component.translatable("screen.laserio.down"),
            Component.translatable("screen.laserio.up"),
            Component.translatable("screen.laserio.north"),
            Component.translatable("screen.laserio.south"),
            Component.translatable("screen.laserio.west"),
            Component.translatable("screen.laserio.east"),
    };

    private final Vec2i[] tabs = {
            new Vec2i(34, 4), //Down
            new Vec2i(6, 4), //Up
            new Vec2i(62, 4),
            new Vec2i(90, 4),
            new Vec2i(118, 4),
            new Vec2i(146, 4)
    };

    public LaserNodeScreen(LaserNodeContainer container, Inventory inv, Component name) {
        super(container, inv, name, 176, 181);
        this.container = container;
        showCardHolderUI = container.cardHolder.isEmpty();
        this.currentParticles = container.tile.getShowParticles();
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();
        Identifier settings = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/settings.png");
        settingsButton = new IconButton(leftPos + 155, topPos + 25, 16, 16, settings, (button) -> {
            Minecraft.getInstance().setScreen(new LaserNodeSettingsScreen(container, Component.translatable("screen.laserio.settings")));
        });
        leftWidgets.add(settingsButton);

        Identifier[] regulateTextures = new Identifier[2];
        regulateTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/regulatefalse.png");
        regulateTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/regulatetrue.png");
        particlesButton = new ToggleButton(leftPos + 155, topPos + 45, 16, 16, regulateTextures, currentParticles ? 1 : 0, (button) -> {
            currentParticles = !currentParticles;
            ((ToggleButton) button).setTexturePosition(currentParticles ? 1 : 0);
            ClientPacketDistributor.sendToServer(new ToggleParticlesPayload(currentParticles));
        });
        leftWidgets.add(particlesButton);

        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }

    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn) {
        if (showCardHolderUI)
            return mouseX < (double) guiLeftIn - 100 || mouseY < (double) guiTopIn || mouseX >= (double) (guiLeftIn + this.imageWidth) || mouseY >= (double) (guiTopIn + this.imageHeight);
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        validateHolder();
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (MiscTools.inBounds(particlesButton.getX(), particlesButton.getY(), particlesButton.getWidth(), particlesButton.getHeight(), mouseX, mouseY)) {
            MutableComponent translatableComponents[] = new MutableComponent[4];
            translatableComponents[0] = Component.translatable("screen.laserio.showparticles");
            translatableComponents[1] = Component.translatable("screen.laserio.hideparticles");
            guiGraphics.setTooltipForNextFrame(font, currentParticles ? translatableComponents[0] : translatableComponents[1], mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.fill(tabs[container.side].x + 2, tabs[container.side].y + 2, tabs[container.side].x + 22, tabs[container.side].y + 14, 0xFFC6C6C6);
        guiGraphics.fill(tabs[container.side].x, tabs[container.side].y + 11, tabs[container.side].x + 2, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.fill(tabs[container.side].x + 22, tabs[container.side].y + 11, tabs[container.side].x + 24, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.text(font, sides[container.side].getString(), imageWidth / 2 - font.width(sides[container.side].getString()) / 2, 20, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "U", 15, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "D", 43, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "N", 71, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "S", 99, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "W", 128, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "E", 155, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        for (Direction direction : Direction.values()) {
            ItemStack itemStack = getAdjacentBlock(direction);
            if (!itemStack.isEmpty()) {
                guiGraphics.item(itemStack, tabs[direction.ordinal()].x + 4, tabs[direction.ordinal()].y - 14, 0);
                if (MiscTools.inBounds(leftPos + tabs[direction.ordinal()].x + 4, topPos + tabs[direction.ordinal()].y - 14, 16, 16, mouseX, mouseY)) {
                    guiGraphics.setTooltipForNextFrame(font, itemStack, mouseX - leftPos, mouseY - topPos);
                }
            }
        }
    }

    protected ItemStack getAdjacentBlock(Direction direction) {
        BlockPos blockPos = this.container.tile.getBlockPos().relative(direction);
        Level level = this.container.playerEntity.level();
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack itemStack = blockState.getBlock().asItem().getDefaultInstance();
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                itemStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntity.getType(), blockEntity.saveCustomOnly(level.registryAccess())));
            }
        }
        return itemStack;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
        if (showCardHolderUI) {
            Identifier CardHolderGUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/cardholder_node.png");
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CardHolderGUI, leftPos - 100, topPos + 24, 0, 0, this.imageWidth, this.imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
        }
    }

    public boolean validateHolder() {
        Inventory playerInventory = container.playerEntity.getInventory();
        var items = playerInventory.getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            if (itemStack.getItem() instanceof CardHolder) {
                if (CardHolder.getUUID(itemStack).equals(container.cardHolderUUID)) {
                    showCardHolderUI = true;
                    toggleHolderSlots();
                    return true;
                }
            }
        }
        showCardHolderUI = false;
        toggleHolderSlots();
        return false;
    }

    public void toggleHolderSlots() {
        for (int i = 10; i < 10 + CardHolderContainer.SLOTS; i++) {
            if (i >= container.slots.size()) continue;
            Slot slot = container.getSlot(i);
            if (!(slot instanceof CardHolderSlot)) continue;
            ((CardHolderSlot) slot).setEnabled(showCardHolderUI);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        int btn = event.button();
        if (hoveredSlot != null && container.getCarried().getItem() instanceof CardCloner) {
            if (hoveredSlot instanceof LaserNodeSlot && !hoveredSlot.getItem().isEmpty())
                if (btn == 0) { //Left click
                    ClientPacketDistributor.sendToServer(new CopyPasteCardPayload(hoveredSlot.getSlotIndex(), true));
                }
            if (btn == 1) { //Right click
                ClientPacketDistributor.sendToServer(new CopyPasteCardPayload(hoveredSlot.getSlotIndex(), false));
            }
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[1].x, topPos + tabs[1].y, 24, 12, x, y) && container.side != 1) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 1));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[0].x, topPos + tabs[0].y, 24, 12, x, y) && container.side != 0) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 0));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[2].x, topPos + tabs[2].y, 24, 12, x, y) && container.side != 2) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 2));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[3].x, topPos + tabs[3].y, 24, 12, x, y) && container.side != 3) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 3));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[4].x, topPos + tabs[4].y, 24, 12, x, y) && container.side != 4) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[5].x, topPos + tabs[5].y, 24, 12, x, y) && container.side != 5) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 5));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        if (hoveredSlot == null || hoveredSlot.getItem().isEmpty() || !(hoveredSlot.getItem().getItem() instanceof BaseCard))
            return super.mouseClicked(event, doubleClick);

        if (btn == 1 && hoveredSlot instanceof LaserNodeSlot) { //Right click
            int slot = hoveredSlot.getSlotIndex();
            ClientPacketDistributor.sendToServer(new OpenCardPayload(slot, container.tile.getBlockPos(), Minecraft.getInstance().hasShiftDown()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
