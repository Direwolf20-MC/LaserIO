package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CardItemHandler extends DataComponentHandler {
    public ItemStack stack;

    public CardItemHandler(int size, ItemStack itemStack) {
        super(itemStack, size);
        this.stack = itemStack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (this.stack.getItem() instanceof CardEnergy)
            return stack.getItem() instanceof OverclockerCard;
        if (slot == 0)
            return stack.getItem() instanceof BaseFilter;
        return stack.getItem() instanceof OverclockerCard;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (this.stack.getItem() instanceof CardEnergy)
            return 4;
        if (slot == 0)
            return 1;
        return 4;
    }
}
