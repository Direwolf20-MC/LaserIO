package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;

import javax.annotation.Nonnull;

public class CardItemHandler extends ComponentItemHandler {
    public ItemStack stack;

    public CardItemHandler(int size, ItemStack itemStack) {
        super(itemStack, LaserIODataComponents.ITEMSTACK_HANDLER.get(), size);
        this.stack = itemStack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return true;
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
