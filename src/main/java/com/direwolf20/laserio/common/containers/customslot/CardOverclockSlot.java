package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class CardOverclockSlot extends ResourceHandlerSlot {
    protected boolean enabled = true;

    public CardOverclockSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int index, int xPosition, int yPosition) {
        super(handler, slotModifier, index, xPosition, yPosition);
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
