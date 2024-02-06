package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CardHolderItemStackHandler extends ItemStackHandler {
    public CardHolderItemStackHandler() {
        this(1);
    }

    public CardHolderItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public CardHolderItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return (stack.getItem() instanceof BaseCard || stack.getItem() instanceof BaseFilter || stack.getItem() instanceof OverclockerCard || stack.getItem() instanceof OverclockerNode);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return 64;
    }
}
