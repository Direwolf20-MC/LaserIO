package com.direwolf20.laserio.common.network.packets;

import java.util.function.Supplier;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateOverclockerChannel {
	byte channel;
	boolean visible;
	
	public PacketUpdateOverclockerChannel(byte channel, boolean visible) {
		this.channel = channel;
		this.visible = visible;
	}
	
	public static void encode(PacketUpdateOverclockerChannel msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.channel);
        buffer.writeBoolean(msg.visible);
    }
	
	public static PacketUpdateOverclockerChannel decode(FriendlyByteBuf buffer) {
		return new PacketUpdateOverclockerChannel(buffer.readByte(), buffer.readBoolean());
	}
	
	public static class Handler {
        public static void handle(PacketUpdateOverclockerChannel msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
            	
            	ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;
                
                if (container instanceof CardItemContainer || container instanceof CardEnergyContainer) {
                    CardItemHandler handler;
                    if (container instanceof CardEnergyContainer)
                        handler = ((CardEnergyContainer) container).handler;
                    else 
                        handler = ((CardItemContainer) container).handler;

                    if(handler == null) return;
                    
                    ItemStack serverItem = handler.getStackInSlot(container instanceof CardEnergyContainer ? 1 : 2);
                    
                    OverclockerChannel.setChannel(serverItem , msg.channel);
                    OverclockerChannel.setChannelVisible(serverItem, msg.visible);
                    
                }
            });
            ctx.get().setPacketHandled(true);
        }
	}
}
