package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class LaserIOBlockStates extends BlockStateProvider {
    public LaserIOBlockStates(PackOutput output, ExistingFileHelper helper) {
        super(output, LaserIO.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(Registration.LaserConnector.get());
        //simpleBlock(Registration.LaserNode.get(), models().getExistingFile(modLoc("block/laser_node")));
    }
}
