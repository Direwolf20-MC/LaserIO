package com.direwolf20.laserio.common.network.packets;

import java.util.function.Supplier;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateCardChannel {
	byte channel;
	
	public PacketUpdateCardChannel(byte channel) {
		this.channel = channel;
	}
	
	public static void encode(PacketUpdateCardChannel msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.channel);
    }
	
	public static PacketUpdateCardChannel decode(FriendlyByteBuf buffer) {
		return new PacketUpdateCardChannel(buffer.readByte());
	}
	
	public static class Handler {
        public static void handle(PacketUpdateCardChannel msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
            	
            	ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;
                
                if (container instanceof CardItemContainer || container instanceof CardEnergyContainer) {
                    ItemStack card;
                    if (container instanceof CardEnergyContainer)
                        card = ((CardEnergyContainer) container).cardItem;
                    else 
                        card = ((CardItemContainer) container).cardItem;

                    BaseCard.setChannel(card, msg.channel);
                    
                }
            });
            ctx.get().setPacketHandled(true);
        }
	}
}
