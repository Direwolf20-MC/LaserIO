package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.ItemCardContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.CardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

import static com.direwolf20.laserio.common.items.cards.BaseCard.getInventory;


public class PacketOpenCard {
    private int slotNumber;
    private BlockPos sourcePos;

    public PacketOpenCard(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
    }

    public static void encode(PacketOpenCard msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketOpenCard decode(FriendlyByteBuf buffer) {
        return new PacketOpenCard(buffer.readInt(), buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketOpenCard msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                AbstractContainerMenu container = sender.containerMenu;
                if (container == null)
                    return;

                Slot slot = container.slots.get(msg.slotNumber);
                ItemStack itemStack = slot.getItem();
                CardItemHandler handler = getInventory(itemStack);
                /*IIntArray tempArray;
                ItemStackHandler handler = getInventory(itemStack);
                if (itemStack.getItem() instanceof CardStocker) {
                    tempArray = new IIntArray() {
                        @Override
                        public int get(int index) {
                            if (index == 0)
                                return BaseCard.getPriority(itemStack);
                            else if (index < 16)
                                return BaseCard.getInventory(itemStack).getStackInSlot(index - 1).getCount();
                            else
                                throw new IllegalArgumentException("Invalid index: " + index);
                        }

                        @Override
                        public void set(int index, int value) {
                            throw new IllegalStateException("Cannot set values through IIntArray");
                        }

                        @Override
                        public int size() {
                            return 16;
                        }
                    };
                } else {
                    tempArray = new IIntArray() {
                        @Override
                        public int get(int index) {
                            switch (index) {
                                case 0:
                                    return BaseCard.getPriority(itemStack);
                                case 1:
                                    return BaseCard.getExtractAmt(itemStack);
                                default:
                                    throw new IllegalArgumentException("Invalid index: " + index);
                            }
                        }

                        @Override
                        public void set(int index, int value) {
                            throw new IllegalStateException("Cannot set values through IIntArray");
                        }

                        @Override
                        public int size() {
                            return 2;
                        }
                    };
                }*/
                if (itemStack.getItem() instanceof CardItem) {
                    NetworkHooks.openGui(sender, new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new ItemCardContainer(windowId, playerInventory, sender, handler, msg.sourcePos, itemStack), new TranslatableComponent("")), (buf -> {
                        buf.writeItem(itemStack);
                    }));
                } /*else if (itemStack.getItem() instanceof CardInserterTag) {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new TagFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                    }));
                } else if (itemStack.getItem() instanceof CardPolymorph) {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new PolyFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                        buf.writeBlockPos(msg.sourcePos);
                        buf.writeInt(msg.slotNumber);
                    }));
                } else {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                    }));
                }
                //}*/
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
