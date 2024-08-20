package com.direwolf20.laserio.client.particles;

import com.direwolf20.laserio.client.particles.fluidparticle.FluidFlowParticle;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticle;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.integration.mekanism.MekanismIntegration;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ChemicalFlowParticle;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.MekanismModParticles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent evt) {
        evt.registerSpecial(ModParticles.ITEMFLOWPARTICLE.get(), ItemFlowParticle.FACTORY);
        evt.registerSpecial(ModParticles.FLUIDFLOWPARTICLE.get(), FluidFlowParticle.FACTORY);
        if (MekanismIntegration.isLoaded()) {
            evt.registerSpecial(MekanismModParticles.CHEMICALFLOWPARTICLE.get(), ChemicalFlowParticle.FACTORY);
        }
    }

}