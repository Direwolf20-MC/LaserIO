package com.direwolf20.laserio.datagen.customrecipes;

import net.minecraftforge.registries.RegistryObject;

import static com.direwolf20.laserio.setup.Registration.RECIPE_SERIALIZERS;

public class RecipeSerializers {
    public static final RegistryObject<CardClearRecipe.Serializer> CARD_CLEAR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("laserio:cardclear", CardClearRecipe.Serializer::new);
}
