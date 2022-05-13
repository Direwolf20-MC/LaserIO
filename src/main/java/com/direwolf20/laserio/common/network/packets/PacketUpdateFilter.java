package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateFilter {
    boolean allowList;
    boolean compareNBT;

    public PacketUpdateFilter(boolean allowList, boolean compareNBT) {
        this.allowList = allowList;
        this.compareNBT = compareNBT;
    }

    public static void encode(PacketUpdateFilter msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.allowList);
        buffer.writeBoolean(msg.compareNBT);
    }

    public static PacketUpdateFilter decode(FriendlyByteBuf buffer) {
        return new PacketUpdateFilter(buffer.readBoolean(), buffer.readBoolean());
    }

    public static class Handler {
        public static void handle(PacketUpdateFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;

                if (container instanceof CardItemContainer) {
                    ItemStack stack = container.slots.get(0).getItem();
                    if (stack.isEmpty()) return;
                    FilterBasic.setAllowList(stack, msg.allowList);
                    FilterBasic.setCompareNBT(stack, msg.compareNBT);
                }
                if (container instanceof FilterBasicContainer) {
                    ItemStack stack = ((FilterBasicContainer) container).filterItem;
                    FilterBasic.setAllowList(stack, msg.allowList);
                    FilterBasic.setCompareNBT(stack, msg.compareNBT);
                }
                if (container instanceof FilterCountContainer) {
                    ItemStack stack = ((FilterCountContainer) container).filterItem;
                    FilterCount.setAllowList(stack, msg.allowList);
                    FilterCount.setCompareNBT(stack, msg.compareNBT);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
