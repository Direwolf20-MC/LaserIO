package com.direwolf20.laserio.common.blocks.baseblocks;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BaseLaserBlock extends Block {
    public BaseLaserBlock() {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
        );
    }

    //Temp Code for testing
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        /*if (!world.isClientSide) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BaseLaserBE) {
                System.out.println("Connections: " + ((BaseLaserBE) blockEntity).getConnections());
                System.out.println("RenderedConnections: " + ((BaseLaserBE) blockEntity).getRenderedConnections());
            }
            return InteractionResult.SUCCESS;
        }*/
        return super.use(state, world, pos, player, hand, result);
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
