package com.direwolf20.laserio.common.network;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.network.data.*;
import com.direwolf20.laserio.common.network.handler.*;
import com.direwolf20.laserio.integration.mekanism.MekanismIntegration;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;


public class PacketHandler {
    public static void registerNetworking(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(LaserIO.MODID);

        // Server side
        registrar.play(UpdateCardPayload.ID, UpdateCardPayload::new, handler -> handler.server(PacketUpdateCard.get()::handle));
        registrar.play(UpdateRedstoneCardPayload.ID, UpdateRedstoneCardPayload::new, handler -> handler.server(PacketUpdateRedstoneCard.get()::handle));
        registrar.play(UpdateFilterPayload.ID, UpdateFilterPayload::new, handler -> handler.server(PacketUpdateFilter.get()::handle));
        registrar.play(OpenCardPayload.ID, OpenCardPayload::new, handler -> handler.server(PacketOpenCard.get()::handle));
        registrar.play(OpenFilterPayload.ID, OpenFilterPayload::new, handler -> handler.server(PacketOpenFilter.get()::handle));
        registrar.play(GhostSlotPayload.ID, GhostSlotPayload::new, handler -> handler.server(PacketGhostSlot.get()::handle));
        registrar.play(OpenNodePayload.ID, OpenNodePayload::new, handler -> handler.server(PacketOpenNode.get()::handle));
        registrar.play(UpdateFilterTagPayload.ID, UpdateFilterTagPayload::new, handler -> handler.server(PacketUpdateFilterTag.get()::handle));
        registrar.play(ChangeColorPayload.ID, ChangeColorPayload::new, handler -> handler.server(PacketChangeColor.get()::handle));
        registrar.play(CopyPasteCardPayload.ID, CopyPasteCardPayload::new, handler -> handler.server(PacketCopyPasteCard.get()::handle));
        //HANDLER.registerMessage(id++, PacketExtractUpgrade.class,     PacketExtractUpgrade::encode,       PacketExtractUpgrade::decode,       PacketExtractUpgrade.Handler::handle);

        //Client Side
        registrar.play(NodeParticlesPayload.ID, NodeParticlesPayload::new, handler -> handler.client(PacketNodeParticles.get()::handle));
        registrar.play(NodeParticlesFluidPayload.ID, NodeParticlesFluidPayload::new, handler -> handler.client(PacketNodeParticlesFluid.get()::handle));
        //HANDLER.registerMessage(id++, PacketDurabilitySync.class,     PacketDurabilitySync::encode,       PacketDurabilitySync::decode,       PacketDurabilitySync.Handler::handle);


        //Mekanism Packets Only
        if (MekanismIntegration.isLoaded()) {
            //Client Side
            registrar.play(NodeParticlesChemicalPayload.ID, NodeParticlesChemicalPayload::new, handler -> handler.client(PacketNodeParticlesChemical.get()::handle));
        }
    }
}
