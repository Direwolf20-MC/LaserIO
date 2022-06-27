package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FilterCountHandler extends FilterBasicHandler {

    public FilterCountHandler(int size, ItemStack itemStack) {
        super(size, itemStack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        //if (!stack.equals(ItemStack.EMPTY))
        //    FilterCount.setInventory(stack, this);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public void setStackInSlotSave(int slot, @Nonnull ItemStack stack) {
        if (this.getStackInSlot(slot).isEmpty()) {
            this.setStackInSlot(slot, stack);
            FilterCount.setInventory(this.stack, this);
        } else {
            this.setStackInSlot(slot, stack);
            FilterCount.setSlotCount(this.stack, slot, stack.getCount());
            if (stack.isEmpty())
                FilterCount.setInventory(this.stack, this);
        }
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
        FilterCount.setInventory(this.stack, this);
    }
}
