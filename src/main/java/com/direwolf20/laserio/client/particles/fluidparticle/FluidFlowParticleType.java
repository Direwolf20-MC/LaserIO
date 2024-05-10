package com.direwolf20.laserio.client.particles.fluidparticle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class FluidFlowParticleType extends ParticleType<FluidFlowParticleData> {
    public FluidFlowParticleType(boolean pOverrideLimiter) {
        super(pOverrideLimiter);
    }

    public FluidFlowParticleType getType() {
        return this;
    }

    @Override
    public MapCodec<FluidFlowParticleData> codec() {
        return FluidFlowParticleData.MAP_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidFlowParticleData> streamCodec() {
        return FluidFlowParticleData.STREAM_CODEC;
    }

}
