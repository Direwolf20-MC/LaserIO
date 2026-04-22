package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.client.blockentityrenders.LaserConnectorAdvBERender;
import com.direwolf20.laserio.client.blockentityrenders.LaserConnectorBERender;
import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.client.color.CardChannelTintSource;
import com.direwolf20.laserio.client.color.CardRedstoneTintSource;
import com.direwolf20.laserio.client.color.LaserBlockTintSource;
import com.direwolf20.laserio.client.events.ClientEvents;
import com.direwolf20.laserio.client.events.EventTooltip;
import com.direwolf20.laserio.client.model.CardTransferModeProperty;
import com.direwolf20.laserio.client.renderer.MyRenderType;
import com.direwolf20.laserio.client.screens.*;
import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

// TODO(port, mek): Mekanism 26.1 not yet released. Chemical card client setup disabled.
// import com.direwolf20.laserio.integration.mekanism.CardChemical;

@EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT)
public class ClientSetup {
    public static final Identifier CARD_MODE_PROPERTY = Identifier.fromNamespaceAndPath(LaserIO.MODID, "mode");
    public static final Identifier CARD_CHANNEL_TINT = Identifier.fromNamespaceAndPath(LaserIO.MODID, "card_channel");
    public static final Identifier CARD_REDSTONE_CHANNEL_TINT = Identifier.fromNamespaceAndPath(LaserIO.MODID, "card_redstone_channel");

    public static void init(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(ClientEvents.class);
    }

    @SubscribeEvent
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        MyRenderType.registerPipelines(event);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.LaserNode_Container.get(), LaserNodeScreen::new);
        event.register(Registration.CardItem_Container.get(), CardItemScreen::new);
        event.register(Registration.CardFluid_Container.get(), CardFluidScreen::new);
        event.register(Registration.CardEnergy_Container.get(), CardEnergyScreen::new);
        event.register(Registration.CardRedstone_Container.get(), CardRedstoneScreen::new);
        // TODO(port, mek): re-enable CardChemical screen registration when Mekanism 26.1 ships.
        // event.register(Registration.CardChemical_Container.get(), CardChemicalScreen::new);
        event.register(Registration.CardHolder_Container.get(), CardHolderScreen::new);
        event.register(Registration.FilterBasic_Container.get(), FilterBasicScreen::new);
        event.register(Registration.FilterCount_Container.get(), FilterCountScreen::new);
        event.register(Registration.FilterTag_Container.get(), FilterTagScreen::new);
        event.register(Registration.FilterNBT_Container.get(), FilterNBTScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.LaserConnector_BE.get(), LaserConnectorBERender::new);
        event.registerBlockEntityRenderer(Registration.LaserNode_BE.get(), LaserNodeBERender::new);
        event.registerBlockEntityRenderer(Registration.LaserConnectorAdv_BE.get(), LaserConnectorAdvBERender::new);
    }

    @SubscribeEvent
    public static void registerTooltipFactory(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EventTooltip.CopyPasteTooltipComponent.Data.class, EventTooltip.CopyPasteTooltipComponent::new);
    }

    @SubscribeEvent
    public static void registerRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(CARD_MODE_PROPERTY, CardTransferModeProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(CARD_CHANNEL_TINT, CardChannelTintSource.MAP_CODEC);
        event.register(CARD_REDSTONE_CHANNEL_TINT, CardRedstoneTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(LaserBlockTintSource.INSTANCE), Registration.LaserNode.get());
        event.register(List.of(LaserBlockTintSource.INSTANCE), Registration.LaserConnector.get());
        event.register(List.of(LaserBlockTintSource.INSTANCE), Registration.LaserConnectorAdv.get());
    }
}
