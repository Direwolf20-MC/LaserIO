package com.direwolf20.laserio.integration.mekanism;

import net.minecraftforge.fml.ModList;

public class MekanismIntegration {
    private static final String ID = "mekanism";

    public MekanismIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ID);
    }

}