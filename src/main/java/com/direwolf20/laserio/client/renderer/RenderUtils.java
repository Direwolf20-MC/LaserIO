package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.items.LaserWrench;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.CardRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Queue;
import java.util.Set;

import static com.direwolf20.laserio.client.events.ClientEvents.getWrench;
import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class RenderUtils {
    public static void render(Matrix4f matrix, VertexConsumer builder, BlockPos pos, Color color, float scale) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .5f;

        float startX = 0 + (1 - scale) / 2, startY = 0 + (1 - scale) / 2, startZ = -1 + (1 - scale) / 2, endX = 1 - (1 - scale) / 2, endY = 1 - (1 - scale) / 2, endZ = 0 - (1 - scale) / 2;

        //down
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);

        //up
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);

        //east
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);

        //west
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);

        //south
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);

        //north
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
    }

    public static void drawLasersLast2(Queue<BaseLaserBE> beRenders, PoseStack matrixStackIn) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        builder = buffer.getBuffer(MyRenderType.CONNECTING_LASER);
        while (beRenders.size() > 0) {
            BaseLaserBE be = beRenders.remove();
            Level level = be.getLevel();
            long gameTime = level.getGameTime();
            double v = gameTime * 0.04;
            BlockPos startBlock = be.getBlockPos();
            matrixStackIn.pushPose();
            Matrix4f positionMatrix = matrixStackIn.last().pose();

            matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);

            Vector3f startLaser = new Vector3f(0.5f, .5f, 0.5f);
            for (BlockPos target : be.getRenderedConnections()) {
                BlockPos endBlock = be.getWorldPos(target);
                Color color = be.getColor();
                Player myplayer = Minecraft.getInstance().player;
                ItemStack myItem = getWrench(myplayer);
                int alpha = (myItem.getItem() instanceof LaserWrench) ? Math.min(color.getAlpha() + be.getWrenchAlpha(), 255) : color.getAlpha();
                float diffX = endBlock.getX() + .5f - startBlock.getX();
                float diffY = endBlock.getY() + .5f - startBlock.getY();
                float diffZ = endBlock.getZ() + .5f - startBlock.getZ();
                Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
                drawLaser(builder, positionMatrix, endLaser, startLaser, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f, 0.025f, v, v + diffY * 1.5, be);
            }

            if (be instanceof LaserConnectorAdvBE laserConnectorAdvBE && laserConnectorAdvBE.getPartnerGlobalPos() != null && level.getBlockState(be.getBlockPos()).getBlock().equals(Registration.LaserConnectorAdv.get())) {
                Direction facing = level.getBlockState(be.getBlockPos()).getValue(BlockStateProperties.FACING).getOpposite();
                BlockPos endBlock = laserConnectorAdvBE.getBlockPos().relative(facing);
                Color color = be.getColor();
                Player myplayer = Minecraft.getInstance().player;
                ItemStack myItem = getWrench(myplayer);
                int alpha = (myItem.getItem() instanceof LaserWrench) ? Math.min(color.getAlpha() + be.getWrenchAlpha(), 255) : color.getAlpha();
                Vector3f endLaser = calculateEndAdvConnector(startBlock, endBlock, facing);
                drawLaser(builder, positionMatrix, endLaser, startLaser, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f, 0.025f, v, v + endLaser.y() * 1.5, be);
            }
            matrixStackIn.popPose();
        }
        buffer.endBatch(MyRenderType.CONNECTING_LASER); //This apparently is needed in RenderWorldLast
    }

    public static Vector3f calculateEndAdvConnector(BlockPos startBlock, BlockPos endBlock, Direction facing) {

        float diffX = endBlock.getX() - startBlock.getX();
        float diffY = endBlock.getY() - startBlock.getY();
        float diffZ = endBlock.getZ() - startBlock.getZ();

        switch (facing) {
            case UP:
                diffX += 0.5f;
                diffY -= 0.25f;
                diffZ += 0.5f;
                break;
            case DOWN:
                diffX += 0.5f;
                diffY += 1.25f;
                diffZ += 0.5f;
                break;
            case NORTH:
                diffX += 0.5f;
                diffY += 0.5f;
                diffZ += 1.25f;
                break;
            case SOUTH:
                diffX += 0.5f;
                diffY += 0.5f;
                diffZ -= 0.25f;
                break;
            case EAST:
                diffX -= 0.25f;
                diffY += 0.5f;
                diffZ += 0.5f;
                break;
            case WEST:
                diffX += 1.25f;
                diffY += 0.5f;
                diffZ += 0.5f;
                break;
            default:
                break;
        }
        return new Vector3f(diffX, diffY, diffZ);
    }

    public static void drawConnectingLasersLast4(Set<LaserNodeBE> beConnectingRenders, PoseStack matrixStackIn) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer builder;

        float alpha = 1f;
        float thickness = 0.0175f;

        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_BEAM);
        for (LaserNodeBE be : beConnectingRenders) {
            Level level = be.getLevel();
            long gameTime = level.getGameTime();
            double v = gameTime * 0.04;

            BlockPos startBlock = be.getBlockPos();

            matrixStackIn.pushPose();
            Matrix4f positionMatrix = matrixStackIn.last().pose();
            matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);

            for (CardRender cardRender : be.cardRenders) {
                drawLaser(builder, positionMatrix, cardRender.endLaser, cardRender.startLaser, cardRender.r, cardRender.g, cardRender.b, alpha, thickness, v, v + cardRender.diffY * 4.5, be);
            }
            matrixStackIn.popPose();
        }
        buffer.endBatch(MyRenderType.LASER_MAIN_BEAM); //This apparently is needed in RenderWorldLast

        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_CORE);
        for (LaserNodeBE be : beConnectingRenders) {
            Level level = be.getLevel();
            long gameTime = level.getGameTime();
            double v = gameTime * 0.04;

            BlockPos startBlock = be.getBlockPos();

            matrixStackIn.pushPose();
            Matrix4f positionMatrix = matrixStackIn.last().pose();
            matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);

            for (CardRender cardRender : be.cardRenders) {
                drawLaser(builder, positionMatrix, cardRender.endLaser, cardRender.startLaser, cardRender.floatcolors[0], cardRender.floatcolors[1], cardRender.floatcolors[2], 1f, 0.0125f, v, v + cardRender.diffY * 1.5, be);
            }
            matrixStackIn.popPose();
        }
        buffer.endBatch(MyRenderType.LASER_MAIN_CORE); //This apparently is needed in RenderWorldLast
    }

    public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, BlockEntity be) {
        //This method takes the player's position into account, and adjusts the beam so that its rendered properly whereever you stand
        Player player = Minecraft.getInstance().player;
        Vector3f P = new Vector3f((float) player.getX() - be.getBlockPos().getX(), (float) player.getEyeY() - be.getBlockPos().getY(), (float) player.getZ() - be.getBlockPos().getZ());

        Vector3f PS = new Vector3f(from);
        PS.sub(P);
        Vector3f SE = new Vector3f(to);
        SE.sub(from);

        Vector3f adjustedVec = new Vector3f(PS);
        adjustedVec.cross(SE);
        adjustedVec.normalize();
        return adjustedVec;
    }

    public static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to, float r, float g, float b, float alpha, float thickness, double v1, double v2, BlockEntity be) {
        Vector3f adjustedVec = adjustBeamToEyes(from, to, be);
        adjustedVec.mul(thickness); //Determines how thick the beam is

        Vector3f p1 = new Vector3f(from);
        p1.add(adjustedVec);
        Vector3f p2 = new Vector3f(from);
        p2.sub(adjustedVec);
        Vector3f p3 = new Vector3f(to);
        p3.add(adjustedVec);
        Vector3f p4 = new Vector3f(to);
        p4.sub(adjustedVec);

        builder.addVertex(positionMatrix, p1.x(), p1.y(), p1.z())
                .setColor(r, g, b, alpha)
                .setUv(1, (float) v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
        ;
        builder.addVertex(positionMatrix, p3.x(), p3.y(), p3.z())
                .setColor(r, g, b, alpha)
                .setUv(1, (float) v2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
        ;
        builder.addVertex(positionMatrix, p4.x(), p4.y(), p4.z())
                .setColor(r, g, b, alpha)
                .setUv(0, (float) v2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
        ;
        builder.addVertex(positionMatrix, p2.x(), p2.y(), p2.z())
                .setColor(r, g, b, alpha)
                .setUv(0, (float) v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
        ;
    }
}
