package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        //if (event.includeServer()) {
        generator.addProvider(event.includeServer(), new LaserIORecipes(generator));
        generator.addProvider(event.includeServer(), new LaserIOLootTables(generator));
        LaserIOBlockTags blockTags = new LaserIOBlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        //generator.addProvider(new LaserIOItemTags(generator, blockTags, event.getExistingFileHelper()));
        //}
        //if (event.includeClient()) {
        generator.addProvider(event.includeClient(), new LaserIOBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LaserIOItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LaserIOLanguageProvider(generator, "en_us"));
        //}
    }
}
