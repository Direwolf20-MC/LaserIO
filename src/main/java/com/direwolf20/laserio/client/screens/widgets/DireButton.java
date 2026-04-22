package com.direwolf20.laserio.client.screens.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class DireButton extends Button {

    public DireButton(int x, int y, int widthIn, int heightIn, Component buttonText, OnPress action) {
        super(x, y, widthIn, heightIn, buttonText, action, Button.DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        Font fontrenderer = Minecraft.getInstance().font;
        this.isHovered = isMouseOver(mouseX, mouseY);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        int j = 0xFFE0E0E0;

        if (this.packedFGColor != UNSET_FG_COLOR) {
            j = this.packedFGColor | 0xFF000000;
        } else if (!this.active) {
            j = 0xFFA0A0A0;
        } else if (this.isHovered) {
            j = 0xFFFFFFA0;
        }

        String msg = this.getMessage().getString();
        int textX = this.getX() + this.width / 2 - fontrenderer.width(msg) / 2;
        int textY = this.getY() + (this.height - 7) / 2;
        guiGraphics.text(fontrenderer, msg, textX, textY, j);
    }
}
