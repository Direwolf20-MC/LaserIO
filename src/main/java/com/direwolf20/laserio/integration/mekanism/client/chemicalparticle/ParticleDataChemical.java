package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import mekanism.api.chemical.merged.BoxedChemicalStack;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;

public class ParticleDataChemical {
    public record PositionData(GlobalPos node, byte direction, byte position) {

        public PositionData(FriendlyByteBuf buffer) {
            this(buffer.readGlobalPos(), buffer.readByte(), buffer.readByte());
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeGlobalPos(node);
            buffer.writeByte(direction);
            buffer.writeByte(position);
        }
    }

    public final BoxedChemicalStack chemicalStack;
    public final PositionData fromData;
    public final PositionData toData;

    public ParticleDataChemical(FriendlyByteBuf buffer) {
        this(BoxedChemicalStack.read(buffer), new PositionData(buffer), new PositionData(buffer));
    }

    public ParticleDataChemical(BoxedChemicalStack boxedStack, GlobalPos fromNode, byte fromDirection, GlobalPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this(boxedStack, new PositionData(fromNode, fromDirection, extractPosition), new PositionData(toNode, toDirection, insertPosition));
    }

    private ParticleDataChemical(BoxedChemicalStack boxedStack, PositionData fromData, PositionData toData) {
        this.chemicalStack = boxedStack;
        this.fromData = fromData;
        this.toData = toData;
    }

    public void write(FriendlyByteBuf buffer) {
        chemicalStack.write(buffer);
        fromData.write(buffer);
        toData.write(buffer);
    }
}
