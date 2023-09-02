package com.direwolf20.laserio.common.events;

import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketNodeParticles;
import com.direwolf20.laserio.common.network.packets.PacketNodeParticlesFluid;
import com.direwolf20.laserio.util.ParticleData;
import com.direwolf20.laserio.util.ParticleDataFluid;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerTickHandler {
    private static List<ParticleData> particleList = new ArrayList<>();
    private static List<ParticleDataFluid> particleListFluid = new ArrayList<>();

    @SubscribeEvent
    public static void handleTickEndEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!particleList.isEmpty()) {
                Set<Level> levels = new HashSet<>();
                for (ParticleData data : particleList) {
                    levels.add(data.fromData.node().getLevel(event.getServer()));
                    levels.add(data.toData.node().getLevel(event.getServer()));
                }
                for (Level level : levels)
                    PacketHandler.sendToAll(new PacketNodeParticles(particleList), level);
                particleList.clear();
            }
            if (!particleListFluid.isEmpty()) {
                Set<Level> levels = new HashSet<>();
                for (ParticleDataFluid data : particleListFluid) {
                    levels.add(data.fromData.node().getLevel(event.getServer()));
                    levels.add(data.toData.node().getLevel(event.getServer()));
                }
                for (Level level : levels)
                    PacketHandler.sendToAll(new PacketNodeParticlesFluid(particleListFluid), level);
                particleListFluid.clear();
            }
        }
    }

    public static void addToList(ParticleData particleData) {
        particleList.add(particleData);
    }

    public static void addToListFluid(ParticleDataFluid particleData) {
        particleListFluid.add(particleData);
    }
}
