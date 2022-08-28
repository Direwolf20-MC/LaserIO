package com.direwolf20.laserio.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** This class is used to make the numbers on items in the FilterCountContainer smaller when greater than 100 **/
public class LaserIOItemRenderer extends ItemRenderer {
    public LaserIOItemRenderer(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer) {
        super(textureManager, modelManager, itemColors, blockEntityWithoutLevelRenderer);
    }

    private void fillRect(BufferBuilder p_115153_, int p_115154_, int p_115155_, int p_115156_, int p_115157_, int p_115158_, int p_115159_, int p_115160_, int p_115161_) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        p_115153_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        p_115153_.vertex((double) (p_115154_ + 0), (double) (p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + 0), (double) (p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + p_115156_), (double) (p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + p_115156_), (double) (p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        //p_115153_.end();
        BufferUploader.drawWithShader(p_115153_.end());
    }

    @Override
    public void renderGuiItemDecorations(Font font, ItemStack itemstack, int x, int y, @Nullable String altText) {
        if (!itemstack.isEmpty()) {
            PoseStack posestack = new PoseStack();
            if (itemstack.getCount() != 1 || altText != null) {
                String textToDraw = altText == null ? String.valueOf(itemstack.getCount()) : altText;
                posestack.translate(0.0D, 0.0D, (double) (this.blitOffset + 200.0F));
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                if (itemstack.getCount() > 99) {
                    posestack.pushPose();
                    posestack.translate(x, y, 300);
                    posestack.scale(0.65f, 0.65f, 0.65f);
                    font.drawInBatch(textToDraw, (float) (17 - font.width(textToDraw) * 0.65f), (float) (17), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                    posestack.popPose();
                } else {
                    font.drawInBatch(textToDraw, (float) (x + 19 - 2 - font.width(textToDraw)), (float) (y + 6 + 3), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                }
                multibuffersource$buffersource.endBatch();
            }

            if (itemstack.isBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                int i = itemstack.getBarWidth();
                int j = itemstack.getBarColor();
                this.fillRect(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(bufferbuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer localplayer = Minecraft.getInstance().player;
            float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                this.fillRect(bufferbuilder1, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemstack, int x, int y, @Nullable String altText, float scale) {
        if (!itemstack.isEmpty()) {
            PoseStack posestack = new PoseStack();
            if (itemstack.getCount() != 1 || altText != null) {
                String textToDraw = altText == null ? String.valueOf(itemstack.getCount()) : altText;
                posestack.translate(0.0D, 0.0D, (double) (this.blitOffset + 600.0F));
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                posestack.pushPose();
                posestack.translate(0, y, 300);
                posestack.scale(scale, scale, scale);
                font.drawInBatch(textToDraw, (float) (x + 13 - font.width(textToDraw) * scale) / scale, (float) (17), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                posestack.popPose();
                multibuffersource$buffersource.endBatch();
            }

            if (itemstack.isBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                int i = itemstack.getBarWidth();
                int j = itemstack.getBarColor();
                this.fillRect(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(bufferbuilder, x + (int) (2 / scale), y + 13, (int) (i * scale), 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer localplayer = Minecraft.getInstance().player;
            float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                this.fillRect(bufferbuilder1, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    public void renderGuiItem(float scale, ItemStack p_115128_, int p_115129_, int p_115130_, BakedModel p_115131_) {
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double) p_115129_, (double) p_115130_, (double) (500.0F + this.blitOffset));
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(scale, scale, scale);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !p_115131_.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        this.render(p_115128_, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, p_115131_);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
