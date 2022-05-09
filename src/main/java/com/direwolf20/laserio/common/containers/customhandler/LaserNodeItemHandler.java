package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class LaserNodeItemHandler extends ItemStackHandler {
    LaserNodeBE blockEntity;

    public LaserNodeItemHandler(int size) {
        super(size);
    }

    public LaserNodeItemHandler(int size, LaserNodeBE blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onContentsChanged(int slot) {
        // To make sure the TE persists when the chunk is saved later we need to
        // mark it dirty every time the item handler changes
        if (blockEntity == null) return;
        blockEntity.updateThisNode();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot == 9)
            return stack.getItem() instanceof OverclockerNode;
        return stack.getItem() instanceof BaseCard;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        /*if (slot < LaserNodeContainer.CARDSLOTS && !(stack.getItem() instanceof BaseCard))
            return stack;*/
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == 9)
            return 8;
        return 1;
    }

    public void reSize(int size) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++)
            newStacks.set(i, stacks.get(i));
        stacks = newStacks;
    }
}
