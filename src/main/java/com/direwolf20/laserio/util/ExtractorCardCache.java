package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ExtractorCardCache extends BaseCardCache {
    public final byte extractAmt;
    public final int tickSpeed;
    public int remainingSleep;

    public ExtractorCardCache(Direction direction, ItemStack cardItem, int cardSlot) {
        super(direction, cardItem, cardSlot);
        this.extractAmt = BaseCard.getItemExtractAmt(cardItem);
        this.tickSpeed = BaseCard.getItemExtractSpeed(cardItem);
    }

    public int getRemainingSleep() {
        return remainingSleep;
    }

    public void setRemainingSleep(int sleep) {
        remainingSleep = sleep;
    }

    public int decrementSleep() {
        remainingSleep--;
        if (remainingSleep <= 0) {
            remainingSleep = tickSpeed;
            return 0;
        }
        return remainingSleep;
    }
}
