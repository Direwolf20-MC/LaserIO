package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CardOverclockSlot extends SlotItemHandler {
    protected boolean enabled = true;

    public CardOverclockSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
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