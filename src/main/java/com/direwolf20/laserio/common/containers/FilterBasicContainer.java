package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
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

public class FilterBasicContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public FilterBasicHandler handler;
    public ItemStack filterItem;
    public ItemStack sourceCard = ItemStack.EMPTY;
    public Player playerEntity;
    private IItemHandler playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, new FilterBasicHandler(SLOTS, ItemStack.EMPTY), ItemStack.EMPTY);
        filterItem = extraData.readItem();
        this.sourceCard = extraData.readItem();
    }

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, FilterBasicHandler handler, ItemStack filterItem) {
        super(Registration.FilterBasic_Container.get(), windowId);
        playerEntity = player;
        this.handler = handler;
        this.playerInventory = new InvWrapper(playerInventory);
        this.filterItem = filterItem;
        if (handler != null)
            addSlotBox(handler, 0, 44, 22, 5, 18, 3, 18);

        layoutPlayerInventorySlots(8, 84);
    }

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, FilterBasicHandler handler, BlockPos sourcePos, ItemStack filterItem, ItemStack sourceCard) {
        this(windowId, playerInventory, player, handler, filterItem);
        this.sourceContainer = sourcePos;
        this.sourceCard = sourceCard;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
        //return playerIn.getMainHandItem().equals(cardItem); //TODO Validate this and check offhand?
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
            if (ItemHandlerHelper.canItemStacksStack(currentStack, filterItem)) return ItemStack.EMPTY;
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