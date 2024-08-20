package com.direwolf20.laserio.common.containers.customhandler;

import javax.annotation.Nonnull;

import com.direwolf20.laserio.common.items.filters.FilterBasic;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class FilterBasicHandler extends ItemStackHandler {
    public ItemStack stack;

    public FilterBasicHandler(int size, ItemStack itemStack) {
        super(size);
        this.stack = itemStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!stack.isEmpty())
            FilterBasic.setInventory(stack, this);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

}