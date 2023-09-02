package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.ParticleDataFluid;
import com.direwolf20.laserio.util.ParticleRenderDataFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketNodeParticlesFluid {
    private List<ParticleDataFluid> particleList;

    public PacketNodeParticlesFluid(List<ParticleDataFluid> particleList) {
        this.particleList = particleList;
    }

    public static void encode(PacketNodeParticlesFluid msg, FriendlyByteBuf buffer) {
        List<ParticleDataFluid> tempList = msg.particleList;
        int size = tempList.size();
        buffer.writeInt(size);
        for (ParticleDataFluid data : tempList) {
            buffer.writeFluidStack(data.fluidStack);
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

    public static PacketNodeParticlesFluid decode(FriendlyByteBuf buffer) {
        List<ParticleDataFluid> thisList = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            FluidStack fluidStack = buffer.readFluidStack();
            DimBlockPos fromNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte fromDirection = buffer.readByte();
            byte extractPosition = buffer.readByte();
            DimBlockPos toNode = new DimBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
            byte toDirection = buffer.readByte();
            byte insertPosition = buffer.readByte();
            ParticleDataFluid data = new ParticleDataFluid(fluidStack, fromNode, fromDirection, toNode, toDirection, extractPosition, insertPosition);
            thisList.add(data);
        }
        return new PacketNodeParticlesFluid(thisList);
    }

    public static class Handler {
        public static void handle(PacketNodeParticlesFluid msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketNodeParticlesFluid msg) {
        List<ParticleDataFluid> tempList = msg.particleList;
        for (ParticleDataFluid data : tempList) {
            //Extract
            if (data.fromData != null) {
                DimBlockPos fromPos = data.fromData.node();
                BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos.blockPos);
                if (!(fromTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) fromTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, fromPos.blockPos.relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node().blockPos, data.fromData.position()));
                }
            }
            if (data.toData != null) {
                //Insert
                DimBlockPos toPos = data.toData.node();
                BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos.blockPos);
                if (!(toTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) toTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, data.toData.node().blockPos, data.toData.direction(), toPos.blockPos.relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                }
            }
        }
    }
}
