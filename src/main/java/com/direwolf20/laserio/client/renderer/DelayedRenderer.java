package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DelayedRenderer {
    private static final Queue<BaseLaserBE> beRenders = new LinkedList<>();

    public static void render(PoseStack matrixStackIn) {
        while (beRenders.size() > 0) {
            BaseLaserBE blockentity = beRenders.remove();
            Set<BlockPos> renderedConnections = blockentity.getRenderedConnections();
            for (BlockPos target : renderedConnections)
                RenderUtils.drawLasersLast(blockentity, blockentity.getBlockPos(), blockentity.getWorldPos(target), matrixStackIn);
        }
    }

    public static void add(BaseLaserBE be) {
        beRenders.add(be);
    }
}
