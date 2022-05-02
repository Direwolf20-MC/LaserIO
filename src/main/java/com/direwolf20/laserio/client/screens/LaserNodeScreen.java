package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenCard;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class LaserNodeScreen extends AbstractContainerScreen<LaserNodeContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/laser_node.png");
    protected final LaserNodeContainer container;

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
        matrixStack.pushPose();
        matrixStack.scale(0.7f, 0.7f, 0.7f);
        font.draw(matrixStack, new TranslatableComponent("Top").getString(), 16, 10, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, new TranslatableComponent("Bottom").getString(), 50, 10, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, new TranslatableComponent("North").getString(), 93, 10, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, new TranslatableComponent("South").getString(), 132, 10, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, new TranslatableComponent("West").getString(), 175, 10, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, new TranslatableComponent("East").getString(), 214, 10, Color.DARK_GRAY.getRGB());
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
        if (MiscTools.inBounds(getGuiLeft() + 6, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 1));
        if (MiscTools.inBounds(getGuiLeft() + 34, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 0));
        if (MiscTools.inBounds(getGuiLeft() + 62, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 2));
        if (MiscTools.inBounds(getGuiLeft() + 90, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 3));
        if (MiscTools.inBounds(getGuiLeft() + 118, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 4));
        if (MiscTools.inBounds(getGuiLeft() + 146, getGuiTop() + 4, 24, 12, x, y))
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 5));

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
