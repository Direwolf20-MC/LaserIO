package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class FilterCountHandler extends FilterBasicHandler {

    public FilterCountHandler(int size, ItemStack itemStack) {
        super(size, itemStack);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return super.isValid(index, resource);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return 1;
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        return FilterCount.getSlotCount(this.stack, index);
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        // The underlying ItemContainerContents stores count=1; the true amount is stored
        // out-of-band in FILTER_COUNT_SLOT_COUNTS on the filter card.
        super.set(index, resource, Math.min(amount, 1));
        FilterCount.setSlotCount(this.stack, index, amount);
    }

    public void setMBAmountInSlot(int slot, int mbAmt) {
        if (mbAmt == -1) return; //Shouldn't happen unless i done did goofed
        FilterCount.setSlotAmount(this.stack, slot, mbAmt);
    }

    public void syncSlots() {
        for (int i = 0; i < this.size(); i++) {
            if (FilterCount.getSlotAmount(this.stack, i) == 0) {
                ItemResource slotResource = this.getResource(i);
                int slotCount = slotResource.isEmpty() ? 0 : 1;
                FilterCount.setSlotCount(this.stack, i, slotCount);
            }
        }
    }
}
