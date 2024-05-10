package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.util.SharedRecords;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ParticleDataChemical {
    public static final Codec<ParticleDataChemical> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BoxedChemicalStack.CODEC.fieldOf("chemicalStack").forGetter(ParticleDataChemical::getChemicalStack),
                            SharedRecords.PositionData.CODEC.fieldOf("fromData").forGetter(ParticleDataChemical::getFromData),
                            SharedRecords.PositionData.CODEC.fieldOf("toData").forGetter(ParticleDataChemical::getToData)
                    )
                    .apply(instance, ParticleDataChemical::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleDataChemical> STREAM_CODEC = StreamCodec.composite(
            BoxedChemicalStack.STREAM_CODEC,
            ParticleDataChemical::getChemicalStack,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleDataChemical::getFromData,
            SharedRecords.PositionData.STREAM_CODEC,
            ParticleDataChemical::getToData,
            ParticleDataChemical::new
    );

    public final BoxedChemicalStack chemicalStack;
    public final SharedRecords.PositionData fromData;
    public final SharedRecords.PositionData toData;

    public ParticleDataChemical(BoxedChemicalStack boxedStack, GlobalPos fromNode, byte fromDirection, GlobalPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this(boxedStack, new SharedRecords.PositionData(fromNode, fromDirection, extractPosition), new SharedRecords.PositionData(toNode, toDirection, insertPosition));
    }

    private ParticleDataChemical(BoxedChemicalStack boxedStack, SharedRecords.PositionData fromData, SharedRecords.PositionData toData) {
        this.chemicalStack = boxedStack;
        this.fromData = fromData;
        this.toData = toData;
    }

    public BoxedChemicalStack getChemicalStack() {
        return chemicalStack;
    }

    public SharedRecords.PositionData getFromData() {
        return fromData;
    }

    public SharedRecords.PositionData getToData() {
        return toData;
    }
}
