package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class ExtractorCardCache extends BaseCardCache {
    public final byte extractAmt;
    public final int tickSpeed;
    public int remainingSleep;
    public boolean exact;
    public boolean roundRobin;

    public ExtractorCardCache(Direction direction, ItemStack cardItem, int cardSlot, LaserNodeBE be) {
        super(direction, cardItem, cardSlot, be);
        this.extractAmt = BaseCard.getItemExtractAmt(cardItem);
        this.tickSpeed = BaseCard.getItemExtractSpeed(cardItem);
        this.exact = BaseCard.getExact(cardItem);
        this.roundRobin = BaseCard.getRoundRobin(cardItem);
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
