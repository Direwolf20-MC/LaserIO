package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.blockentityrenders.baseberender.BaseLaserBERender;
import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class LaserConnectorAdvBERender extends BaseLaserBERender<LaserConnectorAdvBE> {
    public LaserConnectorAdvBERender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LaserConnectorAdvBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        long gameTime = blockentity.getLevel().getGameTime();
        if (blockentity.getPartnerDimBlockPos() != null) {
            Matrix4f matrix4f = matrixStackIn.last().pose();
            this.renderCube(blockentity, matrix4f, bufferIn.getBuffer(this.renderType()), gameTime, partialTicks);
            DelayedRenderer.add(blockentity);
        }
        else
            super.render(blockentity, partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);
    }

    private void renderCube(LaserConnectorAdvBE blockEntity, Matrix4f matrixStack, VertexConsumer vertexConsumer, long gameTime, float partialTicks) {
        float f = this.getOffsetUp();
        float oneSmall = 0.53125f;
        float zeroSmall = 0.46875f;
        float oneBig = 0.5625f;
        float zeroBig = 0.4375f;
        int ticks = 80;
        float f1 = (float)Math.floorMod(gameTime, ticks) + partialTicks;
        float lerp = f1/ticks;
        float zero;
        float one;
        if (f1 < ticks/2f) {
            zero = Mth.lerp(lerp, zeroSmall, zeroBig);
            one = Mth.lerp(lerp, oneSmall, oneBig);
        } else {
            zero = Mth.lerp(lerp, zeroBig, zeroSmall);
            one = Mth.lerp(lerp, oneBig, oneSmall);
        }
        float diff = one-zero;
        this.renderFace(blockEntity, matrixStack, vertexConsumer, zero, one, f, f+diff, one, one, one, one, Direction.SOUTH);
        this.renderFace(blockEntity, matrixStack, vertexConsumer, zero, one, f+diff, f, zero, zero, zero, zero, Direction.NORTH);
        this.renderFace(blockEntity, matrixStack, vertexConsumer, one, one, f+diff, f, zero, one, one, zero, Direction.EAST);
        this.renderFace(blockEntity, matrixStack, vertexConsumer, zero, zero, f, f+diff, zero, one, one, zero, Direction.WEST);
        this.renderFace(blockEntity, matrixStack, vertexConsumer, zero, one, f, f, zero, zero, one, one, Direction.DOWN);
        this.renderFace(blockEntity, matrixStack, vertexConsumer, zero, one, f+diff, f+diff, one, one, zero, zero, Direction.UP);
    }

    private void renderFace(LaserConnectorAdvBE blockEntity, Matrix4f matrixStack, VertexConsumer vertexConsumer, float p_254147_, float p_253639_, float p_254107_, float p_254109_, float p_254021_, float p_254458_, float p_254086_, float p_254310_, Direction direction) {
        vertexConsumer.vertex(matrixStack, p_254147_, p_254107_, p_254021_).endVertex();
        vertexConsumer.vertex(matrixStack, p_253639_, p_254107_, p_254458_).endVertex();
        vertexConsumer.vertex(matrixStack, p_253639_, p_254109_, p_254086_).endVertex();
        vertexConsumer.vertex(matrixStack, p_254147_, p_254109_, p_254310_).endVertex();
    }

    protected float getOffsetUp() {
        return 0.75F;
    }

    protected RenderType renderType() {
        return RenderType.endPortal();
    }

}
