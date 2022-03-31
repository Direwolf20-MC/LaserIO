package com.direwolf20.laserio.common.blocks.baseblocks;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BaseLaserBlock extends Block {
    public BaseLaserBlock() {
        super(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
        );
    }

    //Temp Code for testing
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!world.isClientSide) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BaseLaserBE) {
                System.out.println(((BaseLaserBE) blockEntity).getConnections());
                System.out.println(((BaseLaserBE) blockEntity).getRenderedConnections());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }
}
