package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.network.data.OpenCardPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

import static com.direwolf20.laserio.common.items.cards.BaseCard.getInventory;

public class PacketOpenCard {
    public static final PacketOpenCard INSTANCE = new PacketOpenCard();

    public static PacketOpenCard get() {
        return INSTANCE;
    }

    public void handle(final OpenCardPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> senderOptional = context.player();
            if (senderOptional.isEmpty())
                return;
            ServerPlayer sender = (ServerPlayer) senderOptional.get();


            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            Slot slot = container.slots.get(payload.slotNumber());
            ItemStack itemStack = slot.getItem();
            CardItemHandler handler = getInventory(itemStack);
            byte sideTemp = -1;
            if (container instanceof LaserNodeContainer laserNodeContainer)
                sideTemp = laserNodeContainer.side;
            final byte side = sideTemp;
            if (itemStack.getItem() instanceof CardItem) {
                if (!payload.hasShiftDown()) {
                    sender.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new CardItemContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                        buf.writeItem(itemStack);
                        buf.writeByte(side);
                    }));
                } else {
                    ItemStack filterItem = handler.getStackInSlot(0);
                    if (filterItem.getItem() instanceof BaseFilter)
                        PacketOpenFilter.doOpenFilter(filterItem, itemStack, sender, payload.sourcePos());
                }
            } else if (itemStack.getItem() instanceof CardFluid) {
                if (!payload.hasShiftDown()) {
                    sender.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new CardFluidContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                        buf.writeItem(itemStack);
                        buf.writeByte(side);
                    }));
                } else {
                    ItemStack filterItem = handler.getStackInSlot(0);
                    if (filterItem.getItem() instanceof BaseFilter)
                        PacketOpenFilter.doOpenFilter(filterItem, itemStack, sender, payload.sourcePos());
                }
            } else if (itemStack.getItem() instanceof CardEnergy) {
                sender.openMenu(new SimpleMenuProvider(
                        (windowId, playerInventory, playerEntity) -> new CardEnergyContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side), net.minecraft.network.chat.Component.translatable("")), (buf -> {
                    buf.writeItem(itemStack);
                    buf.writeByte(side);
                }));

            } else if (itemStack.getItem() instanceof CardRedstone) {
                sender.openMenu(new SimpleMenuProvider(
                        (windowId, playerInventory, playerEntity) -> new CardRedstoneContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side), Component.translatable("")), (buf -> {
                    buf.writeItem(itemStack);
                    buf.writeByte(side);
                }));

            }
        });
    }
}
