package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterNBT;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.network.data.OpenFilterPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.direwolf20.laserio.common.blocks.LaserNode.SCREEN_LASERNODE;

public class PacketOpenFilter {
    public static final PacketOpenFilter INSTANCE = new PacketOpenFilter();

    public static PacketOpenFilter get() {
        return INSTANCE;
    }

    public static void doOpenFilter(ItemStack filterItem, ItemStack cardItem, ServerPlayer sender, BlockPos sourcePos) {
        if (filterItem.getItem() instanceof FilterBasic) {
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(SCREEN_LASERNODE);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new FilterBasicContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem);
                }
            };
            sender.openMenu(containerProvider, (buf -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, filterItem);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterCount) {
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(SCREEN_LASERNODE);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new FilterCountContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem);
                }
            };
            sender.openMenu(containerProvider, (buf -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, filterItem);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, cardItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterTag) {
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(SCREEN_LASERNODE);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new FilterTagContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem);
                }
            };
            sender.openMenu(containerProvider, (buf -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, filterItem);
            }));
        }
        if (filterItem.getItem() instanceof FilterNBT) {
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(SCREEN_LASERNODE);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new FilterNBTContainer(windowId, playerInventory, sender, sourcePos, filterItem, cardItem);
                }
            };
            sender.openMenu(containerProvider, (buf -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, filterItem);
            }));
        }
    }

    public void handle(final OpenFilterPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null || !(container instanceof CardItemContainer))
                return;

            Slot slot = container.slots.get(payload.slotNumber());

            ItemStack itemStack = slot.getItem();
            doOpenFilter(itemStack, ((CardItemContainer) container).cardItem, (ServerPlayer) sender, ((CardItemContainer) container).sourceContainer);
        });
    }
}
