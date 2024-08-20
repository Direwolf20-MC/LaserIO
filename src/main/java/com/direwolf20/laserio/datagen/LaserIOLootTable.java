package com.direwolf20.laserio.datagen;

import java.util.Map;
import java.util.stream.Collectors;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;

import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class LaserIOLootTable extends VanillaBlockLoot {

    @Override
    protected void generate() {
        dropSelf(Registration.LaserNode.get());
        dropSelf(Registration.LaserConnector.get());
        dropSelf(Registration.LaserConnectorAdv.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(LaserIO.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}