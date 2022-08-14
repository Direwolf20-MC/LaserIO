package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customslot.CardOverclockSlot;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

public class CardEnergyContainer extends AbstractCardContainer {
    public static final int SLOTS = 1;
    public CardItemHandler handler;
    public Player playerEntity;
    protected IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;

    protected CardEnergyContainer(@Nullable MenuType<?> pMenuType, int pContainerId, ItemStack cardItem) {
        super(pMenuType, pContainerId, cardItem);
    }

    public CardEnergyContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, extraData.readItem());
        this.direction = extraData.readByte();
    }

    public CardEnergyContainer(int windowId, Inventory playerInventory, Player player, ItemStack cardItem) {
        super(Registration.CardEnergy_Container.get(), windowId, cardItem);
        playerEntity = player;
        this.handler = CardEnergy.getInventory(cardItem);
        this.playerInventory = new InvWrapper(playerInventory);
        if (handler != null) {
            //addSlotRange(handler, 0, 80, 5, 1, 18);
            addSlotRange(handler, 0, 153, 5, 1, 18);
            //addSlotBox(filterHandler, 0, 44, 25, 5, 18, 3, 18);
            //toggleFilterSlots();
        }

        layoutPlayerInventorySlots(8, 84);
    }

    public CardEnergyContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack cardItem, byte direction) {
        this(windowId, playerInventory, player, cardItem);
        this.sourceContainer = sourcePos;
        this.direction = direction;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        /*if (slotId >= SLOTS && slotId < SLOTS + FILTERSLOTS) {
            return;
        }*/
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    /*public void getFilterHandler() {
        ItemStack filterStack = slots.get(0).getItem(); //BaseCard.getInventory(cardItem).getStackInSlot(0);
        if (filterStack.getItem() instanceof FilterBasic)
            filterHandler = FilterBasic.getInventory(filterStack);
        else if (filterStack.getItem() instanceof FilterCount)
            filterHandler = FilterCount.getInventory(filterStack);
        else
            filterHandler = new FilterBasicHandler(15, ItemStack.EMPTY);
    }

    public void toggleFilterSlots() {
        getFilterHandler();
        updateFilterSlots(filterHandler, 0, 44, 25, 5, 18, 3, 18);
    }*/

    @Override
    public boolean stillValid(Player playerIn) {
        if (sourceContainer.equals(BlockPos.ZERO))
            return playerIn.getMainHandItem().equals(cardItem) || playerIn.getOffhandItem().equals(cardItem);
        return true;
    }

    /*public int getStackSize(int slot) {
        ItemStack filterStack = filterHandler.stack;
        if (slot >= SLOTS && slot < SLOTS + FILTERSLOTS && (slots.get(slot) instanceof FilterBasicSlot) && filterStack.getItem() instanceof FilterCount) {
            return FilterCount.getSlotCount(filterStack, slot - SLOTS);
        }
        return filterHandler.getStackInSlot(slot - SLOTS).getCount();
    }*/

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (cardItem.getCount() > 1) return ItemStack.EMPTY; // Don't let quickMove happen in multistack cards
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (ItemHandlerHelper.canItemStacksStack(itemstack, cardItem)) return ItemStack.EMPTY;
            //If its one of the 3 slots at the top try to move it into your inventory
            if (index < SLOTS) {
                if (!this.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else { //From player inventory TO something
                ItemStack currentStack = slot.getItem().copy();
                if (slots.get(0).mayPlace(currentStack)) {
                    if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                if (!playerIn.level.isClientSide())
                    CardEnergy.setInventory(cardItem, handler);
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

    /*protected void updateFilterSlots(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            for (int i = 0; i < horAmount; i++) {
                if (handler instanceof CardItemHandler && index == 0) {
                    //System.out.println("This shouldn't happen");
                } else if (handler instanceof FilterBasicHandler) {
                    slots.set(index + SLOTS, new FilterBasicSlot(handler, index, x, y, slots.get(0).getItem().getItem() instanceof FilterCount));
                    slots.get(index + SLOTS).index = index + SLOTS; //Look at container.addSlot() -- it does this
                } else {
                    //System.out.println("This shouldn't happen");
                }
                x += dx;
                index++;
            }
            y += dy;
            x = x - (dx * horAmount);
        }
    }*/

    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (handler instanceof CardItemHandler && index == 0)
                addSlot(new CardOverclockSlot(handler, index, x, y));
            /*else if (handler instanceof FilterBasicHandler)
                addSlot(new FilterBasicSlot(handler, index, x, y, slots.get(0).getItem().getItem() instanceof FilterCount));*/
            else
                addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    protected int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public void removed(Player playerIn) {
        Level world = playerIn.getLevel();
        if (!world.isClientSide) {
            CardEnergy.setInventory(cardItem, handler);
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE)
                    ((LaserNodeBE) blockEntity).updateThisNode();
            }
        }
        super.removed(playerIn);
    }
}
