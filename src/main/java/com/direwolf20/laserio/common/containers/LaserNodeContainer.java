package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class LaserNodeContainer extends AbstractContainerMenu {
    //private BlockEntity blockEntity;
    public static final int SLOTS = 9;
    private Player playerEntity;
    private IItemHandler playerInventory;
    ContainerLevelAccess containerLevelAccess;

    public LaserNodeContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player) {
        this(windowId, pos, playerInventory, player, new ItemStackHandler(SLOTS), ContainerLevelAccess.NULL);
    }

    public LaserNodeContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player, IItemHandler handler, ContainerLevelAccess containerLevelAccess) {
        super(Registration.LaserNode_Container.get(), windowId);
        //blockEntity = player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.containerLevelAccess = containerLevelAccess;
        if (handler != null)
            addSlotBox(handler, 0, 62, 17, 3, 18, 3, 18);

        layoutPlayerInventorySlots(8, 84);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(containerLevelAccess, playerEntity, Registration.LaserNode.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            //If its one of the 9 slots at the top try to move it into your inventory
            if (index < SLOTS) {
                if (!this.moveItemStackTo(stack, SLOTS, this.playerInventory.getSlots(), false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (stack.getItem() instanceof BaseCard) {
                    if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 36) {
                    if (!this.moveItemStackTo(stack, 36, this.playerInventory.getSlots(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 45 && !this.moveItemStackTo(stack, SLOTS, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }


    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
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
}
