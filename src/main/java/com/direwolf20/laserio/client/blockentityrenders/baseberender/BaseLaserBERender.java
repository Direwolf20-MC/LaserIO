package com.direwolf20.laserio.client.blockentityrenders.baseberender;

import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class BaseLaserBERender<T extends BaseLaserBE> implements BlockEntityRenderer<T> {
    public BaseLaserBERender(BlockEntityRendererProvider.Context p_173636_) {

    }

    @Override
    public void render(T blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        if (blockentity.getRenderedConnections().size() > 0) DelayedRenderer.add(blockentity);
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().above(10).north(10).east(10), blockEntity.getBlockPos().below(10).south(10).west(10));
    }
}
