package com.direwolf20.laserio.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InserterCardCache extends BaseCardCache {
    public final BlockPos relativePos;

    public InserterCardCache(BlockPos relativePos, Direction direction, byte channel, ItemStack filterCard, int cardSlot) {
        super(direction, channel, filterCard, cardSlot);
        this.relativePos = relativePos;
    }
}
