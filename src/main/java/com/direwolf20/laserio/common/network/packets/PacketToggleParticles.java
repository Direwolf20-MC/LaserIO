package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleParticles {
    private boolean renderParticles;

    public PacketToggleParticles(boolean renderParticles) {
        this.renderParticles = renderParticles;
    }

    public static void encode(PacketToggleParticles msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.renderParticles);
    }

    public static PacketToggleParticles decode(FriendlyByteBuf buffer) {
        return new PacketToggleParticles(buffer.readBoolean());
    }

    public static class Handler {
        public static void handle(PacketToggleParticles msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                AbstractContainerMenu container = sender.containerMenu;
                if (container == null)
                    return;

                if (container instanceof LaserNodeContainer laserNodeContainer) {
                    laserNodeContainer.tile.setShowParticles(msg.renderParticles);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}