package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;

import javax.annotation.Nonnull;

public class CardOverclockSlot extends ItemHandlerCopySlot {
    protected boolean enabled = true;

    public CardOverclockSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return true;
        return (stack.getItem() instanceof OverclockerCard);
    }

    @Override
    public boolean isActive() {
        return enabled;
    }

    public CardOverclockSlot setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}