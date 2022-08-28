package com.direwolf20.laserio.client.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends Button {
    private ResourceLocation texture;

    public IconButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress);

        this.texture = texture;
    }

    public IconButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, Component.empty(), onPress, onTooltip);

        this.texture = texture;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
        blit(stack, this.x, this.y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(PoseStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        super.onClick(p_onClick_1_, p_onClick_3_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
