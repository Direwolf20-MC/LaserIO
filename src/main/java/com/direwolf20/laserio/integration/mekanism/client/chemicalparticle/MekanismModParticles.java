package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MekanismModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES_MEKANISM = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, LaserIO.MODID);
    public static final RegistryObject<ParticleType<ChemicalFlowParticleData>> CHEMICALFLOWPARTICLE = PARTICLE_TYPES_MEKANISM.register("chemicalflowparticle", ChemicalFlowParticleType::new);
}