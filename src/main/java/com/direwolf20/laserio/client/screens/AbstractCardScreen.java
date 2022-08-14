package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.awt.*;

public abstract class AbstractCardScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>  {

    public final Color ItemCardColor = new Color(0x80c71f);
    public final Color FluidCardColor = new Color(0x3c44a9);
    public final Color EnergyCardColor = new Color(0xffd83d);
    public final Color RedstoneCardColor = new Color(0xb02e26);

    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/redstonecard.png");

    private final int HeaderOffset = 20;
    private final int ColorInset = 5;

    public AbstractCardScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    public abstract Color cardColor();

    public abstract Component cardTypeName();

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY - HeaderOffset, 0, 0, this.imageWidth, this.imageHeight);
        fill(matrixStack, relX + ColorInset, relY - HeaderOffset + ColorInset,
            relX + this.imageWidth - ColorInset, relY, cardColor().getRGB());

        Font font = Minecraft.getInstance().font;
        matrixStack.pushPose();
        float scale = 1f;
        matrixStack.scale(scale, scale, scale);
        FormattedCharSequence text = this.cardTypeName().getVisualOrderText();
        float x = (relX + this.imageWidth / 2f - font.width(text) / 2f) / scale;
        float y = (relY - HeaderOffset + ColorInset + 4) / scale;
        font.drawShadow(matrixStack, text, x, y, Color.WHITE.getRGB());
        matrixStack.popPose();
    }
    
}
