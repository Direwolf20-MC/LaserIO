package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.setup.LaserIORegistration;
import com.direwolf20.laserio.util.CardHolderItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.access.ItemAccess;


public class CardHolderContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public ItemStack cardHolder;
    public Player playerEntity;
    private Inventory playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public CardHolderItemStackHandler cardHolderHandler;

    public CardHolderContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
    }

    public CardHolderContainer(int windowId, Inventory playerInventory, Player player, ItemStack cardHolder) {
        super(LaserIORegistration.CardHolder_Container.get(), windowId);
        playerEntity = player;
        this.cardHolderHandler = new CardHolderItemStackHandler(SLOTS,
                cardHolder.isEmpty() ? ItemAccess.forStack(new ItemStack(Items.STONE)) : ItemAccess.forStack(cardHolder));
        this.playerInventory = playerInventory;
        this.cardHolder = cardHolder;
        if (cardHolderHandler != null) {
            addSlotBox(cardHolderHandler, 0, 44, 17, 5, 18, 3, 18);
        }

        layoutPlayerInventorySlots(8, 84);
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS && slots.get(slotId) instanceof CardHolderSlot) {
            ItemStack carriedItem = getCarried();
            ItemStack stackInSlot = slots.get(slotId).getItem();
            if (stackInSlot.getMaxStackSize() == 1 && stackInSlot.getCount() > 1) {
                if (!carriedItem.isEmpty() && !stackInSlot.isEmpty() && !ItemStack.isSameItemSameComponents(carriedItem, stackInSlot))
                    return;
            }
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return playerIn.getMainHandItem().equals(cardHolder) || playerIn.getOffhandItem().equals(cardHolder);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int fromSlot, int toSlot, boolean p_38907_) {
        boolean flag = false;
        int i = fromSlot;
        if (p_38907_) {
            i = toSlot - 1;
        }

        while (!itemStack.isEmpty()) {
            if (p_38907_) {
                if (i < fromSlot) {
                    break;
                }
            } else if (i >= toSlot) {
                break;
            }

            Slot slot = this.slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemstack)) {
                int j = itemstack.getCount() + itemStack.getCount();
                int maxSize = Math.min(slot.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                if (j <= maxSize) {
                    itemStack.setCount(0);
                    itemstack.setCount(j);
                    slot.setByPlayer(itemstack);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    itemStack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.setByPlayer(itemstack);
                    slot.setChanged();
                    flag = true;
                }
            }

            if (p_38907_) {
                --i;
            } else {
                ++i;
            }
        }

        if (!itemStack.isEmpty()) {
            if (p_38907_) {
                i = toSlot - 1;
            } else {
                i = fromSlot;
            }

            while (true) {
                if (p_38907_) {
                    if (i < fromSlot) {
                        break;
                    }
                } else if (i >= toSlot) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(itemStack) && slot1.getItem().getCount() < slot1.getMaxStackSize(itemStack)) {
                    if (itemStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(itemStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(itemStack.split(slot1.getMaxStackSize(itemStack)));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (p_38907_) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        if (slot instanceof CardHolderSlot)
            return false;
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            //If its one of the 15 slots at the top try to move it into your inventory
            if (index < SLOTS) {
                if (playerIn.getInventory().getFreeSlot() != -1) {
                    this.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true);
                    slot.setByPlayer(stack);
                } else {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            slot.onTake(playerIn, stack);
            if (stack.getCount() < itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
        }

        return itemstack;
    }

    private int addSlotRange(CardHolderItemStackHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new CardHolderSlot(handler, handler::set, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(CardHolderItemStackHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        // Hotbar
        topRow += 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, leftCol + col * 18, topRow));
        }
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
    }
}
