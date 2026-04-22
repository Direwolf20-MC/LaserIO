package com.direwolf20.laserio.client.screens.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import org.joml.Matrix3x2fStack;

import java.awt.*;

public class NumberButton extends Button {
    private int value;

    public NumberButton(int x, int y, int width, int height, int value, OnPress onPress) {
        super(x, y, width, height, net.minecraft.network.chat.Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.value = value;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF353535);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFFD8D8D8);
        Font font = Minecraft.getInstance().font;
        Matrix3x2fStack stack = guiGraphics.pose();
        stack.pushMatrix();
        float scale = 0.75f;
        stack.scale(scale, scale);
        String msg = String.format("%,d", value);
        float x = (this.getX() + this.width / 2f) / scale - font.width(msg) / 2f;
        float y = (this.getY() + (this.height - font.lineHeight) / 2f / scale) / scale + 1;
        guiGraphics.text(font, msg, (int) x, (int) y, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        stack.popMatrix();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
