package com.direwolf20.laserio.common.containers.customslot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;


public class FilterBasicSlot extends ResourceHandlerSlot {

    protected boolean enabled = true;
    public boolean isCount;

    public FilterBasicSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int index, int xPosition, int yPosition, boolean isCount) {
        super(handler, slotModifier, index, xPosition, yPosition);
        this.isCount = isCount;
    }

    @Override
    public int getMaxStackSize() {
        return isCount ? Integer.MAX_VALUE : 1;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return isCount ? Integer.MAX_VALUE : 1;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean isActive() {
        return enabled;
    }

    public FilterBasicSlot setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
