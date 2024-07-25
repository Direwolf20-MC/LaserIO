package com.direwolf20.laserio.common.network;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.network.data.*;
import com.direwolf20.laserio.common.network.handler.*;
import com.direwolf20.laserio.integration.mekanism.MekanismIntegration;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


public class PacketHandler {
    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(LaserIO.MODID);

        // Server side
        registrar.playToServer(UpdateCardPayload.TYPE, UpdateCardPayload.STREAM_CODEC, PacketUpdateCard.get()::handle);
        registrar.playToServer(UpdateRedstoneCardPayload.TYPE, UpdateRedstoneCardPayload.STREAM_CODEC, PacketUpdateRedstoneCard.get()::handle);
        registrar.playToServer(UpdateFilterPayload.TYPE, UpdateFilterPayload.STREAM_CODEC, PacketUpdateFilter.get()::handle);
        registrar.playToServer(OpenCardPayload.TYPE, OpenCardPayload.STREAM_CODEC, PacketOpenCard.get()::handle);
        registrar.playToServer(OpenFilterPayload.TYPE, OpenFilterPayload.STREAM_CODEC, PacketOpenFilter.get()::handle);
        registrar.playToServer(GhostSlotPayload.TYPE, GhostSlotPayload.STREAM_CODEC, PacketGhostSlot.get()::handle);
        registrar.playToServer(OpenNodePayload.TYPE, OpenNodePayload.STREAM_CODEC, PacketOpenNode.get()::handle);
        registrar.playToServer(UpdateFilterTagPayload.TYPE, UpdateFilterTagPayload.STREAM_CODEC, PacketUpdateFilterTag.get()::handle);
        registrar.playToServer(ToggleParticlesPayload.TYPE, ToggleParticlesPayload.STREAM_CODEC, PacketToggleParticles.get()::handle);
        registrar.playToServer(ChangeColorPayload.TYPE, ChangeColorPayload.STREAM_CODEC, PacketChangeColor.get()::handle);
        registrar.playToServer(CopyPasteCardPayload.TYPE, CopyPasteCardPayload.STREAM_CODEC, PacketCopyPasteCard.get()::handle);

        //Client Side
        registrar.playToClient(NodeParticlesPayload.TYPE, NodeParticlesPayload.STREAM_CODEC, PacketNodeParticles.get()::handle);
        registrar.playToClient(NodeParticlesFluidPayload.TYPE, NodeParticlesFluidPayload.STREAM_CODEC, PacketNodeParticlesFluid.get()::handle);

        //Mekanism Packets Only
        if (MekanismIntegration.isLoaded()) {
            //Client Side
            registrar.playToClient(NodeParticlesChemicalPayload.TYPE, NodeParticlesChemicalPayload.STREAM_CODEC, PacketNodeParticlesChemical.get()::handle);
        }
    }
}
