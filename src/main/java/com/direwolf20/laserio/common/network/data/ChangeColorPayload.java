package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeColorPayload(
        BlockPos sourcePos,
        int color,
        int wrenchAlpha
) implements CustomPacketPayload {
    public static final Type<ChangeColorPayload> TYPE = new Type<>(new ResourceLocation(LaserIO.MODID, "change_color"));

    @Override
    public Type<ChangeColorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, ChangeColorPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ChangeColorPayload::sourcePos,
            ByteBufCodecs.INT, ChangeColorPayload::color,
            ByteBufCodecs.INT, ChangeColorPayload::wrenchAlpha,
            ChangeColorPayload::new
    );
}
