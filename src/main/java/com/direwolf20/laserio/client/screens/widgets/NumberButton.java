package com.direwolf20.laserio.client.screens.widgets;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class NumberButton extends Button {
    private final ResourceLocation resourceLocation = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/blankbutton.png");
    private int value;

    public NumberButton(int x, int y, int width, int height, int value, OnPress onPress) {
        super(x, y, width, height, net.minecraft.network.chat.Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.value = value;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, resourceLocation);
        blit(stack, this.x, this.y, 0, 0, width, height, width, height);*/
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF353535);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFFD8D8D8);
        Font font = Minecraft.getInstance().font;
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        float scale = 0.75f;//value > 99 || value < -99 ? 0.75f : 0.75f;
        stack.scale(scale, scale, scale);
        String msg = String.format("%,d", value);
        ;
        float x = (this.getX() + this.width / 2f) / scale - font.width(msg) / 2f;
        float y = (this.getY() + (this.height - font.lineHeight) / 2f / scale) / scale + 1;
        //font.draw(stack, msg, x, y, Color.DARK_GRAY.getRGB());
        guiGraphics.drawString(font, msg, x, y, Color.DARK_GRAY.getRGB(), false);
        //drawCenteredString(stack, font, String.valueOf(value), (this.x + this.width / 2)*2, (this.y + (this.height - 8) / 2)*2, Color.DARK_GRAY.getRGB());
        stack.popPose();
        //drawCenteredString(stack, font, String.valueOf(channel), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        super.onClick(p_onClick_1_, p_onClick_3_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y, button);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
