package com.direwolf20.laserio.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class FluidStackKey {
    public final Fluid fluid;
    public final CompoundTag nbt;
    private final int hash;


    public FluidStackKey(FluidStack stack, boolean compareNBT) {
        this.fluid = stack.getFluid();
        this.nbt = compareNBT ? stack.getTag() : new CompoundTag();
        this.hash = Objects.hash(fluid, nbt);
    }

    public FluidStack getStack() {
        return new FluidStack(fluid, 1, nbt);
    }

    public FluidStack getStack(int amt) {
        return new FluidStack(fluid, amt, nbt);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluidStackKey) {
            return (((FluidStackKey) obj).fluid == this.fluid) && Objects.equals(((FluidStackKey) obj).nbt, this.nbt);
        }
        return false;
    }
}
