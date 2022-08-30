package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class CardHolderContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    //public CardHolderHandler handler;
    public ItemStack cardHolder;
    public Player playerEntity;
    private IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public IItemHandler iItemHandler;

    public CardHolderContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, extraData.readItem(), new ItemStackHandler(SLOTS));
        //this.cardHolder = extraData.readItem();
    }

    public CardHolderContainer(int windowId, Inventory playerInventory, Player player, ItemStack cardHolder, IItemHandler iItemHandler) {
        super(Registration.CardHolder_Container.get(), windowId);
        playerEntity = player;
        //this.handler = CardHolder.getInventory(cardHolder);
        this.iItemHandler = iItemHandler;
        this.playerInventory = new InvWrapper(playerInventory);
        this.cardHolder = cardHolder;
        if (iItemHandler != null) {
            addSlotBox(iItemHandler, 0, 44, 17, 5, 18, 3, 18);
        }

        layoutPlayerInventorySlots(8, 84);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS && slots.get(slotId) instanceof CardHolderSlot) {
            ItemStack carriedItem = getCarried();
            ItemStack stackInSlot = slots.get(slotId).getItem();
            if (!carriedItem.isEmpty() && !stackInSlot.isEmpty() && !ItemStack.isSameItemSameTags(carriedItem, stackInSlot))
                return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        //if (sourceContainer.equals(BlockPos.ZERO))
        return playerIn.getMainHandItem().equals(cardHolder) || playerIn.getOffhandItem().equals(cardHolder);
        //return true;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int fromSlot, int toSlot, boolean p_38907_) {
        //return super.moveItemStackTo(itemStack, fromSlot, toSlot, p_38907_);
        //System.out.println(itemStack + ":" + fromSlot + ":" + toSlot + ":" + p_38907_);
        boolean flag = false;
        int i = fromSlot;
        if (p_38907_) {
            i = toSlot - 1;
        }

        //if (itemStack.isStackable()) {
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
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemstack)) {
                int j = itemstack.getCount() + itemStack.getCount();
                int maxSize = Math.min(slot.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                if (j <= maxSize) {
                    itemStack.setCount(0);
                    itemstack.setCount(j);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    itemStack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
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
        //}

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
                        slot1.set(itemStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(itemStack.split(slot1.getMaxStackSize(itemStack)));
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
    public ItemStack quickMoveStack(Player playerIn, int index) { //Todo see if we can get this to only run once.
        //System.out.println(index);
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            //itemstack.setCount(1);
            //If its one of the 20 slots at the top try to move it into your inventory
            if (index < SLOTS) {
                if (playerIn.getInventory().getFreeSlot() != -1) {
                    // moveItemStackTo() always moves the item, no matter the return value. fixes #87
                    this.moveItemStackTo(stack.split(1), SLOTS, 36 + SLOTS, true);
                } else {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            /*if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }*/
            slot.onTake(playerIn, stack);
            if (stack.getCount() < itemstack.getCount()) {
                return ItemStack.EMPTY;
            }


        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if ((handler.getSlots() == SLOTS))
                addSlot(new CardHolderSlot(handler, index, x, y));
            else
                addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
    }
}
