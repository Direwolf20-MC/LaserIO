package com.direwolf20.laserio.client.blockentityrenders.baseberender;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

import java.util.Set;

public class BaseLaserBERender<T extends BaseLaserBE> implements BlockEntityRenderer<T> {
    public BaseLaserBERender(BlockEntityRendererProvider.Context p_173636_) {

    }

    @Override
    public void render(T blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        Set<BlockPos> renderedConnections = blockentity.getRenderedConnections();
        for (BlockPos target : renderedConnections)
            RenderUtils.drawLasers(blockentity, BlockPos.ZERO, target, partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);

    }
}
