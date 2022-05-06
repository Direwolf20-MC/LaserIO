package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ExtractorCardCache extends BaseCardCache {
    public final byte extractAmt;

    public ExtractorCardCache(Direction direction, ItemStack cardItem, int cardSlot) {
        super(direction, cardItem, cardSlot);
        this.extractAmt = BaseCard.getItemExtractAmt(cardItem);
    }
}
