package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = LaserIO.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new LaserIORecipes(generator));
            generator.addProvider(new LaserIOLootTables(generator));
            //LaserIOBlockTags blockTags = new LaserIOBlockTags(generator, event.getExistingFileHelper());
            //generator.addProvider(blockTags);
            //generator.addProvider(new LaserIOItemTags(generator, blockTags, event.getExistingFileHelper()));
        }
        if (event.includeClient()) {
            generator.addProvider(new LaserIOBlockStates(generator, event.getExistingFileHelper()));
            generator.addProvider(new LaserIOItemModels(generator, event.getExistingFileHelper()));
            generator.addProvider(new LaserIOLanguageProvider(generator, "en_us"));
        }
    }
}
