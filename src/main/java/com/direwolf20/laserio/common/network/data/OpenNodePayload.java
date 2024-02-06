package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenNodePayload(
        BlockPos sourcePos,
        byte side
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "open_node");

    public OpenNodePayload(final FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(sourcePos());
        buffer.writeByte(side());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
