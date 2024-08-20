package com.direwolf20.laserio.util;

public class ParticleData {

    public record PositionData(DimBlockPos node, byte direction, byte position) {
    }

    public int item;
    public byte itemCount;
    public PositionData fromData;
    public PositionData toData;

    public ParticleData(int item, byte itemCount, DimBlockPos fromNode, byte fromDirection, DimBlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }

}