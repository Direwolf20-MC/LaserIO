package com.direwolf20.laserio.client.particles;

import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleType;
import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(LaserIO.MODID)
public class ModParticles {
    @ObjectHolder("itemflowparticle")
    public static ParticleType<ItemFlowParticleData> ITEMFLOWPARTICLE;

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt) {
        evt.getRegistry().register(
                new ItemFlowParticleType().setRegistryName("itemflowparticle")
        );
    }
}