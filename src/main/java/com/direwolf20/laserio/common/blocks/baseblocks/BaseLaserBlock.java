package com.direwolf20.laserio.common.blocks.baseblocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BaseLaserBlock extends Block {
    public BaseLaserBlock(Properties properties) {
        super(properties);
    }

    public static Properties defaultProperties() {
        return Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .forceSolidOn();
    }
}
