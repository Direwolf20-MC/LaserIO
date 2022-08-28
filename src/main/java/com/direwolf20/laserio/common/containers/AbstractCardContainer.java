package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractCardContainer extends AbstractContainerMenu {

    public final ItemStack cardItem;

    public byte direction = -1;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public Player playerEntity;
    protected IItemHandler playerInventory;

    protected AbstractCardContainer(MenuType<?> pMenuType, int pContainerId, ItemStack cardItem) {
        super(pMenuType, pContainerId);
        this.cardItem = cardItem;
    }

    public Direction getDirection(){
        if (direction == -1)
            return null;
        return LaserNodeContainer.DIRS[direction];
    }
    
    public BlockEntity getBlockFaced(){
        if (sourceContainer.equals(BlockPos.ZERO))
            return null;
        var dir = getDirection();
        if (dir == null)
            return null;
        Level world = playerEntity.getLevel();
        return world.getBlockEntity(sourceContainer.relative(dir));
    }

    public BlockState getBlockStateFaced(){
        if (sourceContainer.equals(BlockPos.ZERO))
            return null;
        var dir = getDirection();
        if (dir == null)
            return null;
        Level world = playerEntity.getLevel();
        return world.getBlockState(sourceContainer.relative(dir));
    }
}
