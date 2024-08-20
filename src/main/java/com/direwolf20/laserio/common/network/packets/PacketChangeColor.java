package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.awt.Color;
import java.util.function.Supplier;

public class PacketChangeColor {
    private BlockPos sourcePos;
    private int color;
    private int wrenchAlpha;

    public PacketChangeColor(BlockPos pos, int color, int wrenchAlpha) {
        this.sourcePos = pos;
        this.color = color;
        this.wrenchAlpha = wrenchAlpha;
    }

    public static void encode(PacketChangeColor msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeInt(msg.color);
        buffer.writeInt(msg.wrenchAlpha);
    }

    public static PacketChangeColor decode(FriendlyByteBuf buffer) {
        return new PacketChangeColor(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketChangeColor msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                BlockEntity blockEntity = sender.level().getBlockEntity(msg.sourcePos);
                if (blockEntity instanceof LaserNodeBE laserNodeBE) {
                    laserNodeBE.setColor(new Color(msg.color, true), msg.wrenchAlpha);
                    laserNodeBE.discoverAllNodes();
                }
            });


            ctx.get().setPacketHandled(true);
        }
    }
}