package com.direwolf20.laserio.datagen.customrecipes;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.direwolf20.laserio.setup.Registration;
import com.google.common.collect.Lists;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class CardClearRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;

    public CardClearRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
    }

    public static CardClearRecipeBuilder shapeless(ItemLike result) {
        return new CardClearRecipeBuilder(result, 1);
    }

    public static CardClearRecipeBuilder shapeless(ItemLike result, int count) {
        return new CardClearRecipeBuilder(result, count);
    }

    public CardClearRecipeBuilder requires(TagKey<Item> pTag) {
        return this.requires(Ingredient.of(pTag));
    }

    public CardClearRecipeBuilder requires(ItemLike pItem) {
        return this.requires(pItem, 1);
    }

    public CardClearRecipeBuilder requires(ItemLike pItem, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.requires(Ingredient.of(pItem));
        }

        return this;
    }

    public CardClearRecipeBuilder requires(Ingredient pIngredient) {
        return this.requires(pIngredient, 1);
    }

    public CardClearRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.ingredients.add(pIngredient);
        }

        return this;
    }

    public CardClearRecipeBuilder unlockedBy(String pName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pName, pCriterionTrigger);

        return this;
    }

    public CardClearRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;

        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation pId) {
        this.ensureValid(pId);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pId)).rewards(AdvancementRewards.Builder.recipe(pId)).requirements(RequirementsStrategy.OR);
        consumer.accept(new CardClearRecipeBuilder.Result(pId, this.result, this.count, this.group == null ? "" : this.group, this.ingredients, this.advancement, new ResourceLocation(pId.getNamespace(), "recipes/misc/" + pId.getPath())));
    }

    private void ensureValid(ResourceLocation consumer) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + consumer);
        }
    }

    public static class Result extends ShapelessRecipeBuilder.Result {

        public Result(ResourceLocation resourceLocation, Item result, int count, String group, List<Ingredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId) {
            super(resourceLocation, result, count, group, CraftingBookCategory.MISC, ingredients, advancement, advancementId);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return Registration.CARD_CLEAR_RECIPE_SERIALIZER.get();
        }

    }

}