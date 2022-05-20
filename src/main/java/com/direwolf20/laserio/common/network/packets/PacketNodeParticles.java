package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.util.ParticleData;
import com.direwolf20.laserio.util.ParticleRenderData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
                buffer.writeBlockPos(data.fromData.node());
                buffer.writeByte(data.fromData.direction());
                buffer.writeByte(data.fromData.position());
            }
            if (data.toData != null) {
                buffer.writeBlockPos(data.toData.node());
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
            BlockPos fromNode = buffer.readBlockPos();
            byte fromDirection = buffer.readByte();
            byte extractPosition = buffer.readByte();
            BlockPos toNode = buffer.readBlockPos();
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
                BlockPos fromPos = data.fromData.node();
                BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos);
                if (!(fromTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) fromTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, fromPos.relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node(), data.fromData.position()));
                }
            }
            if (data.toData != null) {
                //Insert
                BlockPos toPos = data.toData.node();
                BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos);
                if (!(toTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) toTE).addParticleData(new ParticleRenderData(data.item, data.itemCount, data.toData.node(), data.toData.direction(), toPos.relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                }
            }
        }
    }
}
