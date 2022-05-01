package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOBlockTags extends BlockTagsProvider {

    public LaserIOBlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, LaserIO.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Registration.LaserNode.get())
                .add(Registration.LaserConnector.get());
    }

    @Override
    public String getName() {
        return "LaserIO Tags";
    }
}