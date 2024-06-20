package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.util.ParticleDataFluid;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record NodeParticlesFluidPayload(
        List<ParticleDataFluid> particleList
) implements CustomPacketPayload {
    public static final Type<NodeParticlesFluidPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "node_particles_fluid"));

    @Override
    public Type<NodeParticlesFluidPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeParticlesFluidPayload> STREAM_CODEC = StreamCodec.composite(
            ParticleDataFluid.STREAM_CODEC.apply(ByteBufCodecs.list()), NodeParticlesFluidPayload::particleList,
            NodeParticlesFluidPayload::new
    );
}
