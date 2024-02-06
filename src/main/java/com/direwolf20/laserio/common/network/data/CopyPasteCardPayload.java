package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CopyPasteCardPayload(
        int slot,
        boolean copy
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "copy_paste_card");

    public CopyPasteCardPayload(final FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(slot());
        buffer.writeBoolean(copy());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
