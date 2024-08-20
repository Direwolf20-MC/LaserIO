package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleRenderDataChemical;
import com.direwolf20.laserio.util.DimBlockPos;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.ChemicalUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketNodeParticlesChemical {
    private List<ParticleDataChemical> particleList;

    public PacketNodeParticlesChemical(List<ParticleDataChemical> particleList) {
        this.particleList = particleList;
    }

    public static void encode(PacketNodeParticlesChemical msg, FriendlyByteBuf buffer) {
        List<ParticleDataChemical> tempList = msg.particleList;
        int size = tempList.size();
        buffer.writeInt(size);
        for (ParticleDataChemical data : tempList) {
            buffer.writeUtf(data.chemicalType);
            ChemicalUtils.writeChemicalStack(buffer, data.chemicalStack);
            if (data.fromData != null) {
                buffer.writeResourceKey(data.fromData.node().levelKey);
                buffer.writeBlockPos(data.fromData.node().blockPos);
                buffer.writeByte(data.fromData.direction());
                buffer.writeByte(data.fromData.position());
            }
            if (data.toData != null) {
                buffer.writeResourceKey(data.toData.node().levelKey);
                buffer.writeBlockPos(data.toData.node().blockPos);
                buffer.writeByte(data.toData.direction());
                buffer.writeByte(data.toData.position());
            }
        }
    }

    public static PacketNodeParticlesChemical decode(FriendlyByteBuf buffer) {
        List<ParticleDataChemical> thisList = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            String type = buffer.readUtf();
            ChemicalType chemicalType = ChemicalType.fromString(type);
            ChemicalStack<?> chemicalStack;

            if (chemicalType == ChemicalType.GAS)
                chemicalStack = ChemicalUtils.readGasStack(buffer);
            else if (chemicalType == ChemicalType.INFUSION)
                chemicalStack = ChemicalUtils.readInfusionStack(buffer);
            else if (chemicalType == ChemicalType.PIGMENT)
                chemicalStack = ChemicalUtils.readPigmentStack(buffer);
            else if (chemicalType == ChemicalType.SLURRY)
                chemicalStack = ChemicalUtils.readSlurryStack(buffer);
            else
                continue; //Shouldn't happen?

            DimBlockPos fromNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte fromDirection = buffer.readByte();
            byte extractPosition = buffer.readByte();
            DimBlockPos toNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte toDirection = buffer.readByte();
            byte insertPosition = buffer.readByte();
            ParticleDataChemical data = new ParticleDataChemical(chemicalStack, fromNode, fromDirection, toNode, toDirection, extractPosition, insertPosition);
            thisList.add(data);
        }
        return new PacketNodeParticlesChemical(thisList);
    }

    public static class Handler {
        public static void handle(PacketNodeParticlesChemical msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketNodeParticlesChemical msg) {
        List<ParticleDataChemical> tempList = msg.particleList;

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
    }
}