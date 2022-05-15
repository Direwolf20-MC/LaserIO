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

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        //System.out.println(stack);
        //FilterCount.setStackInSlot(this.stack, stack, slot);
        //System.out.println(stack);
        super.setStackInSlot(slot, stack);
        //FilterCount.setSlotCount(this.stack, slot, stack.getCount());
        //FilterCount.setInventory(this.stack, this);
        //System.out.println(stack);
        //FilterCount.setInventory(this.stack, this);
        //System.out.println(stack);
        /*System.out.println("Set slot: " + slot + ": " + stack);
        if (stack.getCount() > 0 && this.stack != null && this.stack.getItem() instanceof FilterCount) {
            validateSlotIndex(slot);
            CompoundTag compound = this.stack.getOrCreateTag();
            ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
            boolean foundInList = false;
            for (int i = 0; i < countList.size(); i++) {
                CompoundTag countTag = countList.getCompound(i);
                int tagslot = countTag.getInt("Slot");
                if (tagslot == slot) {
                    countTag.putInt("Count", stack.getCount());
                    foundInList = true;
                    break;
                }
            }
            if (!foundInList) {
                CompoundTag countTag = new CompoundTag();
                countTag.putInt("Slot", slot);
                countTag.putInt("Count", stack.getCount());
                countList.add(countTag);
            }
            this.stack.getOrCreateTag().put("counts", countList);
            stack.setCount(1);
        }
        this.stacks.set(slot, stack);
        this.stack.getOrCreateTag().put("inv", this.serializeNBT());
        System.out.println("Post Set slot: " + slot + ": " + stack);
        onContentsChanged(slot);*/
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack, int amt) {
        //stack.setCount(amt);
        this.setStackInSlot(slot, stack);
        FilterCount.setSlotCount(this.stack, slot, stack.getCount());
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        /*validateSlotIndex(slot);
        CompoundTag compound = stack.getOrCreateTag();
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int tagslot = countTag.getInt("Slot");
            if (tagslot == slot) {
                ItemStack returnStack = this.stacks.get(slot);
                returnStack.setCount(countTag.getInt("Count"));
                return returnStack;
            }
        }
        return this.stacks.get(slot);*/
        //return FilterCount.getStackInSlot(stack, slot);
        return super.getStackInSlot(slot);
    }

    public ItemStack getStackInSlot(int slot, boolean fromTag) {
        validateSlotIndex(slot);
        if (fromTag)
            return getStackInSlot(slot);
        return this.stacks.get(slot);
    }
}
