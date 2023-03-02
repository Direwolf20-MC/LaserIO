package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;
import com.direwolf20.laserio.setup.Registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class OverclockerChannelContainer extends AbstractContainerMenu{

	public ItemStack overclockerItem;
	public Player playerEntity;
	protected IItemHandler playerInventory;
	
	protected OverclockerChannelContainer(MenuType<?> pMenuType, int pContainerId) {
		super(pMenuType, pContainerId);
	}
	
	public OverclockerChannelContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
	     this(windowId, playerInventory, player, extraData.readItem());
	}
	
	public OverclockerChannelContainer(int windowId, Inventory playerInventory, Player player, ItemStack overclockerItem) {
        super(Registration.OverclockerChannel_Container.get(), windowId);
        this.playerEntity = player;
        this.overclockerItem = overclockerItem;
        this.playerInventory = new InvWrapper(playerInventory);
        		
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return playerIn.getMainHandItem().equals(overclockerItem) || playerIn.getOffhandItem().equals(overclockerItem);	
	}

}