package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FilterCountHandler extends FilterBasicHandler {

    public FilterCountHandler(int size, ItemStack itemStack) {
        super(size, itemStack);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack returnStack = super.getStackInSlot(slot);
        int amt = FilterCount.getSlotCount(this.stack, slot);
        if (amt != returnStack.getCount())
            returnStack.setCount(amt);
        return returnStack;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        ItemStack stackCopy = stack.copy();
        int amt = stackCopy.getCount();
        stackCopy.setCount(1);
        super.setStackInSlot(slot, stackCopy);
        FilterCount.setSlotCount(this.stack, slot, amt);
    }

    public void setMBAmountInSlot(int slot, int mbAmt) {
        if (mbAmt == -1) return; //Shouldn't happen unless i done did goofed
        FilterCount.setSlotAmount(this.stack, slot, mbAmt);
    }

    public void syncSlots() {
        for (int i = 0; i < this.getSlots(); i++) {
            if (FilterCount.getSlotAmount(this.stack, i) == 0)
                FilterCount.setSlotCount(this.stack, i, this.getStackInSlot(i).getCount());
        }
    }
}
