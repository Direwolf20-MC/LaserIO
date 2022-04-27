package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.client.blockentityrenders.LaserConnectorBERender;
import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.client.events.ClientEvents;
import com.direwolf20.laserio.client.screens.BasicFilterScreen;
import com.direwolf20.laserio.client.screens.FilterCountScreen;
import com.direwolf20.laserio.client.screens.ItemCardScreen;
import com.direwolf20.laserio.client.screens.LaserNodeScreen;
import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.LaserNode.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(Registration.LaserConnector.get(), RenderType.cutout());

        //Register our Render Events Class
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);

        //Screens
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.LaserNode_Container.get(), LaserNodeScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.ItemCard_Container.get(), ItemCardScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.BasicFilter_Container.get(), BasicFilterScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.FilterCount_Container.get(), FilterCountScreen::new);           // Attach our container to the screen
        });

    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register Block Entity Renders
        event.registerBlockEntityRenderer(Registration.LaserConnector_BE.get(), LaserConnectorBERender::new);
        event.registerBlockEntityRenderer(Registration.LaserNode_BE.get(), LaserNodeBERender::new);
    }

}
