package com.direwolf20.laserio.util;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

public class ParticleDataFluid {
    public static final Codec<ParticleDataFluid> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            FluidStack.CODEC.fieldOf("fluidStack").forGetter(ParticleDataFluid::getFluidStack),
                            SharedRecords.PositionData.CODEC.fieldOf("fromData").forGetter(ParticleDataFluid::getFromData),
                            SharedRecords.PositionData.CODEC.fieldOf("toData").forGetter(ParticleDataFluid::getToData)
                    )
                    .apply(instance, ParticleDataFluid::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleDataFluid> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC,
            ParticleDataFluid::getFluidStack,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleDataFluid::getFromData,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleDataFluid::getToData,
            ParticleDataFluid::new
    );

    public FluidStack fluidStack;
    public SharedRecords.PositionData fromData;
    public SharedRecords.PositionData toData;

    public ParticleDataFluid(FluidStack fluidStack, GlobalPos fromNode, byte fromDirection, GlobalPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.fluidStack = fluidStack;
        this.fromData = new SharedRecords.PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new SharedRecords.PositionData(toNode, toDirection, insertPosition);
    }

    public ParticleDataFluid(FluidStack fluidStack, SharedRecords.PositionData fromData, SharedRecords.PositionData toData) {
        this.fluidStack = fluidStack;
        this.fromData = fromData;
        this.toData = toData;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public SharedRecords.PositionData getFromData() {
        return fromData;
    }

    public SharedRecords.PositionData getToData() {
        return toData;
    }
}
