package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.world.item.Item;

public class BaseFilter extends Item {
    public BaseFilter() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }
}
