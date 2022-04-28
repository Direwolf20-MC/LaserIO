package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateCard {
    byte mode;
    byte channel;
    byte extractAmt;

    public PacketUpdateCard(byte mode, byte channel, byte extractAmt) {
        this.mode = mode;
        this.channel = channel;
        this.extractAmt = extractAmt;
    }

    public static void encode(PacketUpdateCard msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.mode);
        buffer.writeByte(msg.channel);
        buffer.writeByte(msg.extractAmt);
    }

    public static PacketUpdateCard decode(FriendlyByteBuf buffer) {
        return new PacketUpdateCard(buffer.readByte(), buffer.readByte(), buffer.readByte());
    }

    public static class Handler {
        public static void handle(PacketUpdateCard msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;

                if (container instanceof CardItemContainer) {
                    ItemStack stack = ((CardItemContainer) container).cardItem;
                    BaseCard.setTransferMode(stack, msg.mode);
                    BaseCard.setChannel(stack, msg.channel);
                    BaseCard.setItemExtractAmt(stack, msg.extractAmt);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
