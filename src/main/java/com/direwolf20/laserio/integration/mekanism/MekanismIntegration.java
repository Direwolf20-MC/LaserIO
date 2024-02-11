package com.direwolf20.laserio.integration.mekanism;

import net.neoforged.fml.ModList;

public class MekanismIntegration {
    private static final String ID = "mekanism";

    public MekanismIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ID);
    }
}
