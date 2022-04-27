package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterCountSlot;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class FilterCountContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public FilterCountHandler handler;
    public ItemStack filterItem;
    public Player playerEntity;
    private IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public ContainerData slotCounts;

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, new FilterCountHandler(SLOTS, ItemStack.EMPTY), ItemStack.EMPTY, new SimpleContainerData(15));
        filterItem = extraData.readItem();
    }

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, FilterCountHandler handler, ItemStack filterItem, ContainerData cardData) {
        super(Registration.FilterCount_Container.get(), windowId);
        playerEntity = player;
        this.handler = handler;
        this.playerInventory = new InvWrapper(playerInventory);
        this.filterItem = filterItem;
        if (handler != null)
            addSlotBox(handler, 0, 44, 22, 5, 18, 3, 18);

        layoutPlayerInventorySlots(8, 84);

        this.slotCounts = cardData;
        addDataSlots(cardData);
    }

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, FilterCountHandler handler, BlockPos sourcePos, ItemStack filterItem, ContainerData cardData) {
        this(windowId, playerInventory, player, handler, filterItem, cardData);
        this.sourceContainer = sourcePos;
        this.slotCounts = cardData;
        addDataSlots(cardData);
    }

    public int getStackSize(int slot) {
        return this.slotCounts.get(slot);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
        //return playerIn.getMainHandItem().equals(cardItem); //TODO Validate this and check offhand?
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem().copy();
            currentStack.setCount(1);
            //Only do this if we click from the players inventory
            if (index >= SLOTS) {
                for (int i = 0; i < SLOTS; i++) { //Prevents the same item from going in there more than once.
                    if (this.slots.get(i).getItem().equals(currentStack, false)) //Don't limit tags
                        return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(currentStack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (handler instanceof FilterCountHandler)
                addSlot(new FilterCountSlot(handler, index, x, y));
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
    public void removed(Player playerIn) { //Todo see if we can send the player back to their last container screen?
        Level world = playerIn.getLevel();
        if (!world.isClientSide) {
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE)
                    ((LaserNodeBE) blockEntity).updateThisNode();

            }
        }
        super.removed(playerIn);
    }
}