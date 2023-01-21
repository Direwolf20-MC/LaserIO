package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class SensorCardCache extends ExtractorCardCache {

    public BaseCard.SensorMode sensorMode;

    public SensorCardCache(Direction direction, ItemStack cardItem, int cardSlot, LaserNodeBE be) {
        super(direction, cardItem, cardSlot, be);
        this.sensorMode = BaseCard.getNamedSensorMode(cardItem);
    }
}
