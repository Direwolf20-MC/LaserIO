package com.direwolf20.laserio.client.screens.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ToggleButton extends Button {
    private Identifier[] textures;
    private int texturePosition;

    public ToggleButton(int x, int y, int width, int height, Identifier[] textures, int texturePosition, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);

        this.textures = textures;
        setTexturePosition(texturePosition);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textures[texturePosition], this.getX(), this.getY(), 0, 0, width, height, width, height);
    }

    public int getTexturePosition() {
        return texturePosition;
    }

    public void setTexturePosition(int texturePosition) {
        if (texturePosition > textures.length)
            this.texturePosition = textures.length;
        else
            this.texturePosition = texturePosition;
    }

    public void nextTexturePosition() {
        if (texturePosition == textures.length)
            texturePosition = 0;
        else
            texturePosition++;
    }
}
