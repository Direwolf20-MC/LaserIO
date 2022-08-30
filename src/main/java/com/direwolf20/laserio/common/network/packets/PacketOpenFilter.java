package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.FilterTagContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;


public class PacketOpenFilter {
    private int slotNumber;

    public PacketOpenFilter(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static void encode(PacketOpenFilter msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slotNumber);
    }

    public static PacketOpenFilter decode(FriendlyByteBuf buffer) {
        return new PacketOpenFilter(buffer.readInt());

    }

    public static void doOpenFilter(ItemStack filterItem, ItemStack cardItem, ServerPlayer sender, BlockPos sourcePos) {
        if (filterItem.getItem() instanceof FilterBasic) {
            FilterBasicHandler handler = FilterBasic.getInventory(filterItem);
            NetworkHooks.openScreen(sender, new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterBasicContainer(windowId, playerInventory, sender, handler, sourcePos, filterItem, cardItem), Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterCount) {
            NetworkHooks.openScreen(sender, new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem), Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterTag) {
            FilterBasicHandler handler = FilterBasic.getInventory(filterItem);
            NetworkHooks.openScreen(sender, new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterTagContainer(windowId, playerInventory, sender, handler, sourcePos, filterItem, cardItem), Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(ItemStack.EMPTY);
            }));
        }
    }

    public static class Handler {
        public static void handle(PacketOpenFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                AbstractContainerMenu container = sender.containerMenu;
                if (container == null || !(container instanceof CardItemContainer))
                    return;

                Slot slot = container.slots.get(msg.slotNumber);

                ItemStack itemStack = slot.getItem();
                doOpenFilter(itemStack, ((CardItemContainer) container).cardItem, sender, ((CardItemContainer) container).sourceContainer);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
