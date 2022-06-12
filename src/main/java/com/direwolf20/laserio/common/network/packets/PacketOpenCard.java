package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
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
    private boolean hasShiftDown;

    public PacketOpenCard(int slotNumber, BlockPos pos, boolean hasShiftDown) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
        this.hasShiftDown = hasShiftDown;
    }

    public static void encode(PacketOpenCard msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeBoolean(msg.hasShiftDown);
    }

    public static PacketOpenCard decode(FriendlyByteBuf buffer) {
        return new PacketOpenCard(buffer.readInt(), buffer.readBlockPos(), buffer.readBoolean());

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

                if (itemStack.getItem() instanceof CardItem) {
                    if (!msg.hasShiftDown) {
                        NetworkHooks.openGui(sender, new SimpleMenuProvider(
                                (windowId, playerInventory, playerEntity) -> new CardItemContainer(windowId, playerInventory, sender, msg.sourcePos, itemStack), new TranslatableComponent("")), (buf -> {
                            buf.writeItem(itemStack);
                        }));
                    } else {
                        ItemStack filterItem = handler.getStackInSlot(0);
                        if (filterItem.getItem() instanceof BaseFilter)
                            PacketOpenFilter.doOpenFilter(filterItem, itemStack, sender, msg.sourcePos);
                    }
                } else if (itemStack.getItem() instanceof CardFluid) {
                    if (!msg.hasShiftDown) {
                        NetworkHooks.openGui(sender, new SimpleMenuProvider(
                                (windowId, playerInventory, playerEntity) -> new CardFluidContainer(windowId, playerInventory, sender, msg.sourcePos, itemStack), new TranslatableComponent("")), (buf -> {
                            buf.writeItem(itemStack);
                        }));
                    } else {
                        ItemStack filterItem = handler.getStackInSlot(0);
                        if (filterItem.getItem() instanceof BaseFilter)
                            PacketOpenFilter.doOpenFilter(filterItem, itemStack, sender, msg.sourcePos);
                    }
                } else if (itemStack.getItem() instanceof CardEnergy) {
                    NetworkHooks.openGui(sender, new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new CardEnergyContainer(windowId, playerInventory, sender, msg.sourcePos, itemStack), new TranslatableComponent("")), (buf -> {
                        buf.writeItem(itemStack);
                    }));

                }
            });


            ctx.get().setPacketHandled(true);
        }
    }
}
