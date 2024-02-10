package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public class MekanismStaticRefs {
    public static BlockCapability<IGasHandler, @Nullable Direction> GAS_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "gas_handler"), IGasHandler.class);

}
