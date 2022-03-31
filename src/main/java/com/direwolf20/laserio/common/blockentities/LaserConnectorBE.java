package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorBE extends BaseLaserBE {
    public LaserConnectorBE(BlockPos pos, BlockState state) {
        super(Registration.LaserConnector_BE.get(), pos, state);
    }
}
