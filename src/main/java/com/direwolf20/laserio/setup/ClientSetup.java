package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.client.blockentityrenders.LaserConnectorBERender;
import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.client.events.ClientEvents;
import com.direwolf20.laserio.client.events.EventTooltip;
import com.direwolf20.laserio.client.screens.*;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.awt.*;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.LaserNode.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(Registration.LaserConnector.get(), RenderType.cutout());

        //Register Custom Tooltips
        //MinecraftForgeClient.registerTooltipComponentFactory(EventTooltip.CopyPasteTooltipComponent.Data.class, EventTooltip.CopyPasteTooltipComponent::new);

        //Register our Render Events Class
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(EventTooltip.class);

        //Screens
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.LaserNode_Container.get(), LaserNodeScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.CardItem_Container.get(), CardItemScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.CardFluid_Container.get(), CardFluidScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.CardEnergy_Container.get(), CardEnergyScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.CardRedstone_Container.get(), CardRedstoneScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.CardHolder_Container.get(), CardHolderScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.FilterBasic_Container.get(), FilterBasicScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.FilterCount_Container.get(), FilterCountScreen::new);           // Attach our container to the screen
            MenuScreens.register(Registration.FilterTag_Container.get(), FilterTagScreen::new);           // Attach our container to the screen
        });

        //Item Properties -- For giving the Cards an Insert/Extract on the itemstack
        event.enqueueWork(() -> {
            ItemProperties.register(Registration.Card_Item.get(),
                    new ResourceLocation(LaserIO.MODID, "mode"), (stack, level, living, id) -> {
                        return (int) BaseCard.getTransferMode(stack);
                    });
        });
        event.enqueueWork(() -> {
            ItemProperties.register(Registration.Card_Fluid.get(),
                    new ResourceLocation(LaserIO.MODID, "mode"), (stack, level, living, id) -> {
                        return (int) BaseCard.getTransferMode(stack);
                    });
        });
        event.enqueueWork(() -> {
            ItemProperties.register(Registration.Card_Energy.get(),
                    new ResourceLocation(LaserIO.MODID, "mode"), (stack, level, living, id) -> {
                        return (int) BaseCard.getTransferMode(stack);
                    });
        });
        event.enqueueWork(() -> {
            ItemProperties.register(Registration.Card_Redstone.get(),
                    new ResourceLocation(LaserIO.MODID, "mode"), (stack, level, living, id) -> {
                        return (int) CardRedstone.getTransferMode(stack);
                    });
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register Block Entity Renders
        event.registerBlockEntityRenderer(Registration.LaserConnector_BE.get(), LaserConnectorBERender::new);
        event.registerBlockEntityRenderer(Registration.LaserNode_BE.get(), LaserNodeBERender::new);
    }

    @SubscribeEvent
    public static void registerTooltipFactory(RegisterClientTooltipComponentFactoriesEvent event) {
        //LOGGER.debug("Registering custom tooltip component factories for {}", Reference.MODID);
        event.register(EventTooltip.CopyPasteTooltipComponent.Data.class, EventTooltip.CopyPasteTooltipComponent::new);
    }

    //For giving the cards their channel color on the itemstack
    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        final ItemColors colors = event.getItemColors();

        colors.register((stack, index) -> {
            if (index == 2) {
                if (BaseCard.getTransferMode(stack) == (byte) 3) {
                    Color color = LaserNodeBERender.colors[BaseCard.getRedstoneChannel(stack)];
                    return color.getRGB();
                } else {
                    Color color = LaserNodeBERender.colors[BaseCard.getChannel(stack)];
                    return color.getRGB();
                }
            }
            return 0xFFFFFFFF;
        }, Registration.Card_Item.get());
        colors.register((stack, index) -> {
            if (index == 2) {
                if (BaseCard.getTransferMode(stack) == (byte) 3) {
                    Color color = LaserNodeBERender.colors[BaseCard.getRedstoneChannel(stack)];
                    return color.getRGB();
                } else {
                    Color color = LaserNodeBERender.colors[BaseCard.getChannel(stack)];
                    return color.getRGB();
                }
            }
            return 0xFFFFFFFF;
        }, Registration.Card_Fluid.get());
        colors.register((stack, index) -> {
            if (index == 2) {
                if (BaseCard.getTransferMode(stack) == (byte) 3) {
                    Color color = LaserNodeBERender.colors[BaseCard.getRedstoneChannel(stack)];
                    return color.getRGB();
                } else {
                    Color color = LaserNodeBERender.colors[BaseCard.getChannel(stack)];
                    return color.getRGB();
                }
            }
            return 0xFFFFFFFF;
        }, Registration.Card_Energy.get());
        colors.register((stack, index) -> {
            if (index == 2) {
                Color color = LaserNodeBERender.colors[CardRedstone.getRedstoneChannel(stack)];
                return color.getRGB();
            }
            return 0xFFFFFFFF;
        }, Registration.Card_Redstone.get());
    }


}
