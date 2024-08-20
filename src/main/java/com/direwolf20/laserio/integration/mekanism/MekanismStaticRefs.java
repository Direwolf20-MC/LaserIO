package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.gas.IGasHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class MekanismStaticRefs {
    public static Capability<IGasHandler> GAS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}