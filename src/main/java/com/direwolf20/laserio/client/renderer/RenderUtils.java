package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.util.CardRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.awt.*;
import java.util.EnumMap;
import java.util.Queue;
import java.util.Set;

import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class RenderUtils {
    private static EnumMap<BaseCard.CardType, Color> cardTypeColors = new EnumMap<BaseCard.CardType, Color>(BaseCard.CardType.class);
    
    static {
        cardTypeColors.put(BaseCard.CardType.ITEM, new Color(0f, 1f, 0f));
        cardTypeColors.put(BaseCard.CardType.FLUID, new Color(0f, 0f, 1f));
        cardTypeColors.put(BaseCard.CardType.ENERGY, new Color(1f, 1f, 0f));
        cardTypeColors.put(BaseCard.CardType.REDSTONE, new Color(1f, 0f, 0f));
    }

    public static Color getColor(BaseCard.CardType cardType){
        Color color = cardTypeColors.get(cardType);
        if (color == null)
            return Color.BLACK;
        return color;
    }

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

    public static void drawLasersTile(BlockEntity be, BlockPos startBlock, BlockPos endBlock, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
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

        Vector3f startLaser = new Vector3f(0.5f, .5f, 0.5f);
        Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);

        drawLaser(builder, positionMatrix, endLaser, startLaser, 1, 0, 0, 0.5f, 0.025f, v, v + diffY * 1.5, be);

        matrixStackIn.popPose();
    }

    public static void drawLasersLast(BlockEntity be, BlockPos startBlock, BlockPos endBlock, PoseStack matrixStackIn) {
        Level level = be.getLevel();
        long gameTime = level.getGameTime();
        double v = gameTime * 0.04;
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        float diffX = endBlock.getX() + .5f - startBlock.getX();
        float diffY = endBlock.getY() + .5f - startBlock.getY();
        float diffZ = endBlock.getZ() + .5f - startBlock.getZ();


        VertexConsumer builder;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStackIn.pushPose();
        Matrix4f positionMatrix = matrixStackIn.last().pose();

        matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);

        builder = buffer.getBuffer(MyRenderType.CONNECTING_LASER);

        Vector3f startLaser = new Vector3f(0.5f, .5f, 0.5f);
        Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);

        drawLaser(builder, positionMatrix, endLaser, startLaser, 1, 0, 0, 0.33f, 0.025f, v, v + diffY * 1.5, be);

        matrixStackIn.popPose();
        buffer.endBatch(MyRenderType.CONNECTING_LASER); //This apparently is needed in RenderWorldLast
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
                float diffX = endBlock.getX() + .5f - startBlock.getX();
                float diffY = endBlock.getY() + .5f - startBlock.getY();
                float diffZ = endBlock.getZ() + .5f - startBlock.getZ();
                Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
                drawLaser(builder, positionMatrix, endLaser, startLaser, 1, 0, 0, 0.33f, 0.025f, v, v + diffY * 1.5, be);
            }
            matrixStackIn.popPose();
        }
        buffer.endBatch(MyRenderType.CONNECTING_LASER); //This apparently is needed in RenderWorldLast
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

    public static void drawConnectingLasersLast3(Set<LaserNodeBE> beConnectingRenders, PoseStack matrixStackIn) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer builder;
        float r = 0f;
        float g = 1f;
        float b = 0f;
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

            for (Direction direction : Direction.values()) { //Todo Improve
                IItemHandler h = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(new ItemStackHandler(0));
                for (int slot = 0; slot < h.getSlots(); slot++) {
                    ItemStack card = h.getStackInSlot(slot);
                    if (card.getItem() instanceof BaseCard) {
                        if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
                            if (be.getAttachedInventoryNoCache(direction, (byte) -1).equals(LazyOptional.empty()))
                                continue;
                            boolean reverse = !direction.equals(Direction.DOWN);
                            if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.EXTRACT)
                                reverse = !reverse;

                            BlockPos endBlock = startBlock.relative(direction);
                            Vector3f offset = findOffset(direction, slot, LaserNodeBERender.offsets);
                            float diffX = endBlock.getX() + offset.x() - startBlock.getX();
                            float diffY = endBlock.getY() + offset.y() - startBlock.getY();
                            float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

                            Vector3f endLaser;
                            Vector3f startLaser;

                            if (reverse) {
                                endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                                startLaser = new Vector3f(diffX, diffY, diffZ);
                            } else {
                                startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                                endLaser = new Vector3f(diffX, diffY, diffZ);
                            }

                            drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness, v, v + diffY * 4.5, be);
                        }
                    }
                }
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

            for (Direction direction : Direction.values()) { //Todo Improve
                IItemHandler h = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(new ItemStackHandler(0));
                for (int slot = 0; slot < h.getSlots(); slot++) {
                    ItemStack card = h.getStackInSlot(slot);
                    if (card.getItem() instanceof BaseCard) {
                        if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
                            if (be.getAttachedInventoryNoCache(direction, (byte) -1).equals(LazyOptional.empty()))
                                continue;
                            float[] floatcolors = LaserNodeBERender.colors[BaseCard.getChannel(card)].getColorComponents(new float[3]);
                            boolean reverse = !direction.equals(Direction.DOWN);
                            if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.EXTRACT)
                                reverse = !reverse;

                            BlockPos endBlock = startBlock.relative(direction);
                            Vector3f offset = findOffset(direction, slot, LaserNodeBERender.offsets);
                            float diffX = endBlock.getX() + offset.x() - startBlock.getX();
                            float diffY = endBlock.getY() + offset.y() - startBlock.getY();
                            float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

                            Vector3f endLaser;
                            Vector3f startLaser;

                            if (reverse) {
                                endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                                startLaser = new Vector3f(diffX, diffY, diffZ);
                            } else {
                                startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                                endLaser = new Vector3f(diffX, diffY, diffZ);
                            }

                            drawLaser(builder, positionMatrix, endLaser, startLaser, floatcolors[0], floatcolors[1], floatcolors[2], 1f, 0.0125f, v, v + diffY * 1.5, be);
                        }
                    }
                }
            }
            matrixStackIn.popPose();
        }
        buffer.endBatch(MyRenderType.LASER_MAIN_CORE); //This apparently is needed in RenderWorldLast
    }

    public static void drawConnectingLasersLast2(LaserNodeBE be, PoseStack matrixStackIn) {
        Level level = be.getLevel();
        long gameTime = level.getGameTime();
        double v = gameTime * 0.04;
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer builder;

        float r = 0f;
        float g = 1f;
        float b = 0f;
        float alpha = 1f;
        float thickness = 0.0175f;
        BlockPos startBlock = be.getBlockPos();

        matrixStackIn.pushPose();
        Matrix4f positionMatrix = matrixStackIn.last().pose();
        matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);


        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_BEAM);
        for (Direction direction : Direction.values()) { //Todo Improve
            IItemHandler h = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(new ItemStackHandler(0));
            for (int slot = 0; slot < h.getSlots(); slot++) {
                ItemStack card = h.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
                        if (be.getAttachedInventoryNoCache(direction, (byte) -1).equals(LazyOptional.empty()))
                            continue;
                        boolean reverse = direction.equals(Direction.DOWN) ? false : true;
                        if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.EXTRACT)
                            reverse = !reverse;

                        BlockPos endBlock = startBlock.relative(direction);
                        Vector3f offset = findOffset(direction, slot, LaserNodeBERender.offsets);
                        float diffX = endBlock.getX() + offset.x() - startBlock.getX();
                        float diffY = endBlock.getY() + offset.y() - startBlock.getY();
                        float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

                        Vector3f endLaser;
                        Vector3f startLaser;

                        if (reverse) {
                            endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                            startLaser = new Vector3f(diffX, diffY, diffZ);
                        } else {
                            startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                            endLaser = new Vector3f(diffX, diffY, diffZ);
                        }

                        drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness, v, v + diffY * 4.5, be);
                    }
                }
            }
        }
        buffer.endBatch(MyRenderType.LASER_MAIN_BEAM); //This apparently is needed in RenderWorldLast

        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_CORE);
        for (Direction direction : Direction.values()) { //Todo Improve
            IItemHandler h = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(new ItemStackHandler(0));
            for (int slot = 0; slot < h.getSlots(); slot++) {
                ItemStack card = h.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
                        if (be.getAttachedInventoryNoCache(direction, (byte) -1).equals(LazyOptional.empty()))
                            continue;
                        float[] floatcolors = LaserNodeBERender.colors[BaseCard.getChannel(card)].getColorComponents(new float[3]);
                        boolean reverse = direction.equals(Direction.DOWN) ? false : true;
                        if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.EXTRACT)
                            reverse = !reverse;

                        BlockPos endBlock = startBlock.relative(direction);
                        Vector3f offset = findOffset(direction, slot, LaserNodeBERender.offsets);
                        float diffX = endBlock.getX() + offset.x() - startBlock.getX();
                        float diffY = endBlock.getY() + offset.y() - startBlock.getY();
                        float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

                        Vector3f endLaser;
                        Vector3f startLaser;

                        if (reverse) {
                            endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                            startLaser = new Vector3f(diffX, diffY, diffZ);
                        } else {
                            startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
                            endLaser = new Vector3f(diffX, diffY, diffZ);
                        }

                        drawLaser(builder, positionMatrix, endLaser, startLaser, floatcolors[0], floatcolors[1], floatcolors[2], 1f, 0.0125f, v, v + diffY * 1.5, be);
                    }
                }
            }
        }
        buffer.endBatch(MyRenderType.LASER_MAIN_CORE); //This apparently is needed in RenderWorldLast

        matrixStackIn.popPose();
    }

    public static void drawConnectingLasersLast(BlockEntity be, BlockPos startBlock, BlockPos endBlock, PoseStack matrixStackIn, MultiBufferSource bufferIn, Vector3f offset, float r, float g, float b, float alpha, float thickness, float r2, float g2, float b2, float alpha2, float thickness2, boolean reverse) {
        Level level = be.getLevel();
        long gameTime = level.getGameTime();
        double v = gameTime * 0.04;

        float diffX = endBlock.getX() + offset.x() - startBlock.getX();
        float diffY = endBlock.getY() + offset.y() - startBlock.getY();
        float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

        VertexConsumer builder;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        matrixStackIn.pushPose();
        Matrix4f positionMatrix = matrixStackIn.last().pose();

        matrixStackIn.translate(startBlock.getX() - projectedView.x, startBlock.getY() - projectedView.y, startBlock.getZ() - projectedView.z);

        Vector3f endLaser;
        Vector3f startLaser;

        if (reverse) {
            endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            startLaser = new Vector3f(diffX, diffY, diffZ);
        } else {
            startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            endLaser = new Vector3f(diffX, diffY, diffZ);
        }
        //MyRenderType.updateRenders();
        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_BEAM);
        drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness, v, v + diffY * 4.5, be);
        buffer.endBatch(MyRenderType.LASER_MAIN_BEAM); //This apparently is needed in RenderWorldLast

        builder = buffer.getBuffer(MyRenderType.LASER_MAIN_CORE);
        drawLaser(builder, positionMatrix, endLaser, startLaser, r2, g2, b2, alpha2, thickness2, v, v + diffY * 1.5, be);
        buffer.endBatch(MyRenderType.LASER_MAIN_CORE); //This apparently is needed in RenderWorldLast

        matrixStackIn.popPose();
    }

    public static void drawConnectingLasers(BlockEntity be, BlockPos startBlock, BlockPos endBlock, PoseStack matrixStackIn, MultiBufferSource bufferIn, Vector3f offset, float r, float g, float b, float alpha, float thickness, float r2, float g2, float b2, float alpha2, float thickness2, boolean reverse) {
        Level level = be.getLevel();
        long gameTime = level.getGameTime();
        double v = gameTime * 0.04;

        float diffX = endBlock.getX() + offset.x() - startBlock.getX();
        float diffY = endBlock.getY() + offset.y() - startBlock.getY();
        float diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

        VertexConsumer builder;

        matrixStackIn.pushPose();
        Matrix4f positionMatrix = matrixStackIn.last().pose();

        Vector3f endLaser;
        Vector3f startLaser;

        if (reverse) {
            endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            startLaser = new Vector3f(diffX, diffY, diffZ);
        } else {
            startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            endLaser = new Vector3f(diffX, diffY, diffZ);
        }
        //MyRenderType.updateRenders();
        builder = bufferIn.getBuffer(MyRenderType.LASER_MAIN_BEAM);
        drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness, v, v + diffY * 4.5, be);

        builder = bufferIn.getBuffer(MyRenderType.LASER_MAIN_CORE);
        drawLaser(builder, positionMatrix, endLaser, startLaser, r2, g2, b2, alpha2, thickness2, v, v + diffY * 1.5, be);

        matrixStackIn.popPose();
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
