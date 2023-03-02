package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CardItemHandler extends ItemStackHandler {
    public ItemStack stack;

    public CardItemHandler(int size, ItemStack itemStack) {
        super(size);
        this.stack = itemStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!stack.isEmpty())
        {
            BaseCard.setInventory(stack, this);      
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (this.stack.getItem() instanceof CardEnergy)
            return slot == 0 ? stack.getItem() instanceof OverclockerCard : stack.getItem() instanceof OverclockerChannel;
        if (slot == 0)
            return stack.getItem() instanceof BaseFilter;
        if (slot == 2)
        	return stack.getItem() instanceof OverclockerChannel;
        return stack.getItem() instanceof OverclockerCard;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (this.stack.getItem() instanceof CardEnergy)
            return slot == 0 ? 4 : 1;
        if (slot == 0 || slot == 2)
            return 1;
        return 4;
    }

    public void reSize(int size) {
        NonNullList<ItemStack> newStacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++)
            newStacks.set(i, stacks.get(i));
        stacks = newStacks;
    }
    
    public ItemStack getChannelOverclocker() {
    	if(this.stack.getItem() instanceof CardEnergy) 
    		return this.getStackInSlot(1);
    	else
    		return this.getStackInSlot(2);
    }
    
    public boolean hasChannelOverclocker() {
    	return getChannelOverclocker().getItem() instanceof OverclockerChannel;
    }
}
