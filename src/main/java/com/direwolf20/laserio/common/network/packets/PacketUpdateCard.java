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
    byte sneaky;
    short ticks;

    public PacketUpdateCard(byte mode, byte channel, byte extractAmt, short priority, byte sneaky, short ticks) {
        this.mode = mode;
        this.channel = channel;
        this.extractAmt = extractAmt;
        this.priority = priority;
        this.sneaky = sneaky;
        this.ticks = ticks;
    }

    public static void encode(PacketUpdateCard msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.mode);
        buffer.writeByte(msg.channel);
        buffer.writeByte(msg.extractAmt);
        buffer.writeShort(msg.priority);
        buffer.writeByte(msg.sneaky);
        buffer.writeShort(msg.ticks);
    }

    public static PacketUpdateCard decode(FriendlyByteBuf buffer) {
        return new PacketUpdateCard(buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readShort(), buffer.readByte(), buffer.readShort());
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
                    byte extractAmt = msg.extractAmt;
                    int overClockerCount = container.getSlot(1).getItem().getCount();
                    if (extractAmt > Math.max(overClockerCount * 16, 8)) {
                        extractAmt = (byte) Math.max(overClockerCount * 16, 8);
                    }
                    BaseCard.setItemExtractAmt(stack, extractAmt);
                    BaseCard.setPriority(stack, msg.priority);
                    BaseCard.setSneaky(stack, msg.sneaky);
                    short ticks = msg.ticks;
                    if (ticks < Math.max(20 - overClockerCount * 5, 1))
                        ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                    BaseCard.setItemExtractSpeed(stack, ticks);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
