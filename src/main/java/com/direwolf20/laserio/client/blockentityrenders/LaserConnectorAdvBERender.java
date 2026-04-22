package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.blockentityrenders.baseberender.BaseLaserBERender;
import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class LaserConnectorAdvBERender extends BaseLaserBERender<LaserConnectorAdvBE, LaserConnectorAdvBERender.LaserConnectorAdvRenderState> {

    public static class LaserConnectorAdvRenderState extends BaseLaserRenderState {
        public boolean hasPartner;
        public Direction facing = Direction.NORTH;
        public float animationTime;
    }

    public LaserConnectorAdvBERender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LaserConnectorAdvRenderState createRenderState() {
        return new LaserConnectorAdvRenderState();
    }

    @Override
    public void extractRenderState(LaserConnectorAdvBE blockEntity, LaserConnectorAdvRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        // Skip super.extractRenderState — base always enqueues on any connection. When hasPartner we want
        // the adv-connector queued unconditionally for its cross-dim beam; when not, we fall back to the
        // base's "any connections → enqueue" behavior via BlockEntityRenderState.extractBase directly.
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.hasPartner = blockEntity.getPartnerGlobalPos() != null;
        if (state.hasPartner) {
            state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
            long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
            state.animationTime = (float) Math.floorMod(gameTime, 80) + partialTicks;
            DelayedRenderer.add(blockEntity);
        } else if (!blockEntity.getRenderedConnections().isEmpty()) {
            DelayedRenderer.add(blockEntity);
        }
    }

    @Override
    public void submit(LaserConnectorAdvRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasPartner) return;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.endPortal(),
                (pose, buffer) -> renderCube(state, pose.pose(), buffer));
    }

    private RenderType renderType() {
        return RenderTypes.endPortal();
    }

    private void renderCube(LaserConnectorAdvRenderState state, Matrix4f matrixStack, VertexConsumer vertexConsumer) {
        Direction direction = state.facing;
        float oneSmall = 0.53125f;
        float zeroSmall = 0.46875f;
        float oneBig = 0.5625f;
        float zeroBig = 0.4375f;
        int ticks = 80;
        float f1 = state.animationTime;
        float lerp = f1 / ticks;
        float zero;
        float one;
        if (f1 < ticks / 2f) {
            zero = Mth.lerp(lerp, zeroSmall, zeroBig);
            one = Mth.lerp(lerp, oneSmall, oneBig);
        } else {
            zero = Mth.lerp(lerp, zeroBig, zeroSmall);
            one = Mth.lerp(lerp, oneBig, oneSmall);
        }
        float diff = one - zero;
        float f;
        switch (direction) {
            case UP:
                f = 0.5f + 0.25f; //Center of cube up 1/4 block
                this.renderFace(matrixStack, vertexConsumer, zero, one, f, f + diff, one, one, one, one); //South
                this.renderFace(matrixStack, vertexConsumer, zero, one, f + diff, f, zero, zero, zero, zero); //North
                this.renderFace(matrixStack, vertexConsumer, one, one, f + diff, f, zero, one, one, zero); //East
                this.renderFace(matrixStack, vertexConsumer, zero, zero, f, f + diff, zero, one, one, zero); //West
                this.renderFace(matrixStack, vertexConsumer, zero, one, f, f, zero, zero, one, one); //Down
                this.renderFace(matrixStack, vertexConsumer, zero, one, f + diff, f + diff, one, one, zero, zero); //Up
                break;
            case DOWN:
                f = 0.5f - 0.25f; //Center of cube down 1/4 block
                this.renderFace(matrixStack, vertexConsumer, zero, one, f, f - diff, one, one, one, one); //South
                this.renderFace(matrixStack, vertexConsumer, zero, one, f - diff, f, zero, zero, zero, zero); //North
                this.renderFace(matrixStack, vertexConsumer, one, one, f - diff, f, zero, one, one, zero); //East
                this.renderFace(matrixStack, vertexConsumer, zero, zero, f, f - diff, zero, one, one, zero); //West
                this.renderFace(matrixStack, vertexConsumer, zero, one, f, f, zero, zero, one, one); //Down
                this.renderFace(matrixStack, vertexConsumer, zero, one, f - diff, f - diff, one, one, zero, zero); //Up
                break;
            case NORTH:
                f = 0.5f - 0.25f; //Center of cube up 1/4 block
                this.renderFace(matrixStack, vertexConsumer, zero, one, zero, one, f, f, f, f); //South
                this.renderFace(matrixStack, vertexConsumer, zero, one, one, zero, f - diff, f - diff, f - diff, f - diff); //North
                this.renderFace(matrixStack, vertexConsumer, one, one, one, zero, f - diff, f, f, f - diff); //East
                this.renderFace(matrixStack, vertexConsumer, zero, zero, zero, one, f - diff, f, f, f - diff); //West
                this.renderFace(matrixStack, vertexConsumer, zero, one, zero, zero, f - diff, f - diff, f, f); //Down
                this.renderFace(matrixStack, vertexConsumer, zero, one, one, one, f, f, f - diff, f - diff); //Up
                break;
            case SOUTH:
                f = 0.5f + 0.25f; //Center of cube down 1/4 block
                this.renderFace(matrixStack, vertexConsumer, zero, one, zero, one, f, f, f, f); //South
                this.renderFace(matrixStack, vertexConsumer, zero, one, one, zero, f + diff, f + diff, f + diff, f + diff); //North
                this.renderFace(matrixStack, vertexConsumer, zero, zero, zero, one, f + diff, f, f, f + diff); //East
                this.renderFace(matrixStack, vertexConsumer, one, one, one, zero, f + diff, f, f, f + diff); //West
                this.renderFace(matrixStack, vertexConsumer, zero, one, one, one, f, f, f + diff, f + diff); //Down
                this.renderFace(matrixStack, vertexConsumer, zero, one, zero, zero, f + diff, f + diff, f, f); //Up
                break;
            case EAST:
                f = 0.5f + 0.25f; //Center of cube up 1/4 block
                this.renderFace(matrixStack, vertexConsumer, f, f + diff, zero, one, one, one, one, one); //South
                this.renderFace(matrixStack, vertexConsumer, f, f + diff, one, zero, zero, zero, zero, zero); //North
                this.renderFace(matrixStack, vertexConsumer, f + diff, f + diff, one, zero, zero, one, one, zero); //East
                this.renderFace(matrixStack, vertexConsumer, f, f, zero, one, zero, one, one, zero); //West
                this.renderFace(matrixStack, vertexConsumer, f, f + diff, zero, zero, zero, zero, one, one); //Down
                this.renderFace(matrixStack, vertexConsumer, f, f + diff, one, one, one, one, zero, zero); //Up
                break;
            case WEST:
                f = 0.5f - 0.25f; //Center of cube shifted 1/4 block to the west
                this.renderFace(matrixStack, vertexConsumer, f, f - diff, zero, one, one, one, one, one); //South
                this.renderFace(matrixStack, vertexConsumer, f, f - diff, one, zero, zero, zero, zero, zero); //North
                this.renderFace(matrixStack, vertexConsumer, f, f, zero, one, zero, one, one, zero); //East
                this.renderFace(matrixStack, vertexConsumer, f - diff, f - diff, one, zero, zero, one, one, zero); //West
                this.renderFace(matrixStack, vertexConsumer, f, f - diff, one, one, one, one, zero, zero); //Down
                this.renderFace(matrixStack, vertexConsumer, f, f - diff, zero, zero, zero, zero, one, one); //Up
                break;
            default:
                break;
        }
    }

    private void renderFace(Matrix4f matrixStack, VertexConsumer vertexConsumer, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4) {
        vertexConsumer.addVertex(matrixStack, x1, y1, z1);
        vertexConsumer.addVertex(matrixStack, x2, y1, z2);
        vertexConsumer.addVertex(matrixStack, x2, y2, z3);
        vertexConsumer.addVertex(matrixStack, x1, y2, z4);
    }
}
