package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;

public class ParticleData {
    public record PositionData(BlockPos node, byte direction, byte position) {
    }

    public int item;
    public byte itemCount;
    public PositionData fromData;
    public PositionData toData;

    public ParticleData(int item, byte itemCount, BlockPos node, byte direction, byte slotPosition, boolean extract) {
        this.item = item;
        this.itemCount = itemCount;
        if (extract)
            this.fromData = new PositionData(node, direction, slotPosition);
        else
            this.toData = new PositionData(node, direction, slotPosition);
    }

    public ParticleData(int item, byte itemCount, BlockPos fromNode, byte fromDirection, BlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }

}
