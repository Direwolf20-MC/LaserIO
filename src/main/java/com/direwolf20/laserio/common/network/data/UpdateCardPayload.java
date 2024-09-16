package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.datafixers.util.Function15;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record UpdateCardPayload(
        byte mode,
        byte channel,
        int extractAmt,
        short priority,
        byte sneaky,
        short ticks,
        boolean exact,
        boolean regulate,
        byte roundRobin,
        int extractLimit,
        int insertLimit,
        byte redstoneMode,
        byte redstoneChannel,
        boolean andMode,
        byte maxBackoff
) implements CustomPacketPayload {
    public static final Type<UpdateCardPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "update_card"));

    @Override
    public Type<UpdateCardPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, UpdateCardPayload> STREAM_CODEC = composite(
            ByteBufCodecs.BYTE, UpdateCardPayload::mode,
            ByteBufCodecs.BYTE, UpdateCardPayload::channel,
            ByteBufCodecs.INT, UpdateCardPayload::extractAmt,
            ByteBufCodecs.SHORT, UpdateCardPayload::priority,
            ByteBufCodecs.BYTE, UpdateCardPayload::sneaky,
            ByteBufCodecs.SHORT, UpdateCardPayload::ticks,
            ByteBufCodecs.BOOL, UpdateCardPayload::exact,
            ByteBufCodecs.BOOL, UpdateCardPayload::regulate,
            ByteBufCodecs.BYTE, UpdateCardPayload::roundRobin,
            ByteBufCodecs.INT, UpdateCardPayload::extractLimit,
            ByteBufCodecs.INT, UpdateCardPayload::insertLimit,
            ByteBufCodecs.BYTE, UpdateCardPayload::redstoneMode,
            ByteBufCodecs.BYTE, UpdateCardPayload::redstoneChannel,
            ByteBufCodecs.BOOL, UpdateCardPayload::andMode,
            ByteBufCodecs.BYTE, UpdateCardPayload::maxBackoff,
            UpdateCardPayload::new
    );

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11,
            final Function<C, T11> getter11,
            final StreamCodec<? super B, T12> codec12,
            final Function<C, T12> getter12,
            final StreamCodec<? super B, T13> codec13,
            final Function<C, T13> getter13,
            final StreamCodec<? super B, T14> codec14,
            final Function<C, T14> getter14,
            final StreamCodec<? super B, T15> codec15,
            final Function<C, T15> getter15,
            final Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> p_331335_) {
        return new StreamCodec<>() {
            @Override
            public C decode(B p_330310_) {
                T1 t1 = codec1.decode(p_330310_);
                T2 t2 = codec2.decode(p_330310_);
                T3 t3 = codec3.decode(p_330310_);
                T4 t4 = codec4.decode(p_330310_);
                T5 t5 = codec5.decode(p_330310_);
                T6 t6 = codec6.decode(p_330310_);
                T7 t7 = codec7.decode(p_330310_);
                T8 t8 = codec8.decode(p_330310_);
                T9 t9 = codec9.decode(p_330310_);
                T10 t10 = codec10.decode(p_330310_);
                T11 t11 = codec11.decode(p_330310_);
                T12 t12 = codec12.decode(p_330310_);
                T13 t13 = codec13.decode(p_330310_);
                T14 t14 = codec14.decode(p_330310_);
                T15 t15 = codec15.decode(p_330310_);
                return p_331335_.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
            }

            @Override
            public void encode(B p_332052_, C p_331912_) {
                codec1.encode(p_332052_, getter1.apply(p_331912_));
                codec2.encode(p_332052_, getter2.apply(p_331912_));
                codec3.encode(p_332052_, getter3.apply(p_331912_));
                codec4.encode(p_332052_, getter4.apply(p_331912_));
                codec5.encode(p_332052_, getter5.apply(p_331912_));
                codec6.encode(p_332052_, getter6.apply(p_331912_));
                codec7.encode(p_332052_, getter7.apply(p_331912_));
                codec8.encode(p_332052_, getter8.apply(p_331912_));
                codec9.encode(p_332052_, getter9.apply(p_331912_));
                codec10.encode(p_332052_, getter10.apply(p_331912_));
                codec11.encode(p_332052_, getter11.apply(p_331912_));
                codec12.encode(p_332052_, getter12.apply(p_331912_));
                codec13.encode(p_332052_, getter13.apply(p_331912_));
                codec14.encode(p_332052_, getter14.apply(p_331912_));
                codec15.encode(p_332052_, getter15.apply(p_331912_));
            }
        };
    }
}
