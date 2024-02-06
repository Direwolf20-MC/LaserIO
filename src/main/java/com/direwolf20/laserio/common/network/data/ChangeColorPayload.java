package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeColorPayload(
        BlockPos sourcePos,
        int color,
        int wrenchAlpha
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "change_color");

    public ChangeColorPayload(final FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(sourcePos());
        buffer.writeInt(color());
        buffer.writeInt(wrenchAlpha());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
