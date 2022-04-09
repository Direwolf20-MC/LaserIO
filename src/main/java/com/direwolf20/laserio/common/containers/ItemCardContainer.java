package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemCardContainer extends AbstractContainerMenu {
    public static final int SLOTS = 3;
    public CardItemHandler handler;
    public ItemStack cardItem;
    public Player playerEntity;
    private IItemHandler playerInventory;

    public ItemCardContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, new CardItemHandler(3, ItemStack.EMPTY), ItemStack.EMPTY);
        cardItem = extraData.readItem();
    }

    public ItemCardContainer(int windowId, Inventory playerInventory, Player player, CardItemHandler handler, ItemStack cardItem) {
        super(Registration.ItemCard_Container.get(), windowId);
        playerEntity = player;
        this.handler = handler;
        this.playerInventory = new InvWrapper(playerInventory);
        this.cardItem = cardItem;
        if (handler != null)
            addSlotRange(handler, 0, 62, 35, 3, 18);

        layoutPlayerInventorySlots(8, 84);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return playerIn.getMainHandItem().equals(cardItem); //TODO Validate this and check offhand?
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            //If its one of the 3 slots at the top try to move it into your inventory
            if (index <= 2) {
                if (!this.moveItemStackTo(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                if (stack.getItem() instanceof BaseCard) {
                    if (!this.moveItemStackTo(stack, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 30) {
                    if (!this.moveItemStackTo(stack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 39 && !this.moveItemStackTo(stack, 3, 30, false)) {
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
