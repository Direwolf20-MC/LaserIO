package com.direwolf20.laserio.common.blocks;

import javax.annotation.Nullable;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorAdv extends LaserConnector implements EntityBlock {

    public LaserConnectorAdv() {
        super();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserConnectorAdvBE(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be != null && be instanceof LaserConnectorAdvBE laserConnectorAdvBE) {
                laserConnectorAdvBE.disconnectAllNodes();
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

}