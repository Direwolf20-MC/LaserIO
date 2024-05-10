package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.network.data.ChangeColorPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.*;

public class PacketChangeColor {
    public static final PacketChangeColor INSTANCE = new PacketChangeColor();

    public static PacketChangeColor get() {
        return INSTANCE;
    }

    public void handle(final ChangeColorPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();


            BlockEntity blockEntity = sender.level().getBlockEntity(payload.sourcePos());
            if (blockEntity instanceof LaserNodeBE laserNodeBE) {
                laserNodeBE.setColor(new Color(payload.color(), true), payload.wrenchAlpha());
                laserNodeBE.discoverAllNodes();
            }
        });
    }
}
