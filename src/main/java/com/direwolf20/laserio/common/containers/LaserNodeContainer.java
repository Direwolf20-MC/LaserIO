package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.NodeItemHandler;
import com.direwolf20.laserio.common.containers.customslot.NodeSlot;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

public class LaserNodeContainer extends AbstractContainerMenu {
    //private BlockEntity blockEntity;
    public static final int SLOTS = 9;
    private Player playerEntity;
    private IItemHandler playerInventory;
    ContainerLevelAccess containerLevelAccess;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public LaserNodeBE tile;

    public LaserNodeContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player) {
        this((LaserNodeBE) playerInventory.player.level.getBlockEntity(pos), windowId, pos, playerInventory, player, new NodeItemHandler(SLOTS), ContainerLevelAccess.NULL);
    }

    public LaserNodeContainer(@Nullable LaserNodeBE tile, int windowId, BlockPos pos, Inventory playerInventory, Player player, NodeItemHandler handler, ContainerLevelAccess containerLevelAccess) {
        super(Registration.LaserNode_Container.get(), windowId);
        //blockEntity = player.getCommandSenderWorld().getBlockEntity(pos);
        this.playerEntity = player;
        this.tile = tile;
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
                if (!this.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                    /*if (index < 27 + SLOTS && !this.moveItemStackTo(stack, 27 + SLOTS, 36 + SLOTS, false)) {
                        return ItemStack.EMPTY;
                    } else if (index < 36 + SLOTS && !this.moveItemStackTo(stack, SLOTS, 27 + SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }*/
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
            if (handler instanceof NodeItemHandler)
                addSlot(new NodeSlot(handler, index, x, y));
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
}
