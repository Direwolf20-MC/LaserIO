package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.network.data.NodeParticlesFluidPayload;
import com.direwolf20.laserio.util.ParticleDataFluid;
import com.direwolf20.laserio.util.ParticleRenderDataFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class PacketNodeParticlesFluid {
    public static final PacketNodeParticlesFluid INSTANCE = new PacketNodeParticlesFluid();

    public static PacketNodeParticlesFluid get() {
        return INSTANCE;
    }

    public void handle(final NodeParticlesFluidPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            List<ParticleDataFluid> tempList = payload.particleList();
            for (ParticleDataFluid data : tempList) {
                //Extract
                if (data.fromData != null) {
                    GlobalPos fromPos = data.fromData.node();
                    BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos.pos());
                    if (!(fromTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) fromTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, fromPos.pos().relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node().pos(), data.fromData.position()));
                    }
                }
                if (data.toData != null) {
                    //Insert
                    GlobalPos toPos = data.toData.node();
                    BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos.pos());
                    if (!(toTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) toTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, data.toData.node().pos(), data.toData.direction(), toPos.pos().relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                    }
                }
            }
        });
    }
}
