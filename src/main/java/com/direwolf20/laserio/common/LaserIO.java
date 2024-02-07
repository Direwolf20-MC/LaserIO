package com.direwolf20.laserio.common;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.setup.ClientSetup;
import com.direwolf20.laserio.setup.Config;
import com.direwolf20.laserio.setup.ModSetup;
import com.direwolf20.laserio.setup.Registration;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LaserIO.MODID)
public class LaserIO {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "laserio";

    public LaserIO(IEventBus eventBus) {
        // Register the deferred registry
        Registration.init(eventBus);
        Config.register();
        // Register the setup method for modloading
        // Register the enqueueIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        //MinecraftForge.EVENT_BUS.register(this);
        // Register 'ModSetup::init' to be called at mod setup time (server and client)
        eventBus.addListener(ModSetup::init);
        ModSetup.TABS.register(eventBus);
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(PacketHandler::registerNetworking);
        //modbus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        // Register 'ClientSetup::init' to be called at mod setup time (client only)
        if (FMLLoader.getDist().isClient()) {
            eventBus.addListener(ClientSetup::init);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> ((LaserNodeBE) be).nodeSideCaches[side.ordinal()].itemHandler,
                // blocks to register for
                Registration.LaserNode.get());
        //TODO PORT -- Register and fix Card Holder
        event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> {
                    if (itemStack.getItem() instanceof CardHolder holder)
                        return holder.getItemHandler(itemStack);
                    return null;
                },
                Registration.Card_Holder.get()
        );
    }

    /*public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().registerAll(
                new CardClearRecipe.Serializer().setRegistryName("laserio:cardclear")
        );
    }*/
}
/*
    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("laserio", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
        {
            // Register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
*/