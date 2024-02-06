package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CardHolderSlot extends SlotItemHandler {
    protected boolean enabled = true;

    public CardHolderSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return (stack.getItem() instanceof BaseCard || stack.getItem() instanceof BaseFilter || stack.getItem() instanceof OverclockerCard || stack.getItem() instanceof OverclockerNode);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
        //return super.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 64;
        //return super.getMaxStackSize(stack);
    }

    @Override
    public boolean isActive() {
        return enabled;
    }

    public CardHolderSlot setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
