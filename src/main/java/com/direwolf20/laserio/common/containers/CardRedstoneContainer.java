package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.setup.LaserIORegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public class CardRedstoneContainer extends AbstractContainerMenu {
    public static final int SLOTS = 0;
    public Player playerEntity;
    private Inventory playerInventory;
    public ItemStack cardItem;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public byte direction = -1;

    public CardRedstoneContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
        this.direction = extraData.readByte();
    }

    public CardRedstoneContainer(int windowId, Inventory playerInventory, Player player, ItemStack cardItem) {
        super(LaserIORegistration.CardRedstone_Container.get(), windowId);
        playerEntity = player;
        this.playerInventory = playerInventory;
        this.cardItem = cardItem;
        layoutPlayerInventorySlots(8, 84);
    }

    public CardRedstoneContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack cardItem, byte direction) {
        this(windowId, playerInventory, player, cardItem);
        this.sourceContainer = sourcePos;
        this.direction = direction;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // No Op
        return ItemStack.EMPTY;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        // Hotbar
        topRow += 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, leftCol + col * 18, topRow));
        }
    }

    @Override
    public void removed(Player playerIn) {
        Level world = playerIn.level();
        if (!sourceContainer.equals(BlockPos.ZERO)) {
            BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
            if (blockEntity instanceof LaserNodeBE)
                ((LaserNodeBE) blockEntity).updateThisNode();
        }
        super.removed(playerIn);
    }
}
