package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGhostSlot {
    private int slotNumber;
    private ItemStack stack;
    private int count;

    public PacketGhostSlot(int slotNumber, ItemStack stack, int count) {
        this.slotNumber = slotNumber;
        this.stack = stack;
        this.count = count;
    }

    public static void encode(PacketGhostSlot msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeItem(msg.stack);
        buffer.writeInt(msg.count);
    }

    public static PacketGhostSlot decode(FriendlyByteBuf buffer) {
        return new PacketGhostSlot(buffer.readInt(), buffer.readItem(), buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketGhostSlot msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                AbstractContainerMenu container = sender.containerMenu;
                if (container == null)
                    return;

                if (container instanceof CardItemContainer) {
                    ItemStack stack = msg.stack;
                    stack.setCount(msg.count);
                    FilterCountHandler handler = (FilterCountHandler) ((CardItemContainer) container).filterHandler;
                    handler.setStackInSlotSave(msg.slotNumber - CardItemContainer.SLOTS, stack);
                } else if (container instanceof FilterCountContainer) {
                    ItemStack stack = msg.stack;
                    stack.setCount(msg.count);
                    FilterCountHandler handler = ((FilterCountContainer) container).handler;
                    handler.setStackInSlotSave(msg.slotNumber, stack);
                } else {
                    Slot slot = container.slots.get(msg.slotNumber);
                    ItemStack stack = msg.stack;
                    stack.setCount(msg.count);
                    if (slot instanceof FilterBasicSlot)
                        slot.set(stack);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
