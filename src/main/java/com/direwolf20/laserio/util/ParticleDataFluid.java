package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class ParticleDataFluid {
    public record PositionData(BlockPos node, byte direction, byte position) {
    }

    public FluidStack fluidStack;
    public PositionData fromData;
    public PositionData toData;

    public ParticleDataFluid(FluidStack fluidStack, BlockPos fromNode, byte fromDirection, BlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.fluidStack = fluidStack;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }

}
