package com.direwolf20.laserio.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public class SidePanel extends Panel {

    private Color color;
    private ItemStack itemStack;
    private ItemRenderer itemRenderer;

    public SidePanel(int pX, int pY, int pWidth, int pHeight, Component pMessage, Color color, ItemStack itemStack,
        ItemRenderer itemRenderer)
    {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.color = color;
        this.itemStack = itemStack;
        this.itemRenderer = itemRenderer;
        setColorsFromHighlight(color);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        var font = Minecraft.getInstance().font;
        var text = getMessage().getString();
        drawCenteredString(pPoseStack, font, text, x + width/2, y + 4, 0xffffff);

        if (this.itemStack != null)
            this.itemRenderer.renderAndDecorateFakeItem(this.itemStack, x + width/2 - 8, y + 18);
    }
    
}
