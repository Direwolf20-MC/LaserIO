package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.network.data.NodeParticlesChemicalPayload;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleRenderDataChemical;
import com.direwolf20.laserio.util.DimBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

public class PacketNodeParticlesChemical {
    public static final PacketNodeParticlesChemical INSTANCE = new PacketNodeParticlesChemical();

    public static PacketNodeParticlesChemical get() {
        return INSTANCE;
    }

    public void handle(final NodeParticlesChemicalPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            List<ParticleDataChemical> tempList = payload.particleList();
            for (ParticleDataChemical data : tempList) {
                //Extract
                if (data.fromData != null) {
                    DimBlockPos fromPos = data.fromData.node();
                    BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos.blockPos);
                    if (!(fromTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) fromTE).addParticleDataChemical(new ParticleRenderDataChemical(data.chemicalStack, fromPos.blockPos.relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node().blockPos, data.fromData.position()));
                    }
                }
                if (data.toData != null) {
                    //Insert
                    DimBlockPos toPos = data.toData.node();
                    BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos.blockPos);
                    if (!(toTE instanceof LaserNodeBE)) {
                    } else {
                        ((LaserNodeBE) toTE).addParticleDataChemical(new ParticleRenderDataChemical(data.chemicalStack, data.toData.node().blockPos, data.toData.direction(), toPos.blockPos.relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                    }
                }
            }
        });
    }
}
