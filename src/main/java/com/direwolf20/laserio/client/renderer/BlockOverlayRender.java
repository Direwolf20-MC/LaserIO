package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.*;

public class BlockOverlayRender {
    public static void renderSelectedBlock(RenderLevelStageEvent event, BlockPos pos, BaseLaserBE be) {
        final Minecraft mc = Minecraft.getInstance();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        float scale = (be instanceof LaserConnectorBE || be instanceof LaserConnectorAdvBE) ? 0.375f : 0.625f;


        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x(), -view.y(), -view.z());

        VertexConsumer builder;
        //MyRenderType.updateRenders();
        builder = buffer.getBuffer(MyRenderType.BlockOverlay);

        matrix.pushPose();
        matrix.translate(pos.getX(), pos.getY(), pos.getZ());
        matrix.translate(-0.005f, -0.005f, -0.005f);
        matrix.scale(1.01f, 1.01f, 1.01f);
        matrix.mulPose(Axis.YP.rotationDegrees(-90.0F));

        Matrix4f positionMatrix = matrix.last().pose();
        RenderUtils.render(positionMatrix, builder, pos, Color.GREEN, scale);
        matrix.popPose();

        matrix.popPose();
        //RenderSystem.disableDepthTest();
        buffer.endBatch(MyRenderType.BlockOverlay);
    }
}
