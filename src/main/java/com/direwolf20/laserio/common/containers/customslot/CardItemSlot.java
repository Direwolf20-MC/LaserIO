package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class CardItemSlot extends ResourceHandlerSlot {
    CardItemContainer parentContainer;
    protected boolean enabled = true;

    public CardItemSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, CardItemContainer cardItemContainer, int index, int xPosition, int yPosition) {
        super(handler, slotModifier, index, xPosition, yPosition);
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
