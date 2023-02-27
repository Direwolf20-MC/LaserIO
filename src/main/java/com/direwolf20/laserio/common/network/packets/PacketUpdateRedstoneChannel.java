package com.direwolf20.laserio.common.network.packets;

import java.util.function.Supplier;

import com.direwolf20.laserio.common.containers.CardRedstoneContainer;
import com.direwolf20.laserio.common.items.cards.CardRedstone;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateRedstoneChannel {
	byte channel;
	
	public PacketUpdateRedstoneChannel(byte channel) {
		this.channel = channel;
	}
	
	public static void encode(PacketUpdateRedstoneChannel msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.channel);
    }
	
	public static PacketUpdateRedstoneChannel decode(FriendlyByteBuf buffer) {
		return new PacketUpdateRedstoneChannel(buffer.readByte());
	}
	
	public static class Handler {
        public static void handle(PacketUpdateRedstoneChannel msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
            	
            	ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;
                
                if (!(container instanceof CardRedstoneContainer))
                	return;
                
                ItemStack card;
                card = ((CardRedstoneContainer) container).cardItem;

                CardRedstone.setRedstoneChannel(card, msg.channel);

            });
            ctx.get().setPacketHandled(true);
        }
	}
}
