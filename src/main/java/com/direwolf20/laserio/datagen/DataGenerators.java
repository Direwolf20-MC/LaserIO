package com.direwolf20.laserio.datagen;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.direwolf20.laserio.common.LaserIO;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        //if (event.includeServer()) {
        generator.addProvider(event.includeServer(), new LaserIORecipes(packOutput));
        //generator.addProvider(event.includeServer(), new LaserIOLootTables(generator));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(LaserIOLootTable::new, LootContextParamSets.BLOCK))));
        LaserIOBlockTags blockTags = new LaserIOBlockTags(packOutput, lookupProvider, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        LaserIOItemTags itemTags = new LaserIOItemTags(packOutput, lookupProvider, blockTags, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), itemTags);
        //generator.addProvider(new LaserIOItemTags(generator, blockTags, event.getExistingFileHelper()));
        //}
        //if (event.includeClient()) {
        generator.addProvider(event.includeClient(), new LaserIOBlockStates(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LaserIOItemModels(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LaserIOLanguageProvider(packOutput, "en_us"));
        //}
    }

}