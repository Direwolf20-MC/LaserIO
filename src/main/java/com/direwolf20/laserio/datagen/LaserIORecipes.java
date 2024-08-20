package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.datagen.customrecipes.CardClearRecipeBuilder;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class LaserIORecipes extends RecipeProvider implements IConditionBuilder {

    public LaserIORecipes(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    	//Crafting Components
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Logic_Chip_Raw.get(), 4)
                .pattern("rgr")
                .pattern("cqc")
                .pattern("rgr")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('q', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('c', Items.CLAY_BALL)
                .group("laserio")
                .unlockedBy("has_quartz", InventoryChangeTrigger.TriggerInstance.hasItems(Items.QUARTZ_BLOCK))
                .save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Registration.Logic_Chip_Raw.get()), RecipeCategory.MISC,
                        Registration.Logic_Chip.get(), 1.0f, 100)
                .unlockedBy("has_raw_chip", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.Logic_Chip_Raw.get()).build()))
                .save(consumer);

        //Blocks
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.LaserConnector.get(), 1)
                .pattern(" g ")
                .pattern("rbr")
                .pattern("iii")
                .define('g', Tags.Items.GLASS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Registration.Logic_Chip.get())
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.LaserConnectorAdv.get(), 1)
                .pattern("ede")
                .pattern("rbr")
                .pattern("iii")
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('i', Tags.Items.INGOTS_GOLD)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Registration.LaserConnector_ITEM.get())
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.LaserNode.get(), 1)
                .pattern("igi")
                .pattern("gbg")
                .pattern("igi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Tags.Items.GLASS_PANES)
                .define('b', Registration.LaserConnector.get())
                .group("laserio")
                .unlockedBy("has_logic_connector", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.LaserConnector.get()))
                .save(consumer);


        //Misc Items
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Laser_Wrench.get(), 1)
                .pattern("i i")
                .pattern(" b ")
                .pattern(" i ")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Holder.get(), 1)
                .pattern("i i")
                .pattern("cbc")
                .pattern("i i")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Cloner.get(), 1)
                .pattern("i i")
                .pattern("cbc")
                .pattern("i i")
                .define('b', Registration.Logic_Chip.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Items.PAPER)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        //Cards
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Item.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Tags.Items.GEMS_LAPIS)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Fluid.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Items.BUCKET)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Energy.get(), 1)
                .pattern("rlr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('l', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Redstone.get(), 1)
                .pattern("rrr")
                .pattern("qpq")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        
        //Mekanism Card
        ConditionalRecipe.builder()
        		.addCondition(modLoaded("mekanism"))
        		.addRecipe(t -> 
        			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Card_Chemical.get(), 1)
        					.pattern("rlr")
        					.pattern("qpq")
        					.pattern("ggg")
        					.define('r', Tags.Items.DUSTS_REDSTONE)
        					.define('p', Registration.Logic_Chip.get())
        					.define('g', Tags.Items.NUGGETS_GOLD)
        					.define('l', LaserIOItemTags.CIRCUITS_BASIC)
        					.define('q', Tags.Items.GEMS_QUARTZ)
        					.group("laserio")
        					.unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
        					.save(t))
        		.generateAdvancement(new ResourceLocation(LaserIO.MODID, "recipes/misc/" + Registration.Card_Chemical.getId().getPath()))
        		.build(consumer, Registration.Card_Chemical.getId());
        
        //Filters
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Filter_Basic.get(), 4)
                .pattern("igi")
                .pattern("gqg")
                .pattern("igi")
                .define('i', Items.IRON_BARS)
                .define('q', Registration.Logic_Chip.get())
                .define('g', Tags.Items.GLASS_PANES)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Count.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.OBSERVER, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Basic.get()))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Tag.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.PAPER, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Basic.get()))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_NBT.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.WHITE_WOOL, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Basic.get()))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Mod.get(), 1)
                .requires(Registration.Filter_Basic.get(), 1)
                .requires(Items.BOOK, 1)
                .group("laserio")
                .unlockedBy("has_filter_basic", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Basic.get()))
                .save(consumer);

        //Upgrades
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Overclocker_Card.get(), 1)
                .pattern(" g ")
                .pattern("rpr")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.INGOTS_GOLD)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Overclocker_Node.get(), 1)
                .pattern(" g ")
                .pattern("rpr")
                .pattern("ggg")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('p', Registration.Logic_Chip.get())
                .define('g', Tags.Items.GEMS_DIAMOND)
                .group("laserio")
                .unlockedBy("has_logic_chip", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Logic_Chip.get()))
                .save(consumer);

        //NBT Clearing Recipes
        CardClearRecipeBuilder.shapeless(Registration.Card_Item.get())
                .requires(Registration.Card_Item.get())
                .group("laserio")
                .unlockedBy("has_card_item", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Card_Item.get()))
                .save(consumer, Registration.Card_Item.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(Registration.Card_Fluid.get())
                .requires(Registration.Card_Fluid.get())
                .group("laserio")
                .unlockedBy("has_card_fluid", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Card_Fluid.get()))
                .save(consumer, Registration.Card_Fluid.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(Registration.Card_Energy.get())
                .requires(Registration.Card_Energy.get())
                .group("laserio")
                .unlockedBy("has_card_energy", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Card_Energy.get()))
                .save(consumer, Registration.Card_Energy.getId() + "_nbtclear");
        CardClearRecipeBuilder.shapeless(Registration.Card_Redstone.get())
                .requires(Registration.Card_Redstone.get())
                .group("laserio")
                .unlockedBy("has_card_redstone", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Card_Redstone.get()))
                .save(consumer, Registration.Card_Redstone.getId() + "_nbtclear");
        
        //Mekanism NBT Clearing Recipe
        ConditionalRecipe.builder()
				.addCondition(modLoaded("mekanism"))
				.addRecipe(t -> 
					CardClearRecipeBuilder.shapeless(Registration.Card_Chemical.get())
							.requires(Registration.Card_Chemical.get())
							.group("laserio")
							.unlockedBy("has_card_chemical", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Card_Chemical.get()))
							.save(t))
				.generateAdvancement(new ResourceLocation(LaserIO.MODID, "recipes/misc/" + Registration.Card_Chemical.getId().getPath() + "_nbtclear"))
				.build(consumer, Registration.Card_Chemical.getId().withSuffix("_nbtclear"));
        
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Basic.get())
                .requires(Registration.Filter_Basic.get())
                .group("laserio")
                .unlockedBy("has_filter_basic", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Basic.get()))
                .save(consumer, Registration.Filter_Basic.getId() + "_nbtclear");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Count.get())
                .requires(Registration.Filter_Count.get())
                .group("laserio")
                .unlockedBy("has_filter_count", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Count.get()))
                .save(consumer, Registration.Filter_Count.getId() + "_nbtclear");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Tag.get())
                .requires(Registration.Filter_Tag.get())
                .group("laserio")
                .unlockedBy("has_filter_tag", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Tag.get()))
                .save(consumer, Registration.Filter_Tag.getId() + "_nbtclear");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_NBT.get())
                .requires(Registration.Filter_NBT.get())
                .group("laserio")
                .unlockedBy("has_nbt_tag", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_NBT.get()))
                .save(consumer, Registration.Filter_NBT.getId() + "_nbtclear");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.Filter_Mod.get())
                .requires(Registration.Filter_Mod.get())
                .group("laserio")
                .unlockedBy("has_filter_mod", InventoryChangeTrigger.TriggerInstance.hasItems(Registration.Filter_Mod.get()))
                .save(consumer, Registration.Filter_Mod.getId() + "_nbtclear");

    }
}