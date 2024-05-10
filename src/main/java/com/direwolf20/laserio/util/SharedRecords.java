package com.direwolf20.laserio.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SharedRecords {
    public record PositionData(GlobalPos node, byte direction, byte position) {
        public static final Codec<PositionData> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                GlobalPos.CODEC.fieldOf("node").forGetter(PositionData::node),
                                Codec.BYTE.fieldOf("direction").forGetter(PositionData::direction),
                                Codec.BYTE.fieldOf("position").forGetter(PositionData::position)
                        )
                        .apply(instance, PositionData::new)
        );
        public static final StreamCodec<FriendlyByteBuf, PositionData> STREAM_CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC,
                PositionData::node,
                ByteBufCodecs.BYTE,
                PositionData::direction,
                ByteBufCodecs.BYTE,
                PositionData::position,
                PositionData::new
        );
    }
}
