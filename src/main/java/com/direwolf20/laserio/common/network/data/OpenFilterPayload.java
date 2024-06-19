package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenFilterPayload(
        int slotNumber
) implements CustomPacketPayload {
    public static final Type<OpenFilterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "open_filter"));

    @Override
    public Type<OpenFilterPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, OpenFilterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OpenFilterPayload::slotNumber,
            OpenFilterPayload::new
    );
}
