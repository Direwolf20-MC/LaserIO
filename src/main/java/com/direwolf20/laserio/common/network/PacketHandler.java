package com.direwolf20.laserio.common.network;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.network.packets.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    private static short index = 0;

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(LaserIO.MODID, "main_network_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;

        // Server side
        HANDLER.registerMessage(id++, PacketUpdateCard.class, PacketUpdateCard::encode, PacketUpdateCard::decode, PacketUpdateCard.Handler::handle);
        HANDLER.registerMessage(id++, PacketUpdateRedstoneCard.class, PacketUpdateRedstoneCard::encode, PacketUpdateRedstoneCard::decode, PacketUpdateRedstoneCard.Handler::handle);
        HANDLER.registerMessage(id++, PacketUpdateFilter.class, PacketUpdateFilter::encode, PacketUpdateFilter::decode, PacketUpdateFilter.Handler::handle);
        HANDLER.registerMessage(id++, PacketOpenCard.class, PacketOpenCard::encode, PacketOpenCard::decode, PacketOpenCard.Handler::handle);
        HANDLER.registerMessage(id++, PacketOpenFilter.class, PacketOpenFilter::encode, PacketOpenFilter::decode, PacketOpenFilter.Handler::handle);
        HANDLER.registerMessage(id++, PacketGhostSlot.class, PacketGhostSlot::encode, PacketGhostSlot::decode, PacketGhostSlot.Handler::handle);
        HANDLER.registerMessage(id++, PacketOpenNode.class, PacketOpenNode::encode, PacketOpenNode::decode, PacketOpenNode.Handler::handle);
        HANDLER.registerMessage(id++, PacketUpdateFilterTag.class, PacketUpdateFilterTag::encode, PacketUpdateFilterTag::decode, PacketUpdateFilterTag.Handler::handle);
        //HANDLER.registerMessage(id++, PacketExtractUpgrade.class,     PacketExtractUpgrade::encode,       PacketExtractUpgrade::decode,       PacketExtractUpgrade.Handler::handle);

        //Client Side
        HANDLER.registerMessage(id++, PacketNodeParticles.class, PacketNodeParticles::encode, PacketNodeParticles::decode, PacketNodeParticles.Handler::handle);
        HANDLER.registerMessage(id++, PacketNodeParticlesFluid.class, PacketNodeParticlesFluid::encode, PacketNodeParticlesFluid::decode, PacketNodeParticlesFluid.Handler::handle);
        //HANDLER.registerMessage(id++, PacketDurabilitySync.class,     PacketDurabilitySync::encode,       PacketDurabilitySync::decode,       PacketDurabilitySync.Handler::handle);

    }

    public static void sendTo(Object msg, ServerPlayer player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object msg, Level level) {
        for (Player player : level.players()) {
            if (!(player instanceof FakePlayer))
                HANDLER.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    /**
     * Sends a vanilla packet to the given player
     *
     * @param player Player
     * @param packet Packet
     *               Stolen from Tinkers Construct :)
     */
    public static void sendVanillaPacket(Entity player, Packet<?> packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }
}
