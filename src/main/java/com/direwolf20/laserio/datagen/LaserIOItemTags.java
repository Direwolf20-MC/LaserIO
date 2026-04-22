package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.LaserIORegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class LaserIOItemTags extends ItemTagsProvider {
    public static final TagKey<Item> WRENCHES = forgeTag("wrenches");
    public static final TagKey<Item> TOOLS_WRENCH = forgeTag("tools/wrench");
    public static final TagKey<Item> CIRCUITS_BASIC = forgeTag("circuits/basic");

    public static final TagKey<Item> FILTERS_TAG = ItemTags.create(Identifier.fromNamespaceAndPath(LaserIO.MODID, "filters"));

    private static TagKey<Item> forgeTag(String name) {
        return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
    }

    public LaserIOItemTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider, LaserIO.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(WRENCHES)
                .add(LaserIORegistration.Laser_Wrench.get());
        tag(TOOLS_WRENCH)
                .add(LaserIORegistration.Laser_Wrench.get());
        tag(FILTERS_TAG)
                .add(LaserIORegistration.Filter_Basic.get())
                .add(LaserIORegistration.Filter_Count.get())
                .add(LaserIORegistration.Filter_Tag.get())
                .add(LaserIORegistration.Filter_Mod.get())
                .add(LaserIORegistration.Filter_NBT.get());
    }

    @Override
    public String getName() {
        return "LaserIO Item Tags";
    }
}
