package com.direwolf20.laserio.client.jei;

import com.direwolf20.laserio.client.jei.ghostfilterhandlers.GhostFilterBasic;
import com.direwolf20.laserio.client.jei.ghostfilterhandlers.GhostFilterCard;
import com.direwolf20.laserio.client.jei.ghostfilterhandlers.GhostFilterCount;
import com.direwolf20.laserio.client.jei.ghostfilterhandlers.GhostFilterTag;
import com.direwolf20.laserio.client.screens.CardItemScreen;
import com.direwolf20.laserio.client.screens.FilterBasicScreen;
import com.direwolf20.laserio.client.screens.FilterCountScreen;
import com.direwolf20.laserio.client.screens.FilterTagScreen;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.LaserIORegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIIntegration implements IModPlugin {

    @Nonnull
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(LaserIO.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeManager recipeRegistry = jeiRuntime.getRecipeManager();
        List<RecipeHolder<CraftingRecipe>> hiddenRecipes = new ArrayList<>();
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Card_Item.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Card_Fluid.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Card_Energy.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Card_Redstone.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Filter_Basic.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Filter_Count.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Filter_Tag.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Filter_NBT.getId());
        addHiddenNbtClear(hiddenRecipes, LaserIORegistration.Filter_Mod.getId());
        recipeRegistry.hideRecipes(RecipeTypes.CRAFTING, hiddenRecipes);
    }

    @SuppressWarnings("unchecked")
    private static void addHiddenNbtClear(List<RecipeHolder<CraftingRecipe>> out, Identifier itemId) {
        Identifier recipeId = Identifier.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_nbtclear");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
        RecipeHolder<?> holder = JEIRecipeSync.CLIENT_RECIPES.byKey(recipeKey);
        if (holder != null && holder.value() instanceof CraftingRecipe) {
            out.add((RecipeHolder<CraftingRecipe>) holder);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(CardItemScreen.class, new GhostFilterCard());
        registration.addGhostIngredientHandler(FilterBasicScreen.class, new GhostFilterBasic());
        registration.addGhostIngredientHandler(FilterCountScreen.class, new GhostFilterCount());
        registration.addGhostIngredientHandler(FilterTagScreen.class, new GhostFilterTag());
    }
}
