package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CardItemHandler extends ItemStackHandler {
    public ItemStack stack;

    public CardItemHandler(int size, ItemStack itemStack) {
        super(size);
        this.stack = itemStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        BaseCard.setInventory(stack, this);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot == 0)
            return stack.getItem() instanceof BaseFilter;
        return stack.getItem() instanceof OverclockerCard;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == 0)
            return 1;
        return 4;
    }

    public void reSize(int size) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++)
            newStacks.set(i, stacks.get(i));
        stacks = newStacks;
    }
}
