package com.direwolf20.laserio.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import java.awt.Color;

public class CardHeaderPanel extends Panel {

    private Color color;

    public CardHeaderPanel(int pX, int pY, int pWidth, int pHeight, Component pMessage, Color color) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.color = color;
        setColorsFromHighlight(color);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        
        //fill(pPoseStack, this.x + 4, this.y + 4, this.x + this.width - 4, this.y + 8, color);
    }
    
}
