package com.direwolf20.laserio.datagen.customrecipes;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

public class CardClearRecipe extends ShapelessRecipe {
    public CardClearRecipe(ResourceLocation resourceLocation, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(resourceLocation, group, result, ingredients);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY); //2 spots - one for filters, one for Overclockers

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack itemStack = inv.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof BaseCard) {
                List<ItemStack> containedItems = ((BaseCard) item).getContainerItems(itemStack);
                nonnulllist.set(0, containedItems.get(0));
                nonnulllist.set(1, containedItems.get(1));
            }
        }
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializers.CARD_CLEAR_RECIPE_SERIALIZER.get();
    }

    //@ObjectHolder("laserio:cardclear")
    //public static CardClearRecipe.Serializer SERIALIZER;

    public static class Serializer extends ShapelessRecipe.Serializer {
        private static final ResourceLocation NAME = new ResourceLocation("laserio", "cardclear");

        public CardClearRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            ShapelessRecipe vanilla = super.fromJson(resourceLocation, jsonObject);
            return new CardClearRecipe(vanilla.getId(), vanilla.getGroup(), vanilla.getResultItem(), vanilla.getIngredients());
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < p_44276_.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i));
                if (true || !ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        public CardClearRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf byteBuf) {
            ShapelessRecipe vanilla = super.fromNetwork(resourceLocation, byteBuf);
            return new CardClearRecipe(vanilla.getId(), vanilla.getGroup(), vanilla.getResultItem(), vanilla.getIngredients());
        }

        public void toNetwork(FriendlyByteBuf byteBuf, ShapelessRecipe shapelessRecipe) {
            super.toNetwork(byteBuf, shapelessRecipe);
        }
    }
}
