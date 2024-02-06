package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateFilterPayload(
        boolean allowList,
        boolean compareNBT
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "update_filter");

    public UpdateFilterPayload(final FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(allowList());
        buffer.writeBoolean(compareNBT());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
