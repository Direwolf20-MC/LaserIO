package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

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
        boolean andMode
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "update_card");

    public UpdateCardPayload(final FriendlyByteBuf buffer) {
        this(buffer.readByte(), buffer.readByte(), buffer.readInt(), buffer.readShort(), buffer.readByte(), buffer.readShort(), buffer.readBoolean(), buffer.readBoolean(), buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readByte(), buffer.readByte(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(mode());
        buffer.writeByte(channel());
        buffer.writeInt(extractAmt());
        buffer.writeShort(priority());
        buffer.writeByte(sneaky());
        buffer.writeShort(ticks());
        buffer.writeBoolean(exact());
        buffer.writeBoolean(regulate());
        buffer.writeByte(roundRobin());
        buffer.writeInt(extractLimit());
        buffer.writeInt(insertLimit());
        buffer.writeByte(redstoneMode());
        buffer.writeByte(redstoneChannel());
        buffer.writeBoolean(andMode());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
