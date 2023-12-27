package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class SpecifyQuantityContainer extends AbstractContainerMenu {
    public static final int SLOTS = 1;
    public ItemStack filterItem;
    public Player playerEntity;
    public ItemStack sourceCard = ItemStack.EMPTY;
    public BlockPos sourceContainer = BlockPos.ZERO;

    public SpecifyQuantityContainer(int windowId, Player player, ItemStack filterItem, int quantity) {
        super(null, windowId);
        this.playerEntity = player;
        this.filterItem = filterItem;

        ItemStackHandler inventory = new ItemStackHandler(1);
        inventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(filterItem, 1));

        addSlot(new FilterBasicSlot(inventory, 0, 89, 48, false));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS) {
            //System.out.println("Skipping!");
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player playerIn) { //Todo see if we can send the player back to their last container screen?
        Level world = playerIn.level();
        if (!world.isClientSide) {
            if (!sourceCard.isEmpty()) { //Workaround to the card not always saving...
                ItemStack overclockerStack = BaseCard.getInventory(sourceCard).getStackInSlot(1);
                CardItemHandler cardHandler = new CardItemHandler(CardItemContainer.SLOTS, sourceCard);
                cardHandler.setStackInSlot(0, filterItem);
                cardHandler.setStackInSlot(1, overclockerStack);
                BaseCard.setInventory(sourceCard, cardHandler);
            }
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE)
                    ((LaserNodeBE) blockEntity).updateThisNode();

            }
        }
        super.removed(playerIn);
    }
}