package com.direwolf20.laserio.common.containers.customslot;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FilterBasicSlot extends SlotItemHandler {

    protected boolean enabled = true;
    public boolean isCount;

    public FilterBasicSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean isCount) {
        super(itemHandler, index, xPosition, yPosition);
        this.isCount = isCount;
    }

    @Override
    public int getMaxStackSize() {
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
