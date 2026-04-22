package com.direwolf20.laserio.client.blockentityrenders.baseberender;

import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BaseLaserBERender<T extends BaseLaserBE, S extends BaseLaserBERender.BaseLaserRenderState>
        implements BlockEntityRenderer<T, S> {

    public static class BaseLaserRenderState extends BlockEntityRenderState {
    }

    public BaseLaserBERender(BlockEntityRendererProvider.Context context) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public S createRenderState() {
        return (S) new BaseLaserRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (!blockEntity.getRenderedConnections().isEmpty()) DelayedRenderer.add(blockEntity);
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Connection beams are drawn via DelayedRenderer on RenderLevelStageEvent.AfterTranslucentBlocks.
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().above(10).north(10).east(10), blockEntity.getBlockPos().below(10).south(10).west(10));
    }
}
