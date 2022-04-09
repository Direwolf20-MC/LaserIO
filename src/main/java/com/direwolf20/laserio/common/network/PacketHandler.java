package com.direwolf20.laserio.common.network;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.network.packets.PacketOpenCard;
import com.direwolf20.laserio.common.network.packets.PacketUpdateCard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
        HANDLER.registerMessage(id++, PacketOpenCard.class, PacketOpenCard::encode, PacketOpenCard::decode, PacketOpenCard.Handler::handle);
        //HANDLER.registerMessage(id++, PacketExtractUpgrade.class,     PacketExtractUpgrade::encode,       PacketExtractUpgrade::decode,       PacketExtractUpgrade.Handler::handle);

        //Client Side
        //HANDLER.registerMessage(id++, PacketDurabilitySync.class,     PacketDurabilitySync::encode,       PacketDurabilitySync::decode,       PacketDurabilitySync.Handler::handle);

    }

    public static void sendTo(Object msg, ServerPlayer player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object msg, Level level) {
        //Todo Maybe only send to nearby players?
        for (Player player : level.players()) {
            if (!(player instanceof FakePlayer))
                HANDLER.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }
}
