package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.util.ParticleDataFluid;
import com.direwolf20.laserio.util.ParticleRenderDataFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public static PacketNodeParticlesFluid decode(FriendlyByteBuf buffer) {
        List<ParticleDataFluid> thisList = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            FluidStack fluidStack = buffer.readFluidStack();
            BlockPos fromNode = buffer.readBlockPos();
            byte fromDirection = buffer.readByte();
            byte extractPosition = buffer.readByte();
            BlockPos toNode = buffer.readBlockPos();
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
                BlockPos fromPos = data.fromData.node();
                BlockEntity fromTE = Minecraft.getInstance().level.getBlockEntity(fromPos);
                if (!(fromTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) fromTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, fromPos.relative(Direction.values()[data.fromData.direction()]), data.fromData.direction(), data.fromData.node(), data.fromData.position()));
                }
            }
            if (data.toData != null) {
                //Insert
                BlockPos toPos = data.toData.node();
                BlockEntity toTE = Minecraft.getInstance().level.getBlockEntity(toPos);
                if (!(toTE instanceof LaserNodeBE)) {
                } else {
                    ((LaserNodeBE) toTE).addParticleDataFluid(new ParticleRenderDataFluid(data.fluidStack, data.toData.node(), data.toData.direction(), toPos.relative(Direction.values()[data.toData.direction()]), data.toData.position()));
                }
            }
        }
    }
}
