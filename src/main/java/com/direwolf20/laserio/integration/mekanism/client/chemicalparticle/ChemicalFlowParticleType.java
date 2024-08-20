package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

public class ChemicalFlowParticleType extends ParticleType<ChemicalFlowParticleData> {

    public ChemicalFlowParticleType() {
        super(false, ChemicalFlowParticleData.DESERIALIZER);
    }

    @Override
    public Codec<ChemicalFlowParticleData> codec() {
        return null;
    }

}