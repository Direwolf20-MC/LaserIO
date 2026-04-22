package com.direwolf20.laserio.datagen.customrecipes;

import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class CardClearRecipe extends NormalCraftingRecipe {
    final ItemStackTemplate result;
    final List<Ingredient> ingredients;
    private final boolean isSimple;

    public CardClearRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!isSimple) {
            var nonEmptyItems = new java.util.ArrayList<ItemStack>(input.ingredientCount());
            for (var item : input.items())
                if (!item.isEmpty())
                    nonEmptyItems.add(item);
            return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(input.getItem(0))
                    : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput) {
        ItemStack itemStack = craftingInput.getItem(0);
        if (itemStack.getItem() instanceof BaseCard) {
            CardItemHandler cardItemHandler = BaseCard.getInventory(itemStack);
            ItemStack slot0 = cardItemHandler.getResource(0).toStack(cardItemHandler.getAmountAsInt(0));
            if (!slot0.isEmpty()) {
                return slot0;
            }
            ItemStack slot1 = cardItemHandler.getResource(1).toStack(cardItemHandler.getAmountAsInt(1));
            if (!slot1.isEmpty()) {
                return slot1;
            }
        }
        return this.result.create();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(this.ingredients);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(1, ItemStack.EMPTY);

        ItemStack itemStack = craftingInput.getItem(0);
        if (itemStack.getItem() instanceof BaseCard) {
            CardItemHandler cardItemHandler = BaseCard.getInventory(itemStack);
            ItemStack slot0 = cardItemHandler.getResource(0).toStack(cardItemHandler.getAmountAsInt(0));
            if (!slot0.isEmpty()) {
                ItemStack returnStack = itemStack.copy();
                BaseCard.getInventory(returnStack).set(0, ItemResource.EMPTY, 0);
                nonnulllist.set(0, returnStack);
                return nonnulllist;
            }
            ItemStack slot1 = cardItemHandler.getResource(1).toStack(cardItemHandler.getAmountAsInt(1));
            if (!slot1.isEmpty()) {
                ItemStack returnStack = itemStack.copy();
                BaseCard.getInventory(returnStack).set(1, ItemResource.EMPTY, 0);
                nonnulllist.set(0, returnStack);
                return nonnulllist;
            }
        } else {
            ItemStackTemplate remainder = itemStack.getItem().getCraftingRemainder();
            if (remainder != null) {
                nonnulllist.set(0, remainder.create());
            }
        }
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<CardClearRecipe> getSerializer() {
        return Registration.CARD_CLEAR_RECIPE_SERIALIZER.get();
    }

    public static final MapCodec<CardClearRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
                    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9))
                            .fieldOf("ingredients")
                            .flatXmap(
                                    list -> list.isEmpty()
                                            ? DataResult.error(() -> "No ingredients for shapeless recipe")
                                            : DataResult.success(list),
                                    DataResult::success
                            )
                            .forGetter(r -> r.ingredients)
            ).apply(i, CardClearRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CardClearRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            CardClearRecipe::new
    );

    public static final RecipeSerializer<CardClearRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
