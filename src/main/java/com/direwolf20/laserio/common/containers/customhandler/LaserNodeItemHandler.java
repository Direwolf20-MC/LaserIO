package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class LaserNodeItemHandler extends ItemStacksResourceHandler {
    LaserNodeBE blockEntity;

    public LaserNodeItemHandler(int size) {
        super(size);
    }

    public LaserNodeItemHandler(int size, LaserNodeBE blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onContentsChanged(int slot, ItemStack previous) {
        // To make sure the TE persists when the chunk is saved later we need to
        // mark it dirty every time the item handler changes.
        // Client-side menus create dummy handlers with null blockEntity; null-guard.
        if (blockEntity == null) return;
        blockEntity.updateThisNode();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) return true;
        if (index == 9)
            return resource.getItem() instanceof OverclockerNode;
        return resource.getItem() instanceof BaseCard;
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        if (index == 9)
            return 8;
        return 1;
    }

    public void reSize(int size) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(stacks.size(), size); i++)
            newStacks.set(i, stacks.get(i));
        setStacks(newStacks);
    }

    // Convenience wrappers to keep legacy call sites in LaserNodeBE concise
    public int getSlots() {
        return size();
    }

    public ItemStack getStackInSlot(int slot) {
        return getResource(slot).toStack(getAmountAsInt(slot));
    }
}
