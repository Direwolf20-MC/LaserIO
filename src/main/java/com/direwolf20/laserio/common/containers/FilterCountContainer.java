package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class FilterCountContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public FilterCountHandler handler;
    public ItemStack filterItem;
    public Player playerEntity;
    public ItemStack sourceCard = ItemStack.EMPTY;
    private IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, extraData.readItem());
        this.sourceCard = extraData.readItem();
    }

    public FilterCountContainer(int windowId, Inventory playerInventory, Player player, ItemStack filterItem) {
        super(Registration.FilterCount_Container.get(), windowId);
        playerEntity = player;
        this.handler = FilterCount.getInventory(filterItem);
        this.playerInventory = new InvWrapper(playerInventory);
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
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS) {
            //System.out.println("Skipping!");
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
            if (ItemHandlerHelper.canItemStacksStack(currentStack, filterItem)) return ItemStack.EMPTY;
            //Only do this if we click from the players inventory
            if (index >= SLOTS) {
                for (int i = 0; i < SLOTS; i++) { //Prevents the same item from going in there more than once.
                    if (ItemHandlerHelper.canItemStacksStack(this.slots.get(i).getItem(), currentStack)) //Don't limit tags
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

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (handler instanceof FilterCountHandler)
                addSlot(new FilterBasicSlot(handler, index, x, y, true));
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
            if (!sourceCard.equals(ItemStack.EMPTY)) { //Workaround to the card not always saving...
                ItemStack overclockerStack = BaseCard.getInventory(sourceCard).getStackInSlot(1);
                CardItemHandler cardHandler = new CardItemHandler(CardItemContainer.SLOTS, sourceCard);
                cardHandler.setStackInSlot(0, filterItem);
                cardHandler.setStackInSlot(1, overclockerStack);
                BaseCard.setInventory(sourceCard, cardHandler);
            }
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE)
                    ((LaserNodeBE) blockEntity).updateThisNode();

            }
        }
        super.removed(playerIn);
    }
}