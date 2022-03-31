package com.direwolf20.laserio.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.*;

public class RenderUtils {
    public static void render(Matrix4f matrix, VertexConsumer builder, BlockPos pos, Color color, float scale) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .5f;

        float startX = 0 + (1 - scale) / 2, startY = 0 + (1 - scale) / 2, startZ = -1 + (1 - scale) / 2, endX = 1 - (1 - scale) / 2, endY = 1 - (1 - scale) / 2, endZ = 0 - (1 - scale) / 2;

        //down
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
    }

    public static void drawLasers(BlockEntity be, BlockPos startBlock, BlockPos endBlock, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        Level level = be.getLevel();
        long gameTime = level.getGameTime();
        double v = gameTime * 0.04;

        float diffX = endBlock.getX() + .5f - startBlock.getX();
        float diffY = endBlock.getY() + .5f - startBlock.getY();
        float diffZ = endBlock.getZ() + .5f - startBlock.getZ();

        VertexConsumer builder;

        matrixStackIn.pushPose();
        Matrix4f positionMatrix = matrixStackIn.last().pose();

        builder = bufferIn.getBuffer(MyRenderType.LASER_MAIN_BEAM);

        //matrixStackIn.translate(startBlock.getX(), startBlock.getY(), startBlock.getZ());

        Vector3f startLaser = new Vector3f(0.5f, .5f, 0.5f);
        Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
        //Vector3f sortPos = new Vector3f(startBlock.getX(), startBlock.getY(), startBlock.getZ());

        drawLaser(builder, positionMatrix, endLaser, startLaser, 1, 0, 0, 0.5f, 0.025f, v, v + diffY * 1.5, be);

        matrixStackIn.popPose();
        bufferIn.getBuffer(MyRenderType.LASER_MAIN_BEAM).endVertex();
    }

    public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, BlockEntity be) {
        //This method takes the player's position into account, and adjusts the beam so that its rendered properly whereever you stand
        Player player = Minecraft.getInstance().player;
        Vector3f P = new Vector3f((float) player.getX() - be.getBlockPos().getX(), (float) player.getEyeY() - be.getBlockPos().getY(), (float) player.getZ() - be.getBlockPos().getZ());

        Vector3f PS = from.copy();
        PS.sub(P);
        Vector3f SE = to.copy();
        SE.sub(from);

        Vector3f adjustedVec = PS.copy();
        adjustedVec.cross(SE);
        adjustedVec.normalize();
        return adjustedVec;
    }

    public static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to, float r, float g, float b, float alpha, float thickness, double v1, double v2, BlockEntity be) {
        Vector3f adjustedVec = adjustBeamToEyes(from, to, be);
        adjustedVec.mul(thickness); //Determines how thick the beam is

        Vector3f p1 = from.copy();
        p1.add(adjustedVec);
        Vector3f p2 = from.copy();
        p2.sub(adjustedVec);
        Vector3f p3 = to.copy();
        p3.add(adjustedVec);
        Vector3f p4 = to.copy();
        p4.sub(adjustedVec);

        builder.vertex(positionMatrix, p1.x(), p1.y(), p1.z())
                .color(r, g, b, alpha)
                .uv(1, (float) v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .endVertex();
        builder.vertex(positionMatrix, p3.x(), p3.y(), p3.z())
                .color(r, g, b, alpha)
                .uv(1, (float) v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .endVertex();
        builder.vertex(positionMatrix, p4.x(), p4.y(), p4.z())
                .color(r, g, b, alpha)
                .uv(0, (float) v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .endVertex();
        builder.vertex(positionMatrix, p2.x(), p2.y(), p2.z())
                .color(r, g, b, alpha)
                .uv(0, (float) v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .endVertex();
    }
}
