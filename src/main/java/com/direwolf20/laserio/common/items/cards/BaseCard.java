package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.world.item.Item;

public class BaseCard extends Item {
    protected BaseCard.CardType CARDTYPE;

    public enum CardType {
        ITEM,
        FLUID,
        ENERGY
    }

    public BaseCard() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    public CardType getCardType() {
        return CARDTYPE;
    }
}
