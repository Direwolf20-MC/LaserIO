package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class MyRenderType {
    private MyRenderType() {
    }

    private static final Identifier laserBeam = Identifier.parse(LaserIO.MODID + ":textures/misc/laser.png");
    private static final Identifier laserBeam2 = Identifier.parse(LaserIO.MODID + ":textures/misc/laser2.png");
    private static final Identifier laserBeamGlow = Identifier.parse(LaserIO.MODID + ":textures/misc/laser_glow.png");

    // Custom pipeline: POSITION_COLOR, NO_CULL, translucent blend, depth test ALWAYS_PASS (draw through walls).
    // Used by BlockOverlay for wrench-selected-block highlight so it remains visible through geometry.
    static final RenderPipeline BLOCK_OVERLAY_PIPELINE = RenderPipelines.DEBUG_QUADS.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(LaserIO.MODID, "pipeline/block_overlay"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BLOCK_OVERLAY_PIPELINE);
    }

    public static final RenderType LASER_MAIN_BEAM = RenderType.create(
            "MiningLaserMainBeam",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", laserBeam2)
                    .useLightmap()
                    .createRenderSetup()
    );

    public static final RenderType LASER_MAIN_CORE = RenderType.create(
            "MiningLaserCoreBeam",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", laserBeam)
                    .useLightmap()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    public static final RenderType CONNECTING_LASER = RenderType.create(
            "ConnectingLaser",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", laserBeam)
                    .useLightmap()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    // Unused in current code paths; kept for parity with pre-port surface.
    public static final RenderType LASER_MAIN_ADDITIVE = RenderType.create(
            "MiningLaserAdditiveBeam",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                    .withTexture("Sampler0", laserBeamGlow)
                    .useLightmap()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    public static final RenderType BlockOverlay = RenderType.create(
            "MiningLaserBlockOverlay",
            RenderSetup.builder(BLOCK_OVERLAY_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
}
