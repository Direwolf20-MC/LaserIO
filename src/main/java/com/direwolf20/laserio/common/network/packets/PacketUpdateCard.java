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
    short priority;

    public PacketUpdateCard(byte mode, byte channel, byte extractAmt, short priority) {
        this.mode = mode;
        this.channel = channel;
        this.extractAmt = extractAmt;
        this.priority = priority;
    }

    public static void encode(PacketUpdateCard msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.mode);
        buffer.writeByte(msg.channel);
        buffer.writeByte(msg.extractAmt);
        buffer.writeShort(msg.priority);
    }

    public static PacketUpdateCard decode(FriendlyByteBuf buffer) {
        return new PacketUpdateCard(buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readShort());
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
                    BaseCard.setPriority(stack, msg.priority);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
