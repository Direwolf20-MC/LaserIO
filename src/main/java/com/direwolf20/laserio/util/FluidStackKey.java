package com.direwolf20.laserio.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

public class FluidStackKey {
    public final Holder<Fluid> fluid;
    public final DataComponentPatch dataComponents;
    private final int hash;


    public FluidStackKey(FluidStack stack, boolean compareNBT) {
        this.fluid = stack.getFluidHolder();
        this.dataComponents = compareNBT ? stack.getComponentsPatch() : DataComponentPatch.EMPTY;
        ;
        this.hash = Objects.hash(fluid, dataComponents);
    }

    public FluidStack getStack() {
        return new FluidStack(fluid, 1, dataComponents);
    }

    public FluidStack getStack(int amt) {
        return new FluidStack(fluid, amt, dataComponents);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluidStackKey) {
            return (((FluidStackKey) obj).fluid == this.fluid) && Objects.equals(((FluidStackKey) obj).dataComponents, this.dataComponents);
        }
        return false;
    }
}
