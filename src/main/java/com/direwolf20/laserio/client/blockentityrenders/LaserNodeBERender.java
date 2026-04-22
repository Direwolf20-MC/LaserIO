package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.blockentityrenders.baseberender.BaseLaserBERender;
import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public class LaserNodeBERender extends BaseLaserBERender<LaserNodeBE, LaserNodeBERender.LaserNodeRenderState> {
    public static final Vector3f[] offsets = {
            new Vector3f(0.65f, 0.65f, 0.5f),
            new Vector3f(0.5f, 0.65f, 0.5f),
            new Vector3f(0.35f, 0.65f, 0.5f),
            new Vector3f(0.65f, 0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(0.35f, 0.5f, 0.5f),
            new Vector3f(0.65f, 0.35f, 0.5f),
            new Vector3f(0.5f, 0.35f, 0.5f),
            new Vector3f(0.35f, 0.35f, 0.5f)
    };
    public static final Color[] colors = {
            new Color(255, 255, 255),
            new Color(249, 128, 29),
            new Color(198, 79, 189),
            new Color(58, 179, 218),
            new Color(255, 216, 61),
            new Color(128, 199, 31),
            new Color(243, 140, 170),
            new Color(71, 79, 82),
            new Color(156, 157, 151),
            new Color(22, 156, 157),
            new Color(137, 50, 183),
            new Color(60, 68, 169),
            new Color(130, 84, 50),
            new Color(93, 124, 21),
            new Color(176, 46, 38),
            new Color(29, 28, 33)
    };

    public static class LaserNodeRenderState extends BaseLaserRenderState {
    }

    public LaserNodeBERender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LaserNodeRenderState createRenderState() {
        return new LaserNodeRenderState();
    }

    @Override
    public void extractRenderState(LaserNodeBE blockEntity, LaserNodeRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (!blockEntity.rendersChecked) blockEntity.populateRenderList();
        DelayedRenderer.addConnecting(blockEntity);
    }
}
