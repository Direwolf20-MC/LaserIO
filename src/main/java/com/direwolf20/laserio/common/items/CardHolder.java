package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.world.item.Item;

public class CardHolder extends Item {
    public CardHolder() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP)
                .stacksTo(1));
    }
}
