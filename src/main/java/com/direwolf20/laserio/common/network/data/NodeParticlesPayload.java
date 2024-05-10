package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.util.ParticleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record NodeParticlesPayload(
        List<ParticleData> particleList
) implements CustomPacketPayload {
    public static final Type<NodeParticlesPayload> TYPE = new Type<>(new ResourceLocation(LaserIO.MODID, "node_particles_item"));

    @Override
    public Type<NodeParticlesPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeParticlesPayload> STREAM_CODEC = StreamCodec.composite(
            ParticleData.STREAM_CODEC.apply(ByteBufCodecs.list()), NodeParticlesPayload::particleList,
            NodeParticlesPayload::new
    );
}
