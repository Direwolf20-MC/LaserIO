package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class ParticleRenderDataFluid {
    public FluidStack fluidStack;
    public BlockPos fromPos;
    public byte direction;
    public BlockPos toPos;
    public byte position;

    public ParticleRenderDataFluid(FluidStack fluidStack, BlockPos fromPos, byte direction, BlockPos toPos, byte position) {
        this.fluidStack = fluidStack;
        this.fromPos = fromPos;
        this.direction = direction;
        this.toPos = toPos;
        this.position = position;
    }

}
