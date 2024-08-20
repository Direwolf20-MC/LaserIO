package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CardItemSlot extends SlotItemHandler {
    CardItemContainer parentContainer;
    protected boolean enabled = true;

    public CardItemSlot(IItemHandler itemHandler, CardItemContainer cardItemContainer, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.parentContainer = cardItemContainer;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return (stack.getItem() instanceof BaseFilter);
    }

    @Override
    public void setChanged() {
        super.setChanged();
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