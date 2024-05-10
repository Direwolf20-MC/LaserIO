package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChemicalFlowParticleType extends ParticleType<ChemicalFlowParticleData> {
    public ChemicalFlowParticleType(boolean pOverrideLimiter) {
        super(pOverrideLimiter);
    }

    public ChemicalFlowParticleType getType() {
        return this;
    }

    @Override
    public MapCodec<ChemicalFlowParticleData> codec() {
        return ChemicalFlowParticleData.MAP_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalFlowParticleData> streamCodec() {
        return ChemicalFlowParticleData.STREAM_CODEC;
    }
}
