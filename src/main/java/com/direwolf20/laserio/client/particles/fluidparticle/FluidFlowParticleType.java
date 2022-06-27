package com.direwolf20.laserio.client.particles.fluidparticle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class FluidFlowParticleType extends ParticleType<FluidFlowParticleData> {
    public FluidFlowParticleType() {
        super(false, FluidFlowParticleData.DESERIALIZER);
    }

    @Override
    public Codec<FluidFlowParticleData> codec() {
        return null;
    }

}
