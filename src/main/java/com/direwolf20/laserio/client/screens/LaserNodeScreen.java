package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenCard;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.Vec2i;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class LaserNodeScreen extends AbstractContainerScreen<LaserNodeContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/laser_node.png");
    protected final LaserNodeContainer container;
    private final TranslatableComponent[] sides = {
            new TranslatableComponent("screen.laserio.down"),
            new TranslatableComponent("screen.laserio.up"),
            new TranslatableComponent("screen.laserio.north"),
            new TranslatableComponent("screen.laserio.south"),
            new TranslatableComponent("screen.laserio.west"),
            new TranslatableComponent("screen.laserio.east"),
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
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        fill(matrixStack, tabs[container.side].x + 2, tabs[container.side].y + 2, tabs[container.side].x + 22, tabs[container.side].y + 14, 0xFFC6C6C6);
        fill(matrixStack, tabs[container.side].x, tabs[container.side].y + 11, tabs[container.side].x + 2, tabs[container.side].y + 12, 0xFFFFFFFF);
        fill(matrixStack, tabs[container.side].x + 22, tabs[container.side].y + 11, tabs[container.side].x + 24, tabs[container.side].y + 12, 0xFFFFFFFF);
        matrixStack.pushPose();
        font.draw(matrixStack, sides[container.side].getString(), imageWidth / 2 - font.width(sides[container.side].getString()) / 2, 20, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "U", 15, 7, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "D", 43, 7, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "N", 71, 7, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "S", 99, 7, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "W", 128, 7, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, "E", 155, 7, Color.DARK_GRAY.getRGB());
        matrixStack.popPose();
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
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
