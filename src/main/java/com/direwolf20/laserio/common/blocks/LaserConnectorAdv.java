package com.direwolf20.laserio.common.blocks;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class LaserConnectorAdv extends LaserConnector implements EntityBlock {
    public LaserConnectorAdv(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserConnectorAdvBE(pos, state);
    }
}
