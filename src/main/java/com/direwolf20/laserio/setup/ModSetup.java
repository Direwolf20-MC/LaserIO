package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(ServerTickHandler.class);
    }

    public static final String TAB_NAME = "laserio";
    public static DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LaserIO.MODID);
    public static DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_LASERIO = TABS.register(TAB_NAME, () -> CreativeModeTab.builder()
            .title(Component.literal("LaserIO"))
            .icon(() -> new ItemStack(Registration.Laser_Wrench.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                Registration.ITEMS.getEntries().forEach(e -> {
                    Item item = e.get();
                    output.accept(item);
                });
            })
            .build());
}
