package com.direwolf20.laserio.client.color;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;

public final class LaserBlockTintSource implements BlockTintSource {
    public static final LaserBlockTintSource INSTANCE = new LaserBlockTintSource();

    private LaserBlockTintSource() {
    }

    @Override
    public int color(BlockState state) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BaseLaserBE laserBE) {
            Color color = laserBE.getColor();
            return ARGB.color(255, color.getRed(), color.getGreen(), color.getBlue());
        }
        return -1;
    }
}
