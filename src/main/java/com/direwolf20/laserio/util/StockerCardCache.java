package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class StockerCardCache extends ExtractorCardCache {
    public boolean regulate;

    public StockerCardCache(Direction direction, ItemStack cardItem, int cardSlot, LaserNodeBE be) {
        super(direction, cardItem, cardSlot, be);
        this.regulate = BaseCard.getRegulate(cardItem);
    }
}
