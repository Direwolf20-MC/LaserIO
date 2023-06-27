package com.direwolf20.laserio.common.blocks;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class LaserConnectorAdv extends LaserConnector implements EntityBlock {
    public LaserConnectorAdv() {
        super();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserConnectorAdvBE(pos, state);
    }


    //TODO Temp Code for testing
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!world.isClientSide) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LaserConnectorAdvBE baseLaserBE) {
                System.out.println("Partner Is: " + baseLaserBE.getPartnerBlockPos());
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, result);
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
