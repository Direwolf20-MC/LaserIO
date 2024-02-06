package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record UpdateFilterTagPayload(
        boolean allowList,
        List<String> tags
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "update_filter_tag");

    public UpdateFilterTagPayload(final FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readList(FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(allowList());
        buffer.writeCollection(tags(), FriendlyByteBuf::writeUtf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
