package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.client.color.CardChannelTintSource;
import com.direwolf20.laserio.client.color.CardRedstoneTintSource;
import com.direwolf20.laserio.client.model.CardTransferModeProperty;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Stream;

public class LaserIOModelProvider extends ModelProvider {

    public LaserIOModelProvider(PackOutput output) {
        super(output, LaserIO.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Block items fall through to ModelProvider's BlockItem auto-fallback
        // (emits a plain ClientItem pointing at the matching block model), so
        // they don't need explicit registration — the hand-written
        // models/block/*.json files are what that fallback targets.

        // Simple flat-texture items.
        itemModels.generateFlatItem(Registration.Laser_Wrench.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Card_Holder.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Card_Cloner.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Filter_Basic.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Filter_Count.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Filter_Tag.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Filter_Mod.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Filter_NBT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Logic_Chip.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Logic_Chip_Raw.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Overclocker_Card.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Overclocker_Node.get(), ModelTemplates.FLAT_ITEM);

        // Cards — three-layer models with per-mode variants, tinted on layer 2.
        ItemTintSource cardChannelTint = new CardChannelTintSource(true);
        generateCardItem(itemModels, Registration.Card_Item.get(), "item", cardChannelTint);
        generateCardItem(itemModels, Registration.Card_Fluid.get(), "fluid", cardChannelTint);
        generateCardItem(itemModels, Registration.Card_Energy.get(), "energy", cardChannelTint);
        generateCardItem(itemModels, Registration.Card_Redstone.get(), "redstone", new CardRedstoneTintSource());
    }

    /**
     * Generates the four per-mode three-layered models for a card and wires them together
     * via a RangeSelect item model keyed on {@code laserio:mode}.
     * <p>
     * Layer0 is the base card texture. Layer1 is the mode overlay (insert/extract/stock/sensor).
     * Layer2 is the channel indicator (tinted per-stack by the given {@link ItemTintSource}).
     */
    private void generateCardItem(ItemModelGenerators itemModels, Item card, String cardType, ItemTintSource channelTint) {
        Material channelLayer = new Material(id("item/cardlayers/channel"));
        Material baseCardLayer = new Material(id("item/card_" + cardType));

        // Build one three-layered model per mode and attach the tint to the generated ClientItem.
        ItemModel.Unbaked insertModel = threeLayerModel(itemModels, card, "", baseCardLayer,
                new Material(id("item/cardlayers/insert")), channelLayer, channelTint);
        ItemModel.Unbaked extractModel = threeLayerModel(itemModels, card, "_extract", baseCardLayer,
                new Material(id("item/cardlayers/extract")), channelLayer, channelTint);
        ItemModel.Unbaked stockModel = threeLayerModel(itemModels, card, "_stock", baseCardLayer,
                new Material(id("item/cardlayers/stock")), channelLayer, channelTint);
        ItemModel.Unbaked sensorModel = threeLayerModel(itemModels, card, "_sensor", baseCardLayer,
                new Material(id("item/cardlayers/sensor")), channelLayer, channelTint);

        ItemModel.Unbaked dispatchModel = ItemModelUtils.rangeSelect(
                new CardTransferModeProperty(),
                insertModel,
                new RangeSelectItemModel.Entry(1.0F, extractModel),
                new RangeSelectItemModel.Entry(2.0F, stockModel),
                new RangeSelectItemModel.Entry(3.0F, sensorModel)
        );
        itemModels.itemModelOutput.accept(card, dispatchModel);
    }

    private ItemModel.Unbaked threeLayerModel(ItemModelGenerators itemModels, Item card, String suffix,
                                              Material layer0, Material layer1, Material layer2,
                                              ItemTintSource channelTint) {
        Identifier modelId = ModelLocationUtils.getModelLocation(card, suffix);
        ModelTemplates.THREE_LAYERED_ITEM.create(modelId, TextureMapping.layered(layer0, layer1, layer2), itemModels.modelOutput);
        return ItemModelUtils.tintedModel(modelId,
                ItemModelUtils.constantTint(-1),
                ItemModelUtils.constantTint(-1),
                channelTint);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(LaserIO.MODID, path);
    }

    // Blockstates and block models stay hand-written — don't have datagen try to validate them.
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    // Generate items/*.json ClientItem entries for every laserio item (and all block items).
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        Set<Item> ours = Set.of(
                Registration.LaserConnector_ITEM.get(),
                Registration.LaserNode_ITEM.get(),
                Registration.LaserConnectorAdv_ITEM.get(),
                Registration.Laser_Wrench.get(),
                Registration.Card_Holder.get(),
                Registration.Card_Cloner.get(),
                Registration.Card_Item.get(),
                Registration.Card_Fluid.get(),
                Registration.Card_Energy.get(),
                Registration.Card_Redstone.get(),
                Registration.Filter_Basic.get(),
                Registration.Filter_Count.get(),
                Registration.Filter_Tag.get(),
                Registration.Filter_Mod.get(),
                Registration.Filter_NBT.get(),
                Registration.Logic_Chip.get(),
                Registration.Logic_Chip_Raw.get(),
                Registration.Overclocker_Card.get(),
                Registration.Overclocker_Node.get()
        );
        return ours.stream().map(Item::builtInRegistryHolder);
    }
}
