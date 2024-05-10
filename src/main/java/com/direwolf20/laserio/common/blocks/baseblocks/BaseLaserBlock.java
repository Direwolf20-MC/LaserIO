package com.direwolf20.laserio.common.blocks.baseblocks;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BaseLaserBlock extends Block {
    public BaseLaserBlock() {
        super(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .forceSolidOn()
        );
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            BlockEntity be = worldIn.getBlockEntity(pos);
            if (be != null && be instanceof BaseLaserBE) {
                ((BaseLaserBE) be).disconnectAllNodes();
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
