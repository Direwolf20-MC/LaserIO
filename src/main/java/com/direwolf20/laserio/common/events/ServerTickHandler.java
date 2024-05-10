package com.direwolf20.laserio.common.events;

import com.direwolf20.laserio.common.network.data.NodeParticlesChemicalPayload;
import com.direwolf20.laserio.common.network.data.NodeParticlesFluidPayload;
import com.direwolf20.laserio.common.network.data.NodeParticlesPayload;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.ParticleData;
import com.direwolf20.laserio.util.ParticleDataFluid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerTickHandler {
    private static List<ParticleData> particleList = new ArrayList<>();
    private static List<ParticleDataFluid> particleListFluid = new ArrayList<>();
    private static List<ParticleDataChemical> particleListChemical = new ArrayList<>();

    @SubscribeEvent
    public static void handleTickEndEvent(ServerTickEvent.Post event) {
        if (!particleList.isEmpty()) {
            Set<Level> levels = new HashSet<>();
            for (ParticleData data : particleList) {
                levels.add(MiscTools.getLevel(event.getServer(), data.fromData.node()));
                levels.add(MiscTools.getLevel(event.getServer(), data.toData.node()));
            }
            for (Level level : levels)
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new NodeParticlesPayload(List.copyOf(particleList)));
            particleList.clear();
        }
        if (!particleListFluid.isEmpty()) {
            Set<Level> levels = new HashSet<>();
            for (ParticleDataFluid data : particleListFluid) {
                levels.add(MiscTools.getLevel(event.getServer(), data.fromData.node()));
                levels.add(MiscTools.getLevel(event.getServer(), data.toData.node()));
            }
            for (Level level : levels)
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new NodeParticlesFluidPayload(List.copyOf(particleListFluid)));
            particleListFluid.clear();
        }
        if (!particleListChemical.isEmpty()) {
            Set<Level> levels = new HashSet<>();
            for (ParticleDataChemical data : particleListChemical) {
                levels.add(event.getServer().getLevel(data.fromData.node().dimension()));
                levels.add(event.getServer().getLevel(data.toData.node().dimension()));
            }
            for (Level level : levels)
                PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new NodeParticlesChemicalPayload(List.copyOf(particleListChemical)));
            particleListChemical.clear();
        }

    }

    public static void addToList(ParticleData particleData) {
        particleList.add(particleData);
    }

    public static void addToListFluid(ParticleDataFluid particleData) {
        particleListFluid.add(particleData);
    }

    public static void addToListFluid(ParticleDataChemical particleData) {
        particleListChemical.add(particleData);
    }
}
