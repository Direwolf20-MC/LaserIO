package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class LaserNodeSlot extends SlotItemHandler {
    public LaserNodeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return (stack.getItem() instanceof BaseCard);
    }
}
