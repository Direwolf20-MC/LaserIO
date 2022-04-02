package com.direwolf20.laserio.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class WhitelistButton extends Button {
    private boolean isWhitelist;

    public WhitelistButton(int widthIn, int heightIn, int width, int height, boolean isWhitelist, OnPress onPress) {
        super(widthIn, heightIn, width, height, TextComponent.EMPTY, onPress);
        this.isWhitelist = isWhitelist;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        fill(stack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFFa8a8a8);
        fill(stack, this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2, this.isWhitelist ? 0xFFFFFFFF : 0xFF000000);
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }
}
