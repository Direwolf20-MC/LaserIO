package com.direwolf20.laserio.common.containers;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractCardContainer extends AbstractContainerMenu {

    public final ItemStack cardItem;

    public byte direction = -1;

    protected AbstractCardContainer(MenuType<?> pMenuType, int pContainerId, ItemStack cardItem) {
        super(pMenuType, pContainerId);
        this.cardItem = cardItem;
    }
    
}
