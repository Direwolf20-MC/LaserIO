package com.direwolf20.laserio.integration.mekanism;

// TODO(port, mek): Mekanism 26.1 not yet released. isLoaded() is hard-wired to false to gate off all chemical handling.
// import net.neoforged.fml.ModList;

public class MekanismIntegration {
    private static final String ID = "mekanism";

    public MekanismIntegration() {
    }

    public static boolean isLoaded() {
        // TODO(port, mek): restore `return ModList.get().isLoaded(ID);` once Mekanism 26.1 ships.
        return false;
    }
}
