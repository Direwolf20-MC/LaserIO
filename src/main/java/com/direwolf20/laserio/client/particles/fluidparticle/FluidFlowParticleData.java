package com.direwolf20.laserio.client.particles.fluidparticle;

import com.direwolf20.laserio.client.particles.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidFlowParticleData implements ParticleOptions {
    public static final MapCodec<FluidFlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FluidStack.CODEC.fieldOf("fluidStack").forGetter(p -> p.fluidStack),
                    Codec.DOUBLE.fieldOf("targetX").forGetter(p -> p.targetX),
                    Codec.DOUBLE.fieldOf("targetY").forGetter(p -> p.targetY),
                    Codec.DOUBLE.fieldOf("targetZ").forGetter(p -> p.targetZ),
                    Codec.INT.fieldOf("ticksPerBlock").forGetter(p -> p.ticksPerBlock)
            ).apply(instance, FluidFlowParticleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFlowParticleData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC,
            FluidFlowParticleData::getFluidStack,
            ByteBufCodecs.DOUBLE,
            FluidFlowParticleData::getTargetX,
            ByteBufCodecs.DOUBLE,
            FluidFlowParticleData::getTargetY,
            ByteBufCodecs.DOUBLE,
            FluidFlowParticleData::getTargetZ,
            ByteBufCodecs.INT,
            FluidFlowParticleData::getTicksPerBlock,
            FluidFlowParticleData::new
    );

    private final FluidStack fluidStack;
    public final double targetX;
    public final double targetY;
    public final double targetZ;
    public final int ticksPerBlock;

    public FluidFlowParticleData(FluidStack fluidStack, double tx, double ty, double tz, int ticks) {
        this.fluidStack = fluidStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        ticksPerBlock = ticks;
    }

    @Nonnull
    @Override
    public ParticleType<FluidFlowParticleData> getType() {
        return ModParticles.FLUIDFLOWPARTICLE.get();
    }

    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public int getTicksPerBlock() {
        return ticksPerBlock;
    }
}
