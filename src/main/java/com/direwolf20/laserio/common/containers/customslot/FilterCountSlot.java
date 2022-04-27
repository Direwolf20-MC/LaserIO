package com.direwolf20.laserio.common.containers.customslot;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FilterCountSlot extends SlotItemHandler {
    public FilterCountSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
