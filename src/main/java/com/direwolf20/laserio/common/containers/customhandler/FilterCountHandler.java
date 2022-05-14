package com.direwolf20.laserio.common.containers.customhandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FilterCountHandler extends FilterBasicHandler {

    public FilterCountHandler(int size, ItemStack itemStack) {
        super(size, itemStack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        /*if (!stack.equals(ItemStack.EMPTY))
            FilterCount.setInventory(stack, this);*/
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
        if (stack.getCount() > 0) {
            validateSlotIndex(slot);
            System.out.println(stack);
            CompoundTag compound = this.stack.getOrCreateTag();
            ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
            for (int i = 0; i < countList.size(); i++) {
                CompoundTag countTag = countList.getCompound(i);
                int tagslot = countTag.getInt("Slot");
                if (tagslot == slot) {
                    countTag.putInt("Count", stack.getCount());
                    break;
                }
            }
            this.stack.getOrCreateTag().put("counts", countList);
            System.out.println(this.stack);
            stack.setCount(1);
        }
        this.stacks.set(slot, stack);
        this.stack.getOrCreateTag().put("inv", this.serializeNBT());
        System.out.println(stack);
        onContentsChanged(slot);
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
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
        return this.stacks.get(slot);
    }

    public ItemStack getStackInSlot(int slot, boolean fromTag) {
        validateSlotIndex(slot);
        if (fromTag)
            return getStackInSlot(slot);
        return this.stacks.get(slot);
    }
}
