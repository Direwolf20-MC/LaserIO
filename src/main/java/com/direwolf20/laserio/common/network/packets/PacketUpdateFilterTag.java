package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.FilterTagContainer;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketUpdateFilterTag {
    boolean allowList;
    List<String> tags;

    public PacketUpdateFilterTag(boolean allowList, List<String> tags) {
        this.allowList = allowList;
        this.tags = tags;
    }

    public static void encode(PacketUpdateFilterTag msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.allowList);
        buffer.writeCollection(msg.tags, FriendlyByteBuf::writeUtf);
    }

    public static PacketUpdateFilterTag decode(FriendlyByteBuf buffer) {
        return new PacketUpdateFilterTag(buffer.readBoolean(), buffer.readCollection(Lists::newArrayListWithCapacity, FriendlyByteBuf::readUtf));
    }

    public static class Handler {
        public static void handle(PacketUpdateFilterTag msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;

                if (container instanceof FilterTagContainer) {
                    ItemStack stack = ((FilterTagContainer) container).filterItem;
                    FilterTag.setAllowList(stack, msg.allowList);
                    FilterTag.setTags(stack, msg.tags);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
