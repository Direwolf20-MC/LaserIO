package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES_MEKANISM = DeferredRegister.create(Registries.PARTICLE_TYPE, LaserIO.MODID);
    public static final DeferredHolder<ParticleType<?>, ChemicalFlowParticleType> CHEMICALFLOWPARTICLE = PARTICLE_TYPES_MEKANISM.register("chemicalflowparticle", () -> new ChemicalFlowParticleType(false));
}
