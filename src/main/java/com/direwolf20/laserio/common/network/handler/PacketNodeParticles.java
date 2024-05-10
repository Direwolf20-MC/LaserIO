package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.network.data.NodeParticlesPayload;
import com.direwolf20.laserio.util.ParticleData;
import com.direwolf20.laserio.util.ParticleRenderData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class PacketNodeParticles {
    public static final PacketNodeParticles INSTANCE = new PacketNodeParticles();

    public static PacketNodeParticles get() {
        return INSTANCE;
    }

    public void handle(final NodeParticlesPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            List<ParticleData> tempList = payload.particleList();

            for (ParticleData data : tempList) {
                //Extract
                if (data.fromData != null) {
                    GlobalPos fromPos = data.fromData.node();
                    BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos.pos());
                    if (!(fromTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) fromTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, fromPos.pos().relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node().pos(), data.fromData.position()));
                    }
                }
                if (data.toData != null) {
                    //Insert
                    GlobalPos toPos = data.toData.node();
                    BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos.pos());
                    if (!(toTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) toTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, data.toData.node().pos(), data.toData.direction(), toPos.pos().relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                    }
                }
            }
        });
    }
}
