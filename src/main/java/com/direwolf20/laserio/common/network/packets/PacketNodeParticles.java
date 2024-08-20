package com.direwolf20.laserio.common.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.ParticleData;
import com.direwolf20.laserio.util.ParticleRenderData;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PacketNodeParticles {
    private List<ParticleData> particleList;

    public PacketNodeParticles(List<ParticleData> particleList) {
        this.particleList = particleList;
    }

    public static void encode(PacketNodeParticles msg, FriendlyByteBuf buffer) {
        List<ParticleData> tempList = msg.particleList;
        int size = tempList.size();
        buffer.writeInt(size);
        for (ParticleData data : tempList) {
            buffer.writeInt(data.item);
            buffer.writeByte(data.itemCount);
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

    public static PacketNodeParticles decode(FriendlyByteBuf buffer) {
        List<ParticleData> thisList = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int item = buffer.readInt();
            byte itemCount = buffer.readByte();
            DimBlockPos fromNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte fromDirection = buffer.readByte();
            byte extractPosition = buffer.readByte();
            DimBlockPos toNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte toDirection = buffer.readByte();
            byte insertPosition = buffer.readByte();
            ParticleData data = new ParticleData(item, itemCount, fromNode, fromDirection, toNode, toDirection, extractPosition, insertPosition);
            thisList.add(data);
        }
        return new PacketNodeParticles(thisList);
    }

    public static class Handler {
        public static void handle(PacketNodeParticles msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketNodeParticles msg) {
        List<ParticleData> tempList = msg.particleList;

        for (ParticleData data : tempList) {
            //Extract
            if (data.fromData != null) {
                DimBlockPos fromPos = data.fromData.node();
                BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos.blockPos);
                if (!(fromTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) fromTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, fromPos.blockPos.relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node().blockPos, data.fromData.position()));
                }
            }
            if (data.toData != null) {
                //Insert
                DimBlockPos toPos = data.toData.node();
                BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos.blockPos);
                if (!(toTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) toTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, data.toData.node().blockPos, data.toData.direction(), toPos.blockPos.relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                }
            }
        }
    }

}