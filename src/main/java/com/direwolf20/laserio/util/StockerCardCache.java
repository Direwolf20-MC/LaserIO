package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class StockerCardCache extends ExtractorCardCache {
    public boolean regulate;

    public StockerCardCache(Direction direction, ItemStack cardItem, int cardSlot) {
        super(direction, cardItem, cardSlot);
        this.regulate = BaseCard.getRegulate(cardItem);
    }
}
