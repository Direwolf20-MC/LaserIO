package com.direwolf20.laserio.datagen;

import java.util.concurrent.CompletableFuture;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOItemTags extends ItemTagsProvider {
    public static final TagKey<Item> WRENCHES = forgeTag("wrenches");
    public static final TagKey<Item> TOOLS_WRENCH = forgeTag("tools/wrench");
    public static final TagKey<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");

    public static final TagKey<Item> FILTERS_TAG = ItemTags.create(new ResourceLocation(LaserIO.MODID, "filters"));
    
    private static TagKey<Item> forgeTag(String name) {
        return ItemTags.create(new ResourceLocation("forge", name));
    }

    public LaserIOItemTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(packOutput, lookupProvider, blockTags.contentsGetter(), LaserIO.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(WRENCHES)
                .add(Registration.Laser_Wrench.get());
        tag(TOOLS_WRENCH)
                .add(Registration.Laser_Wrench.get());
        tag(FILTERS_TAG)
                .add(Registration.Filter_Basic.get())
                .add(Registration.Filter_Count.get())
                .add(Registration.Filter_Tag.get())
                .add(Registration.Filter_Mod.get())
                .add(Registration.Filter_NBT.get());
    }

    @Override
    public String getName() {
        return "LaserIO Item Tags";
    }

}