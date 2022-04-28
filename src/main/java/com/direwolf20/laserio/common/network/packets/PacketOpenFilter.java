package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.BasicFilterContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.ItemCardContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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

    public static class Handler {
        public static void handle(PacketOpenFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                AbstractContainerMenu container = sender.containerMenu;
                if (container == null || !(container instanceof ItemCardContainer))
                    return;

                Slot slot = container.slots.get(msg.slotNumber);
                ItemStack itemStack = slot.getItem();


                if (itemStack.getItem() instanceof FilterBasic) {
                    FilterBasicHandler handler = FilterBasic.getInventory(itemStack);
                    NetworkHooks.openGui(sender, new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(windowId, playerInventory, sender, handler, ((ItemCardContainer) container).sourceContainer, itemStack), new TranslatableComponent("")), (buf -> {
                        buf.writeItem(itemStack);
                    }));
                }
                if (itemStack.getItem() instanceof FilterCount) {
                    FilterCountHandler handler = FilterCount.getInventory(itemStack);
                    ContainerData slotCounts = new ContainerData() {
                        @Override
                        public int get(int index) {
                            if (index < 15)
                                return FilterCount.getInventory(itemStack).getStackInSlot(index).getCount();
                            else
                                throw new IllegalArgumentException("Invalid index: " + index);
                        }

                        @Override
                        public void set(int index, int value) {
                            throw new IllegalStateException("Cannot set values through IIntArray");
                        }

                        @Override
                        public int getCount() {
                            return 15;
                        }
                    };


                    NetworkHooks.openGui(sender, new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, sender, handler, ((ItemCardContainer) container).sourceContainer, itemStack, slotCounts), new TranslatableComponent("")), (buf -> {
                        buf.writeItem(itemStack);
                    }));
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
