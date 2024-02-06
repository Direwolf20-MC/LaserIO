package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenCardPayload(
        int slotNumber,
        BlockPos sourcePos,
        boolean hasShiftDown
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "open_card");

    public OpenCardPayload(final FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readBlockPos(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(slotNumber());
        buffer.writeBlockPos(sourcePos());
        buffer.writeBoolean(hasShiftDown());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
