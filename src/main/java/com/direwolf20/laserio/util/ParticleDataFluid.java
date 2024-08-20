package com.direwolf20.laserio.util;

import net.minecraftforge.fluids.FluidStack;

public class ParticleDataFluid {
    public record PositionData(DimBlockPos node, byte direction, byte position) {
    }

    public FluidStack fluidStack;
    public PositionData fromData;
    public PositionData toData;

    public ParticleDataFluid(FluidStack fluidStack, DimBlockPos fromNode, byte fromDirection, DimBlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.fluidStack = fluidStack;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }
}