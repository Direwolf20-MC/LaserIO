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
import com.direwolf20.laserio.setup.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIIntegration implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(LaserIO.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeManager recipeRegistry = jeiRuntime.getRecipeManager();
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<CraftingRecipe> hiddenRecipes = new ArrayList<>();
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Card_Item.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Card_Fluid.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Card_Energy.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Card_Redstone.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Filter_Basic.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Filter_Count.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Filter_Tag.getId() + "_nbtclear")).get());
        hiddenRecipes.add((CraftingRecipe) recipeManager.byKey(new ResourceLocation(Registration.Filter_Mod.getId() + "_nbtclear")).get());
        recipeRegistry.hideRecipes(RecipeTypes.CRAFTING, hiddenRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(CardItemScreen.class, new GhostFilterCard());
        registration.addGhostIngredientHandler(FilterBasicScreen.class, new GhostFilterBasic());
        registration.addGhostIngredientHandler(FilterCountScreen.class, new GhostFilterCount());
        registration.addGhostIngredientHandler(FilterTagScreen.class, new GhostFilterTag());
    }
}
