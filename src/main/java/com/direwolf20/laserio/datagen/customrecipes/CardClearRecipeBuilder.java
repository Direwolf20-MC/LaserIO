package com.direwolf20.laserio.datagen.customrecipes;

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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

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

    public static CardClearRecipeBuilder shapeless(ItemLike p_126190_) {
        return new CardClearRecipeBuilder(p_126190_, 1);
    }

    public static CardClearRecipeBuilder shapeless(ItemLike p_126192_, int p_126193_) {
        return new CardClearRecipeBuilder(p_126192_, p_126193_);
    }

    public CardClearRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public CardClearRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public CardClearRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for (int i = 0; i < p_126213_; ++i) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public CardClearRecipeBuilder requires(Ingredient p_126185_) {
        return this.requires(p_126185_, 1);
    }

    public CardClearRecipeBuilder requires(Ingredient p_126187_, int p_126188_) {
        for (int i = 0; i < p_126188_; ++i) {
            this.ingredients.add(p_126187_);
        }

        return this;
    }

    public CardClearRecipeBuilder unlockedBy(String p_126197_, CriterionTriggerInstance p_126198_) {
        this.advancement.addCriterion(p_126197_, p_126198_);
        return this;
    }

    public CardClearRecipeBuilder group(@Nullable String p_126195_) {
        this.group = p_126195_;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> p_126205_, ResourceLocation p_126206_) {
        this.ensureValid(p_126206_);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_126206_)).rewards(AdvancementRewards.Builder.recipe(p_126206_)).requirements(RequirementsStrategy.OR);
        p_126205_.accept(new CardClearRecipeBuilder.Result(p_126206_, this.result, this.count, this.group == null ? "" : this.group, this.ingredients, this.advancement, new ResourceLocation(p_126206_.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + p_126206_.getPath())));
    }

    private void ensureValid(ResourceLocation p_126208_) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126208_);
        }
    }

    public static class Result extends ShapelessRecipeBuilder.Result {

        public Result(ResourceLocation resourceLocation, Item result, int count, String group, List<Ingredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId) {
            super(resourceLocation, result, count, group, ingredients, advancement, advancementId);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializers.CARD_CLEAR_RECIPE_SERIALIZER.get();
        }

    }
}
