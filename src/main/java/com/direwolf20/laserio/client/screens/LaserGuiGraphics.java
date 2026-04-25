package com.direwolf20.laserio.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class LaserGuiGraphics {
    private LaserGuiGraphics() {
    }

    /**
     * Renders an item at a custom size. {@code pixelSize} is the desired width/height in screen pixels
     * (e.g. {@code 8f} = half a slot, {@code 16f} = full slot). {@code GuiGraphics#item} renders at the
     * default 16px size, so we divide by 16 to convert pixel-size into a multiplier — matches the
     * 1.21.1 {@code LaserGuiGraphics#renderItemScale} semantics callers expect.
     */
    public static void renderItemScale(GuiGraphicsExtractor guiGraphics, float pixelSize, ItemStack itemStack, int x, int y) {
        if (itemStack.isEmpty()) return;
        float multiplier = pixelSize / 16f;
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(x + 8f, y + 8f);
        pose.scale(multiplier, multiplier);
        guiGraphics.item(itemStack, -8, -8, 0);
        pose.popMatrix();
    }

    public static void renderItemScaleWithDecorations(GuiGraphicsExtractor guiGraphics, float scale, ItemStack itemStack, int x, int y) {
        if (itemStack.isEmpty()) return;
        renderItemScale(guiGraphics, scale, itemStack, x, y);
        Font font = Minecraft.getInstance().font;
        guiGraphics.itemDecorations(font, itemStack, x, y, null);
    }
}
