package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.datagen.customrecipes.CardClearRecipeBuilder;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class LaserIORecipes extends RecipeProvider {

    public LaserIORecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        //Crafting Components
        this.shaped(RecipeCategory.MISC, Registration.Logic_Chip_Raw.get(), 4)
                .pattern("rgr")
                .pattern("cqc")
                .pattern("rgr")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('q', Blocks.QUARTZ_BLOCK)
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('c', Items.CLAY_BALL)
                .group("laserio")
                .unlockedBy("has_quartz", this.has(Items.QUARTZ_BLOCK))
                .save(this.output);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Registration.Logic_Chip_Raw.get()), RecipeCategory.MISC,
                        CookingBookCategory.MISC, Registration.Logic_Chip.get(), 1.0f, 100)
                .unlockedBy("has_raw_chip", this.has(Registration.Logic_Chip_Raw.get()))
                .save(this.output);

        //Blocks
        this.shaped(RecipeCategory.MISC, Registration.LaserConnector.get(), 1)
                .pattern(" g ")
                .pattern("rbr")
                .pattern("iii")
                .define('g', Tags.Items.GLASS_BLOCKS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Registration.Logic_Chip.get())
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.LaserConnectorAdv.get(), 1)
                .pattern("ede")
                .pattern("rbr")
                .pattern("iii")
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('i', Tags.Items.INGOTS_GOLD)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Registration.LaserConnector_ITEM.get())
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.LaserNode.get(), 1)
                .pattern("igi")
                .pattern("gbg")
                .pattern("igi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Tags.Items.GLASS_PANES)
                .define('b', Registration.LaserConnector.get())
                .group("laserio")
                .unlockedBy("has_logic_connector", this.has(Registration.LaserConnector.get()))
                .save(this.output);

        //Misc Items
        this.shaped(RecipeCategory.MISC, Registration.Laser_Wrench.get(), 1)
                .pattern("i i")
                .pattern(" b ")
                .pattern(" i ")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Card_Holder.get(), 1)
                .pattern("i i")
                .pattern("cbc")
                .pattern("i i")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Card_Cloner.get(), 1)
                .pattern("i i")
                .pattern("cbc")
                .pattern("i i")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Items.PAPER)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);

        //Cards
        this.shaped(RecipeCategory.MISC, Registration.Card_Item.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Tags.Items.GEMS_LAPIS)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Card_Fluid.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Items.BUCKET)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Card_Energy.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Card_Redstone.get(), 1)
                .pattern("rrr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        // TODO(port, mek): re-enable Chemical Card recipe when Mekanism 26.1 ships.

        //Filters
        this.shaped(RecipeCategory.MISC, Registration.Filter_Basic.get(), 4)
                .pattern("igi")
                .pattern("gqg")
                .pattern("igi")
                .define('i', Items.IRON_BARS)
                .define('q', Registration.Logic_Chip.get())
                .define('g', Tags.Items.GLASS_PANES)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Count.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.OBSERVER, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", this.has(Registration.Filter_Basic.get()))
                .save(this.output);
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Tag.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.PAPER, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", this.has(Registration.Filter_Basic.get()))
                .save(this.output);
        this.shapeless(RecipeCategory.MISC, Registration.Filter_NBT.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.WHITE_WOOL, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", this.has(Registration.Filter_Basic.get()))
                .save(this.output);
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Mod.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.BOOK, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", this.has(Registration.Filter_Basic.get()))
                .save(this.output);

        //Upgrades
        this.shaped(RecipeCategory.MISC, Registration.Overclocker_Card.get(), 1)
                .pattern(" g ")
                .pattern("rpr")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.INGOTS_GOLD)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);
        this.shaped(RecipeCategory.MISC, Registration.Overclocker_Node.get(), 1)
                .pattern(" g ")
                .pattern("rpr")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.GEMS_DIAMOND)
                .group("laserio")
                .unlockedBy("has_logic_chip", this.has(Registration.Logic_Chip.get()))
                .save(this.output);

        //NBT Clearing Recipes
        CardClearRecipeBuilder.shapeless(this.items, Registration.Card_Item.get())
                .requires(Registration.Card_Item.get())
                .group("laserio")
                .unlockedBy("has_card_item", this.has(Registration.Card_Item.get()))
                .save(this.output, Registration.Card_Item.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(this.items, Registration.Card_Fluid.get())
                .requires(Registration.Card_Fluid.get())
                .group("laserio")
                .unlockedBy("has_card_fluid", this.has(Registration.Card_Fluid.get()))
                .save(this.output, Registration.Card_Fluid.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(this.items, Registration.Card_Energy.get())
                .requires(Registration.Card_Energy.get())
                .group("laserio")
                .unlockedBy("has_card_energy", this.has(Registration.Card_Energy.get()))
                .save(this.output, Registration.Card_Energy.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(this.items, Registration.Card_Redstone.get())
                .requires(Registration.Card_Redstone.get())
                .group("laserio")
                .unlockedBy("has_card_redstone", this.has(Registration.Card_Redstone.get()))
                .save(this.output, Registration.Card_Redstone.getId() + "_nbtclear");
        // TODO(port, mek): re-enable Chemical Card NBT-clear recipe when Mekanism 26.1 ships.

        this.shapeless(RecipeCategory.MISC, Registration.Filter_Basic.get())
                .requires(Registration.Filter_Basic.get())
                .group("laserio")
                .unlockedBy("has_filter_basic", this.has(Registration.Filter_Basic.get()))
                .save(this.output, Registration.Filter_Basic.getId() + "_nbtclear");
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Count.get())
                .requires(Registration.Filter_Count.get())
                .group("laserio")
                .unlockedBy("has_filter_count", this.has(Registration.Filter_Count.get()))
                .save(this.output, Registration.Filter_Count.getId() + "_nbtclear");
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Tag.get())
                .requires(Registration.Filter_Tag.get())
                .group("laserio")
                .unlockedBy("has_filter_tag", this.has(Registration.Filter_Tag.get()))
                .save(this.output, Registration.Filter_Tag.getId() + "_nbtclear");
        this.shapeless(RecipeCategory.MISC, Registration.Filter_NBT.get())
                .requires(Registration.Filter_NBT.get())
                .group("laserio")
                .unlockedBy("has_nbt_tag", this.has(Registration.Filter_NBT.get()))
                .save(this.output, Registration.Filter_NBT.getId() + "_nbtclear");
        this.shapeless(RecipeCategory.MISC, Registration.Filter_Mod.get())
                .requires(Registration.Filter_Mod.get())
                .group("laserio")
                .unlockedBy("has_filter_mod", this.has(Registration.Filter_Mod.get()))
                .save(this.output, Registration.Filter_Mod.getId() + "_nbtclear");
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new LaserIORecipes(registries, output);
        }

        @Override
        public String getName() {
            return "LaserIO Recipes";
        }
    }
}
