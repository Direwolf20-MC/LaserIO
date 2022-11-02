package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOItemTags extends ItemTagsProvider {
    public static final TagKey<Item> WRENCHES = forgeTag("wrenches");
    public static final TagKey<Item> TOOLS_WRENCH = forgeTag("tools/wrench");

    private static TagKey<Item> forgeTag(String name) {
        return ItemTags.create(new ResourceLocation("forge", name));
    }

    public LaserIOItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, LaserIO.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(WRENCHES)
                .add(Registration.Laser_Wrench.get());
        tag(TOOLS_WRENCH)
                .add(Registration.Laser_Wrench.get());
    }

    @Override
    public String getName() {
        return "LaserIO Tags";
    }
}
