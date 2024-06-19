package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CopyPasteCardPayload(
        int slot,
        boolean copy
) implements CustomPacketPayload {
    public static final Type<CopyPasteCardPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "copy_paste_card"));

    @Override
    public Type<CopyPasteCardPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, CopyPasteCardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CopyPasteCardPayload::slot,
            ByteBufCodecs.BOOL, CopyPasteCardPayload::copy,
            CopyPasteCardPayload::new
    );
}
