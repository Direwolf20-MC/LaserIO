package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardHolderContainer;
import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenCard;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CardHolderScreen extends AbstractContainerScreen<CardHolderContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/cardholder.png");

    protected final CardHolderContainer container;

    public CardHolderScreen(CardHolderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.imageHeight = 181;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //super.renderLabels(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (btn == 1 && hoveredSlot instanceof CardHolderSlot) { //Right click
            int slot = hoveredSlot.getSlotIndex();
            PacketHandler.sendToServer(new PacketOpenCard(slot, new BlockPos(0, -9999, 0), false));
            return true;
        }
        return super.mouseClicked(x, y, btn);
    }
}
