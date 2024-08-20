package com.direwolf20.laserio.client.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LaserGuiGraphics extends GuiGraphics {
    public Minecraft minecraft;

    public LaserGuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        super(minecraft, bufferSource);
        this.minecraft = minecraft;
    }

    @Override
    public void renderItemDecorations(Font font, ItemStack itemStack, int x, int y, @Nullable String altText) {
        PoseStack pose = pose();
        if (!itemStack.isEmpty()) {
            pose.pushPose();
            if (itemStack.getCount() != 1 || altText != null) {
                String s = altText == null ? String.valueOf(itemStack.getCount()) : altText;
                pose().translate(0.0F, 0.0F, 200.0F);
                if (itemStack.getCount() > 99) {
                    pose.pushPose();
                    pose.translate(x, y, 300);
                    pose.scale(0.65f, 0.65f, 0.65f);
                    this.drawString(font, s, (float) (17 - font.width(s) * 0.65f), (float) (17), 16777215, true);
                    pose.popPose();
                } else {
                    this.drawString(font, s, x + 19 - 2 - font.width(s), y + 6 + 3, 16777215, true);
                }
            }

            if (itemStack.isBarVisible()) {
                int l = itemStack.getBarWidth();
                int i = itemStack.getBarColor();
                int j = x + 2;
                int k = y + 13;
                this.fill(RenderType.guiOverlay(), j, k, j + 13, k + 2, -16777216);
                this.fill(RenderType.guiOverlay(), j, k, j + l, k + 1, i | -16777216);
            }

            LocalPlayer localplayer = this.minecraft.player;
            float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemStack.getItem(), this.minecraft.getFrameTime());
            if (f > 0.0F) {
                int i1 = y + Mth.floor(16.0F * (1.0F - f));
                int j1 = i1 + Mth.ceil(16.0F * f);
                this.fill(RenderType.guiOverlay(), x, i1, x + 16, j1, Integer.MAX_VALUE);
            }

            pose.popPose();
            net.minecraftforge.client.ItemDecoratorHandler.of(itemStack).render(this, font, itemStack, x, y);
        }
    }

    public void renderItemScale(float scale, ItemStack itemStack, int x, int y) {
        PoseStack posestack = pose();
        posestack.pushPose();
        posestack.translate(x, y, 500);
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(scale, scale, scale);

        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        this.minecraft.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, pose(), this.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        this.flush();
        if (flag) {
            Lighting.setupFor3DItems();
        }
        posestack.popPose();
    }

}