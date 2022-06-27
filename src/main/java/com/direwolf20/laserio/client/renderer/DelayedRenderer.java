package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DelayedRenderer {
    private static final Queue<BaseLaserBE> beRenders = new LinkedList<>();
    private static final Set<LaserNodeBE> beConnectingRenders = new HashSet<>();

    public static void render(PoseStack matrixStackIn) {
        if (beRenders.size() > 0) {
            RenderUtils.drawLasersLast2(beRenders, matrixStackIn);
            /*BaseLaserBE blockentity = beRenders.remove();
            Set<BlockPos> renderedConnections = blockentity.getRenderedConnections();
            for (BlockPos target : renderedConnections)
                RenderUtils.drawLasersLast(blockentity, blockentity.getBlockPos(), blockentity.getWorldPos(target), matrixStackIn);*/
        }
    }

    public static void renderConnections(PoseStack matrixStackIn) {
        if (beConnectingRenders.isEmpty()) return;
        RenderUtils.drawConnectingLasersLast4(beConnectingRenders, matrixStackIn);
        /*for (LaserNodeBE blockentity : beConnectingRenders) {
            //LaserNodeBE blockentity = beConnectingRenders.remove();
            RenderUtils.drawConnectingLasersLast2(blockentity, matrixStackIn);
        }*/
        beConnectingRenders.clear();
    }

    public static void add(BaseLaserBE be) {
        beRenders.add(be);
    }

    public static void addConnecting(LaserNodeBE be) {
        beConnectingRenders.add(be);
    }
}
