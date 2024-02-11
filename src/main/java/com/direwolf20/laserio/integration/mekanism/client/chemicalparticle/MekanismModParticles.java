package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MekanismModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES_MEKANISM = DeferredRegister.create(Registries.PARTICLE_TYPE, LaserIO.MODID);

    public static final Supplier<ParticleType<ChemicalFlowParticleData>> CHEMICALFLOWPARTICLE = PARTICLE_TYPES_MEKANISM.register("chemicalflowparticle", ChemicalFlowParticleType::new);
}
