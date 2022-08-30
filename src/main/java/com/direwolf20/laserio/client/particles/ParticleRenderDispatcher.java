package com.direwolf20.laserio.client.particles;

import com.direwolf20.laserio.client.particles.fluidparticle.FluidFlowParticle;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticle;
import com.direwolf20.laserio.common.LaserIO;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent evt) {
        evt.register(ModParticles.ITEMFLOWPARTICLE.get(), ItemFlowParticle.FACTORY);
        evt.register(ModParticles.FLUIDFLOWPARTICLE.get(), FluidFlowParticle.FACTORY);
    }
}
