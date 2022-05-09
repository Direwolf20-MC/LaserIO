package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;

public class ParticleData {
    public int item;
    public byte itemCount;
    public BlockPos fromNode;
    public byte fromDirection;
    public BlockPos toNode;
    public byte toDirection;
    public byte extractPosition;
    public byte insertPosition;

    public ParticleData(int item, byte itemCount, BlockPos fromNode, byte fromDirection, BlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromNode = fromNode;
        this.fromDirection = fromDirection;
        this.toNode = toNode;
        this.toDirection = toDirection;
        this.extractPosition = extractPosition;
        this.insertPosition = insertPosition;
    }

}
