package com.direwolf20.laserio.common.network.data;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.ParticleDataFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public record NodeParticlesFluidPayload(
        List<ParticleDataFluid> particleList
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(LaserIO.MODID, "node_particles_fluid");

    public NodeParticlesFluidPayload(final FriendlyByteBuf buffer) {
        this(decodeList(buffer));
    }

    public static List<ParticleDataFluid> decodeList(final FriendlyByteBuf buffer) {
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
        return thisList;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        int size = particleList.size();
        buffer.writeInt(size);
        for (ParticleDataFluid data : particleList) {
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

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
