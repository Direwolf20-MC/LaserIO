package com.direwolf20.laserio.client.blockentityrenders.baseberender;

import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LaserNodeBERender extends BaseLaserBERender<LaserNodeBE> {
    public LaserNodeBERender(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public void render(LaserNodeBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        super.render(blockentity, partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);
    }

}
