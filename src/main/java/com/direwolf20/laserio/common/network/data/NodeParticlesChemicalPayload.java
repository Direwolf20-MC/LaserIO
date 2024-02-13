package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record NodeParticlesChemicalPayload(
        List<ParticleDataChemical> particleList
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "node_particles_chemical");

    public NodeParticlesChemicalPayload(final FriendlyByteBuf buffer) {
        this(buffer.readList(ParticleDataChemical::new));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeObjectCollection(particleList, ParticleDataChemical::write);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
