package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterNBT;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.network.data.OpenFilterPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

public class PacketOpenFilter {
    public static final PacketOpenFilter INSTANCE = new PacketOpenFilter();

    public static PacketOpenFilter get() {
        return INSTANCE;
    }

    public static void doOpenFilter(ItemStack filterItem, ItemStack cardItem, ServerPlayer sender, BlockPos sourcePos) {
        if (filterItem.getItem() instanceof FilterBasic) {
            FilterBasicHandler handler = FilterBasic.getInventory(filterItem);
            sender.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterBasicContainer(windowId, playerInventory, sender, handler, sourcePos, filterItem, cardItem), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterCount) {
            sender.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterTag) {
            FilterBasicHandler handler = FilterBasic.getInventory(filterItem);
            sender.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterTagContainer(windowId, playerInventory, sender, handler, sourcePos, filterItem, cardItem), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(ItemStack.EMPTY);
            }));
        }
        if (filterItem.getItem() instanceof FilterNBT) {
            FilterBasicHandler handler = FilterBasic.getInventory(filterItem);
            sender.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new FilterNBTContainer(windowId, playerInventory, sender, handler, sourcePos, filterItem, cardItem), Component.translatable("")), (buf -> {
                buf.writeItem(filterItem);
                buf.writeItem(ItemStack.EMPTY);
            }));
        }
    }

    public void handle(final OpenFilterPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> senderOptional = context.player();
            if (senderOptional.isEmpty())
                return;
            ServerPlayer sender = (ServerPlayer) senderOptional.get();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null || !(container instanceof CardItemContainer))
                return;

            Slot slot = container.slots.get(payload.slotNumber());

            ItemStack itemStack = slot.getItem();
            doOpenFilter(itemStack, ((CardItemContainer) container).cardItem, sender, ((CardItemContainer) container).sourceContainer);
        });
    }
}
