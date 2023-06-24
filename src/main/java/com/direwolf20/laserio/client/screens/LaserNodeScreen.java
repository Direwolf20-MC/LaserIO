package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardHolderContainer;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenCard;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.Vec2i;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;

public class LaserNodeScreen extends AbstractContainerScreen<LaserNodeContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/laser_node.png");
    protected final LaserNodeContainer container;
    private boolean showCardHolderUI;
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
        super(container, inv, name);
        this.container = container;
        this.imageHeight = 181;
        showCardHolderUI = container.cardHolder.isEmpty();
        //this.imageWidth = 202;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        if (showCardHolderUI)
            return mouseX < (double) guiLeftIn - 50 || mouseY < (double) guiTopIn || mouseX >= (double) (guiLeftIn + this.imageWidth) || mouseY >= (double) (guiTopIn + this.imageHeight);
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        validateHolder();
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.fill(tabs[container.side].x + 2, tabs[container.side].y + 2, tabs[container.side].x + 22, tabs[container.side].y + 14, 0xFFC6C6C6);
        guiGraphics.fill(tabs[container.side].x, tabs[container.side].y + 11, tabs[container.side].x + 2, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.fill(tabs[container.side].x + 22, tabs[container.side].y + 11, tabs[container.side].x + 24, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.drawString(font, sides[container.side].getString(), imageWidth / 2 - font.width(sides[container.side].getString()) / 2, 20, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "U", 15, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "D", 43, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "N", 71, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "S", 99, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "W", 128, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "E", 155, 7, Color.DARK_GRAY.getRGB(), false);
        for (Direction direction : Direction.values()) {
            ItemStack itemStack = getAdjacentBlock(direction);
            if (!itemStack.isEmpty()) {
                guiGraphics.renderItem(itemStack, tabs[direction.ordinal()].x + 4, tabs[direction.ordinal()].y - 14, 0);
                if (MiscTools.inBounds(getGuiLeft() + tabs[direction.ordinal()].x+4, getGuiTop() + tabs[direction.ordinal()].y-14, 16, 16, mouseX, mouseY)) {
                    guiGraphics.renderTooltip(font, itemStack, mouseX-getGuiLeft(), mouseY-getGuiTop());
                }
            }
        }
    }

    protected ItemStack getAdjacentBlock(Direction direction) {
        BlockState blockState = container.playerEntity.level().getBlockState(this.container.tile.getBlockPos().relative(direction));
        ItemStack itemStack = blockState.getBlock().asItem().getDefaultInstance();
        return itemStack;
    }

                                         @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        if (showCardHolderUI) {
            ResourceLocation CardHolderGUI = new ResourceLocation(LaserIO.MODID, "textures/gui/cardholder_node.png");
            RenderSystem.setShaderTexture(0, CardHolderGUI);
            guiGraphics.blit(CardHolderGUI, getGuiLeft() - 50, getGuiTop() + 24, 0, 0, this.imageWidth, this.imageHeight);
        }
    }

    public boolean validateHolder() {
        Inventory playerInventory = container.playerEntity.getInventory();
        for (int i = 0; i < playerInventory.items.size(); i++) {
            ItemStack itemStack = playerInventory.items.get(i);
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
    public boolean mouseClicked(double x, double y, int btn) {
        if (MiscTools.inBounds(getGuiLeft() + tabs[1].x, getGuiTop() + tabs[1].y, 24, 12, x, y) && container.side != 1) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 1));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[0].x, getGuiTop() + tabs[0].y, 24, 12, x, y) && container.side != 0) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 0));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[2].x, getGuiTop() + tabs[2].y, 24, 12, x, y) && container.side != 2) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 2));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[3].x, getGuiTop() + tabs[3].y, 24, 12, x, y) && container.side != 3) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 3));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[4].x, getGuiTop() + tabs[4].y, 24, 12, x, y) && container.side != 4) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[5].x, getGuiTop() + tabs[5].y, 24, 12, x, y) && container.side != 5) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 5));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        if (hoveredSlot == null || hoveredSlot.getItem().isEmpty() || !(hoveredSlot.getItem().getItem() instanceof BaseCard))
            return super.mouseClicked(x, y, btn);

        if (btn == 1 && hoveredSlot instanceof LaserNodeSlot) { //Right click
            int slot = hoveredSlot.getSlotIndex();
            PacketHandler.sendToServer(new PacketOpenCard(slot, container.tile.getBlockPos(), hasShiftDown()));
            return true;
        }
        return super.mouseClicked(x, y, btn);
    }
}
