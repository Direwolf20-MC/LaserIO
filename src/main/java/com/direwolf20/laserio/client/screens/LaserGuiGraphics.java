package com.direwolf20.laserio.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class LaserGuiGraphics {
    private LaserGuiGraphics() {
    }

    public static void renderItemScale(GuiGraphicsExtractor guiGraphics, float scale, ItemStack itemStack, int x, int y) {
        if (itemStack.isEmpty()) return;
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(x + 8f, y + 8f);
        pose.scale(scale, scale);
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
