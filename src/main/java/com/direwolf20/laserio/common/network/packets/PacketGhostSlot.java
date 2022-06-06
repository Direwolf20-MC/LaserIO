package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterCount;
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
    private int mbAmt;

    public PacketGhostSlot(int slotNumber, ItemStack stack, int count) {
        this(slotNumber, stack, count, -1);
    }

    public PacketGhostSlot(int slotNumber, ItemStack stack, int count, int mbAmt) {
        this.slotNumber = slotNumber;
        this.stack = stack;
        this.count = count;
        this.mbAmt = mbAmt;
    }

    public static void encode(PacketGhostSlot msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeItem(msg.stack);
        buffer.writeInt(msg.count);
        buffer.writeInt(msg.mbAmt);
    }

    public static PacketGhostSlot decode(FriendlyByteBuf buffer) {
        return new PacketGhostSlot(buffer.readInt(), buffer.readItem(), buffer.readInt(), buffer.readInt());
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

                ItemStack filterStack = container.slots.get(0).getItem();

                if (container instanceof CardItemContainer && filterStack.getItem() instanceof FilterCount) {
                    ItemStack stack = msg.stack;
                    FilterCountHandler handler = (FilterCountHandler) ((CardItemContainer) container).filterHandler;
                    int mbAmt = msg.mbAmt;
                    if (mbAmt == 0 && container instanceof CardFluidContainer) {
                        stack.setCount(0);
                    } else {
                        stack.setCount(msg.count);
                    }
                    handler.setStackInSlotSave(msg.slotNumber - CardItemContainer.SLOTS, stack);

                    if (mbAmt != -1 && container instanceof CardFluidContainer) { //MB amt is only done in CardFluidContainers
                        handler.setMBAmountInSlot(msg.slotNumber - CardItemContainer.SLOTS, mbAmt);
                    }
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
