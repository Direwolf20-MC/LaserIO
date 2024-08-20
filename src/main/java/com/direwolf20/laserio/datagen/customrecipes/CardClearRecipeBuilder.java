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

    public CardClearRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public CardClearRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public CardClearRecipeBuilder requires(ItemLike item, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            this.requires(Ingredient.of(item));
        }

        return this;
    }

    public CardClearRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public CardClearRecipeBuilder requires(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            this.ingredients.add(ingredient);
        }

        return this;
    }

    public CardClearRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterionTrigger) {
        this.advancement.addCriterion(name, criterionTrigger);

        return this;
    }

    public CardClearRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;

        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumer.accept(new CardClearRecipeBuilder.Result(id, this.result, this.count, this.group == null ? "" : this.group, this.ingredients, this.advancement, new ResourceLocation(id.getNamespace(), "recipes/misc/" + id.getPath())));
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