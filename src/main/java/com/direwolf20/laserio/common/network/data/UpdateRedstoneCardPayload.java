package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateRedstoneCardPayload(
        byte mode,
        byte channel,
        boolean strong
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "update_redstone_card");

    public UpdateRedstoneCardPayload(final FriendlyByteBuf buffer) {
        this(buffer.readByte(), buffer.readByte(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(mode());
        buffer.writeByte(channel());
        buffer.writeBoolean(strong());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
