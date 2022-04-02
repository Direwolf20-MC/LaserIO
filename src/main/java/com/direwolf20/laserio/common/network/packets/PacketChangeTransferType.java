package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeTransferType {
    public PacketChangeTransferType() {
    }

    public static void encode(PacketChangeTransferType msg, FriendlyByteBuf buffer) {
    }

    public static PacketChangeTransferType decode(FriendlyByteBuf buffer) {
        return new PacketChangeTransferType();
    }

    public static class Handler {
        public static void handle(PacketChangeTransferType msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = player.getMainHandItem(); //ToDo Support for offhand?
                BaseCard.nextTransferMode(stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
