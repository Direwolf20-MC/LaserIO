package com.direwolf20.laserio.util;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ExtractorCardCache extends BaseCardCache {
    public final byte extractAmt;

    public ExtractorCardCache(byte extractAmt, Direction direction, byte channel, ItemStack filterCard) {
        super(direction, channel, filterCard);
        this.extractAmt = extractAmt;
    }
}
