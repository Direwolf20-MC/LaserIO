package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.network.data.OpenCardPayload;
import com.direwolf20.laserio.integration.mekanism.CardChemical;
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
import static com.direwolf20.laserio.common.items.cards.BaseCard.getInventory;

public class PacketOpenCard {
    public static final PacketOpenCard INSTANCE = new PacketOpenCard();

    public static PacketOpenCard get() {
        return INSTANCE;
    }

    public void handle(final OpenCardPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();

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
                            return new CardItemContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side);
                        }
                    };
                    sender.openMenu(containerProvider, (buf -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
                        buf.writeByte(side);
                    }));
                } else {
                    ItemStack filterItem = handler.getStackInSlot(0);
                    if (filterItem.getItem() instanceof BaseFilter)
                        PacketOpenFilter.doOpenFilter(filterItem, itemStack, (ServerPlayer) sender, payload.sourcePos());
                }
            } else if (itemStack.getItem() instanceof CardFluid) {
                if (!payload.hasShiftDown()) {
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
                            return new CardFluidContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side);
                        }
                    };
                    sender.openMenu(containerProvider, (buf -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
                        buf.writeByte(side);
                    }));
                } else {
                    ItemStack filterItem = handler.getStackInSlot(0);
                    if (filterItem.getItem() instanceof BaseFilter)
                        PacketOpenFilter.doOpenFilter(filterItem, itemStack, (ServerPlayer) sender, payload.sourcePos());
                }
            } else if (itemStack.getItem() instanceof CardEnergy) {
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
                        return new CardEnergyContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side);
                    }
                };
                sender.openMenu(containerProvider, (buf -> {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
                    buf.writeByte(side);
                }));
            } else if (itemStack.getItem() instanceof CardRedstone) {
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
                        return new CardRedstoneContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side);
                    }
                };
                sender.openMenu(containerProvider, (buf -> {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
                    buf.writeByte(side);
                }));
            } else if (itemStack.getItem() instanceof CardChemical) {
                if (!payload.hasShiftDown()) {
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
                            return new CardChemicalContainer(windowId, playerInventory, sender, payload.sourcePos(), itemStack, side);
                        }
                    };
                    sender.openMenu(containerProvider, (buf -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
                        buf.writeByte(side);
                    }));
                } else {
                    ItemStack filterItem = handler.getStackInSlot(0);
                    if (filterItem.getItem() instanceof BaseFilter)
                        PacketOpenFilter.doOpenFilter(filterItem, itemStack, (ServerPlayer) sender, payload.sourcePos());
                }
            }
        });
    }
}
