package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

import java.util.Set;

public class LaserConnectorBERender implements BlockEntityRenderer<LaserConnectorBE> {
    public LaserConnectorBERender(BlockEntityRendererProvider.Context p_173636_) {

    }
    @Override
    public void render(LaserConnectorBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        Set<BlockPos> renderedConnections = blockentity.getRenderedConnections();
        renderedConnections.forEach((target)-> {
            RenderUtils.drawLasers(blockentity, BlockPos.ZERO, target,partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);
        });
    }

    /*@Override
    public boolean shouldRenderOffScreen(LaserConnectorBE be) {
        return false;
    }*/
}
