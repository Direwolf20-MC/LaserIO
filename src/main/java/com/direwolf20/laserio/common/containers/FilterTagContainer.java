package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;


public class FilterTagContainer extends AbstractContainerMenu {
    public static final int SLOTS = 1;
    public FilterBasicHandler handler;
    public ItemStack filterItem;
    public ItemStack sourceCard = ItemStack.EMPTY;
    public Player playerEntity;
    private IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;

    public FilterTagContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
        this.sourceCard = ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData);
    }

    public FilterTagContainer(int windowId, Inventory playerInventory, Player player, ItemStack filterItem) {
        super(Registration.FilterTag_Container.get(), windowId);
        playerEntity = player;
        this.handler = new FilterBasicHandler(SLOTS, filterItem);
        this.playerInventory = new InvWrapper(playerInventory);
        this.filterItem = filterItem;
        if (handler != null)
            addSlotRange(handler, 0, 177, 6, 1, 18);

        layoutPlayerInventorySlots(8, 172);
    }

    public FilterTagContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack filterItem, ItemStack sourceCard) {
        this(windowId, playerInventory, player, filterItem);
        this.sourceContainer = sourcePos;
        this.sourceCard = sourceCard;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS) {
            //System.out.println("Skipping!");
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
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
                for (int i = 0; i < SLOTS; i++) { //Loop through slots
                    handler.setStackInSlot(i, ItemStack.EMPTY); //Clear the current slots
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
            if (handler instanceof FilterBasicHandler)
                addSlot(new FilterBasicSlot(handler, index, x, y, false));
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
        Level world = playerIn.level();
        if (!world.isClientSide) {
            handler.setStackInSlot(0, ItemStack.EMPTY); //Clear the current slot
            if (sourceCard != null && !sourceCard.isEmpty()) {
                CardItemHandler cardItemHandler = BaseCard.getInventory(sourceCard);
                cardItemHandler.setStackInSlot(0, filterItem);
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