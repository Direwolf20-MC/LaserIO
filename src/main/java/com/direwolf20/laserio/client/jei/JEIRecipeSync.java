package com.direwolf20.laserio.client.jei;

import com.direwolf20.laserio.common.LaserIO;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * In 26.1 the client-side recipe manager no longer exposes arbitrary recipes — {@code RecipeAccess}
 * only serves property sets and stonecutter recipes. To hide our {@code _nbtclear} crafting recipes
 * in JEI we need to ask the server to sync the vanilla crafting recipes via
 * {@link OnDatapackSyncEvent#sendRecipes}; the client receives them as a {@link RecipeMap} through
 * {@link RecipesReceivedEvent}.
 */
@EventBusSubscriber(modid = LaserIO.MODID)
public class JEIRecipeSync {
    public static volatile RecipeMap CLIENT_RECIPES = RecipeMap.EMPTY;

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(RecipeType.CRAFTING);
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        CLIENT_RECIPES = event.getRecipeMap();
    }
}
