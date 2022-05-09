package com.direwolf20.laserio.common.events;

import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketNodeParticles;
import com.direwolf20.laserio.util.ParticleData;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerTickHandler {
    private static List<ParticleData> particleList = new ArrayList<>();
    private static Level serverWorld;

    @SubscribeEvent
    public static void handleTickEndEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!particleList.isEmpty()) {
                PacketHandler.sendToAll(new PacketNodeParticles(particleList), serverWorld);
                particleList.clear();
            }
        }
    }

    public static void addToList(ParticleData particleData, Level world) {
        particleList.add(particleData);
        serverWorld = world;
    }
}
