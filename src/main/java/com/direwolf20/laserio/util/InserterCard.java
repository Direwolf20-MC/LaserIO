package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class InserterCard {
    public final BlockPos relativePos;
    public final Direction direction;
    public final byte channel;

    public InserterCard(BlockPos relativePos, Direction direction, byte channel) {
        this.relativePos = relativePos;
        this.direction = direction;
        this.channel = channel;
    }
}
