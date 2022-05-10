package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;

public class ParticleRenderData {
    public int item;
    public byte itemCount;
    public BlockPos fromPos;
    public byte direction;
    public BlockPos toPos;
    public byte position;

    public ParticleRenderData(int item, byte itemCount, BlockPos fromPos, byte direction, BlockPos toPos, byte position) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromPos = fromPos;
        this.direction = direction;
        this.toPos = toPos;
        this.position = position;
    }

}
