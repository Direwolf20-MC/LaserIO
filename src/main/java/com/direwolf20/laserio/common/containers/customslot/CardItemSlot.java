package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;

import javax.annotation.Nonnull;

public class CardItemSlot extends ItemHandlerCopySlot {
    CardItemContainer parentContainer;
    protected boolean enabled = true;

    public CardItemSlot(IItemHandler itemHandler, CardItemContainer cardItemContainer, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.parentContainer = cardItemContainer;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return true;
        return (stack.getItem() instanceof BaseFilter);
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        super.setStackCopy(stack);
        parentContainer.toggleFilterSlots();
    }

    @Override
    public boolean isActive() {
        return enabled;
    }

    public CardItemSlot setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}