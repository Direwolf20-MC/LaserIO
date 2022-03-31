package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOBlockStates extends BlockStateProvider {
    public LaserIOBlockStates(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, LaserIO.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(Registration.LaserConnector.get());
        //simpleBlock(Registration.LaserNode.get(), models().getExistingFile(modLoc("block/laser_node")));
    }
}
