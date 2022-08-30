package com.direwolf20.laserio.client.screens.widgets;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WhitelistButton extends Button {
    private boolean isWhitelist;
    private ResourceLocation allow = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlisttrue.png");
    private ResourceLocation block = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlistfalse.png");

    public WhitelistButton(int widthIn, int heightIn, int width, int height, boolean isWhitelist, OnPress onPress) {
        super(widthIn, heightIn, width, height, Component.empty(), onPress);
        this.isWhitelist = isWhitelist;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (isWhitelist)
            RenderSystem.setShaderTexture(0, allow);
        else
            RenderSystem.setShaderTexture(0, block);

        blit(stack, this.x, this.y, 0, 0, 16, 16, 16, 16);
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }
}
