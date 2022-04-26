package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;

public class LaserIOLootTables extends BaseLootTableProvider {

    public LaserIOLootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(Registration.LaserNode.get(), createSimpleTable("lasernode", Registration.LaserNode.get()));
        lootTables.put(Registration.LaserConnector.get(), createSimpleTable("laserconnector", Registration.LaserConnector.get()));
    }
}
