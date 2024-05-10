package com.direwolf20.laserio.client.particles;

import com.direwolf20.laserio.client.particles.fluidparticle.FluidFlowParticleType;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleType;
import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, LaserIO.MODID);
    public static final DeferredHolder<ParticleType<?>, ItemFlowParticleType> ITEMFLOWPARTICLE = PARTICLE_TYPES.register("itemflowparticle", () -> new ItemFlowParticleType(false));
    public static final DeferredHolder<ParticleType<?>, FluidFlowParticleType> FLUIDFLOWPARTICLE = PARTICLE_TYPES.register("fluidflowparticle", () -> new FluidFlowParticleType(false));
}