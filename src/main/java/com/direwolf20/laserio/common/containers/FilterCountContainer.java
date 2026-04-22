package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public class FilterCountContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public FilterCountHandler handler;
    public ItemStack filterItem;
    public Player playerEntity;
    public ItemStack sourceCard = ItemStack.EMPTY;
    private Inventory playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
        this.sourceCard = ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData);
    }

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, ItemStack filterItem) {
        super(Registration.FilterCount_Container.get(), windowId);
        playerEntity = player;
        this.handler = FilterCount.getInventory(filterItem);
        this.playerInventory = playerInventory;
        this.filterItem = filterItem;
        if (handler != null)
            addSlotBox(handler, 0, 44, 22, 5, 18, 3, 18);

        layoutPlayerInventorySlots(8, 84);
    }

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack filterItem, ItemStack sourceCard) {
        this(windowId, playerInventory, player, filterItem);
        this.sourceContainer = sourcePos;
        this.sourceCard = sourceCard;
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public int getStackSize(int slot) {
        ItemStack filterStack = handler.stack;
        if (slot >= 0 && slot < SLOTS && (slots.get(slot) instanceof FilterBasicSlot)) {
            return FilterCount.getSlotCount(filterStack, slot);
        }
        return 0;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem().copy();
            if (ItemStack.isSameItemSameComponents(currentStack, filterItem)) return ItemStack.EMPTY;
            //Only do this if we click from the players inventory
            if (index >= SLOTS) {
                for (int i = 0; i < SLOTS; i++) { //Prevents the same item from going in there more than once.
                    if (ItemStack.isSameItemSameComponents(this.slots.get(i).getItem(), currentStack)) //Don't limit tags
                        return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(currentStack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
                handler.syncSlots();
            }
        }

        return itemstack;
    }

    private int addSlotRange(FilterCountHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new FilterBasicSlot(handler, handler::set, index, x, y, true));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(FilterCountHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
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
        Level world = playerIn.level();
        if (!world.isClientSide()) {
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE)
                    ((LaserNodeBE) blockEntity).updateThisNode();

            }
        }
        super.removed(playerIn);
    }
}
