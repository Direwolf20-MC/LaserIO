package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenFilterPayload(
        int slotNumber
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "open_filter");

    public OpenFilterPayload(final FriendlyByteBuf buffer) {
        this(buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(slotNumber());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
