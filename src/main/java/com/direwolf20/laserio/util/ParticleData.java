package com.direwolf20.laserio.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ParticleData {
    public static final Codec<ParticleData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("item").forGetter(ParticleData::getItem),
                            Codec.BYTE.fieldOf("itemCount").forGetter(ParticleData::getItemCount),
                            SharedRecords.PositionData.CODEC.fieldOf("fromData").forGetter(ParticleData::getFromData),
                            SharedRecords.PositionData.CODEC.fieldOf("toData").forGetter(ParticleData::getToData)
                    )
                    .apply(instance, ParticleData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ParticleData::getItem,
            ByteBufCodecs.BYTE,
            ParticleData::getItemCount,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleData::getFromData,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleData::getToData,
            ParticleData::new
    );

    public int item;
    public byte itemCount;
    public SharedRecords.PositionData fromData;
    public SharedRecords.PositionData toData;

    public ParticleData(int item, byte itemCount, GlobalPos fromNode, byte fromDirection, GlobalPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromData = new SharedRecords.PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new SharedRecords.PositionData(toNode, toDirection, insertPosition);
    }

    public ParticleData(int item, byte itemCount, SharedRecords.PositionData fromData, SharedRecords.PositionData toData) {
        this.item = item;
        this.itemCount = itemCount;
        this.fromData = fromData;
        this.toData = toData;
    }

    public int getItem() {
        return item;
    }

    public byte getItemCount() {
        return itemCount;
    }

    public SharedRecords.PositionData getFromData() {
        return fromData;
    }

    public SharedRecords.PositionData getToData() {
        return toData;
    }

}
