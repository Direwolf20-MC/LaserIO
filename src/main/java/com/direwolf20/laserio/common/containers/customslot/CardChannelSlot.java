package com.direwolf20.laserio.common.containers.customslot;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CardChannelSlot extends SlotItemHandler {

	AbstractContainerMenu parentContainer;
    protected boolean enabled = true;

    public CardChannelSlot(IItemHandler itemHandler, AbstractContainerMenu cardContainer, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.parentContainer = cardContainer;
    }
    
    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return (stack.getItem() instanceof OverclockerChannel);
    }

    @Override
    public void setChanged() {
    	super.setChanged();
    	if(parentContainer instanceof CardItemContainer)
    		if(((CardItemContainer)parentContainer).currentScreen != null) {	
    			((CardItemContainer)parentContainer).currentScreen.updateChannel();
        }
    	if(parentContainer instanceof CardEnergyContainer)
            if(((CardEnergyContainer)parentContainer).currentScreen != null) {	
            	((CardEnergyContainer)parentContainer).currentScreen.updateChannel();
            }
    }

    
    
    @Override
    public boolean isActive() {
        return enabled;
    }

    public CardChannelSlot setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
