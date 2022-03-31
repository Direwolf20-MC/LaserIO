package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LaserConnectorBERender implements BlockEntityRenderer<LaserConnectorBE> {
    public LaserConnectorBERender(BlockEntityRendererProvider.Context p_173636_) {

    }
    @Override
    public void render(LaserConnectorBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        //System.out.println("Hello there!");
        RenderUtils.drawLasers(blockentity, blockentity.getBlockPos(), blockentity.getBlockPos().above(),partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);
    }
}
