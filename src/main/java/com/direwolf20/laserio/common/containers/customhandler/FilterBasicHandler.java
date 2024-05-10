package com.direwolf20.laserio.common.containers.customhandler;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FilterBasicHandler extends DataComponentHandler {
    public ItemStack stack;

    public FilterBasicHandler(int size, ItemStack itemStack) {
        super(itemStack, size);
        this.stack = itemStack;
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
