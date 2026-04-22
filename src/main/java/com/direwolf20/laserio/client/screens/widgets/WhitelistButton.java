package com.direwolf20.laserio.client.screens.widgets;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class WhitelistButton extends Button {
    private boolean isWhitelist;
    private Identifier allow = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/allowlisttrue.png");
    private Identifier block = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/allowlistfalse.png");

    public WhitelistButton(int widthIn, int heightIn, int width, int height, boolean isWhitelist, OnPress onPress) {
        super(widthIn, heightIn, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.isWhitelist = isWhitelist;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, isWhitelist ? allow : block, this.getX(), this.getY(), 0, 0, 16, 16, 16, 16);
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }
}
