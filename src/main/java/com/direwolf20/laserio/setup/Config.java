package com.direwolf20.laserio.setup;


import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    //public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_CARD = "card";
    public static final String SUBCATEGORY_FLUID = "fluid_card";
    public static final String SUBCATEGORY_ENERGY = "energy_card";
    public static final String SUBCATEGORY_CHEMICAL = "chemical_card";

    public static ModConfigSpec.IntValue BASE_MILLI_BUCKETS_FLUID;
    public static ModConfigSpec.IntValue MULTIPLIER_MILLI_BUCKETS_FLUID;
    public static ModConfigSpec.IntValue BASE_MILLI_BUCKETS_CHEMICAL;
    public static ModConfigSpec.IntValue MULTIPLIER_MILLI_BUCKETS_CHEMICAL;
    public static ModConfigSpec.IntValue MAX_FE_TICK;

    public static void register() {
        //registerServerConfigs();
        registerCommonConfigs();
        //registerClientConfigs();
    }

    private static void registerClientConfigs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
    }

    private static void registerCommonConfigs() {
        COMMON_BUILDER.comment("Card settings").push(CATEGORY_CARD);
        cardConfig();
        COMMON_BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
    }

    private static void registerServerConfigs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_BUILDER.build());
    }

    private static void generalConfig() {

    }

    private static void cardConfig() {
        COMMON_BUILDER.comment("Fluid Card").push(SUBCATEGORY_FLUID);
        BASE_MILLI_BUCKETS_FLUID = COMMON_BUILDER.comment("Millibuckets for Fluid Cards without Overclockers installed")
                .defineInRange("base_milli_buckets_fluid", 5000, 0, Integer.MAX_VALUE);
        MULTIPLIER_MILLI_BUCKETS_FLUID = COMMON_BUILDER.comment("Multiplier for Overclocker Cards - Number of Overclockers * this value = max millibuckets")
                .defineInRange("multiplier_milli_buckets_fluid", 10000, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Energy Card").push(SUBCATEGORY_ENERGY);
        MAX_FE_TICK = COMMON_BUILDER.comment("Maximum FE/T for Energy Cards")
                .defineInRange("max_fe_tick", 1000000, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Chemical Card").push(SUBCATEGORY_CHEMICAL);
        BASE_MILLI_BUCKETS_CHEMICAL = COMMON_BUILDER.comment("Millibuckets for Chemical Cards without Overclockers installed (Only is Mekanism is installed)")
                .defineInRange("base_milli_buckets_chemical", 5000, 0, Integer.MAX_VALUE);
        MULTIPLIER_MILLI_BUCKETS_CHEMICAL = COMMON_BUILDER.comment("Multiplier for Overclocker Cards - Number of Overclockers * this value = max millibuckets  (Only is Mekanism is installed)")
                .defineInRange("multiplier_milli_buckets_chemical", 10000, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
    }


    private static void clientConfig() {

    }

    private static void serverConfig() {

    }

}
