package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorAdvBE extends BaseLaserBE {
    public LaserConnectorAdvBE(BlockPos pos, BlockState state) {
        super(Registration.LaserConnectorAdv_BE.get(), pos, state);
    }
}
