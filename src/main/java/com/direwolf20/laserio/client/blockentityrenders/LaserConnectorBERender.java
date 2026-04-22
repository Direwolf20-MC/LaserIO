package com.direwolf20.laserio.client.blockentityrenders;

import com.direwolf20.laserio.client.blockentityrenders.baseberender.BaseLaserBERender;
import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LaserConnectorBERender extends BaseLaserBERender<LaserConnectorBE, BaseLaserBERender.BaseLaserRenderState> {
    public LaserConnectorBERender(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
