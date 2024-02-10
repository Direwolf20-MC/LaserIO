package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.LaserConnector;
import com.direwolf20.laserio.common.blocks.LaserConnectorAdv;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.items.*;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.items.filters.*;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.datagen.customrecipes.CardClearRecipe;
import com.direwolf20.laserio.integration.mekanism.CardChemical;
import com.direwolf20.laserio.integration.mekanism.MekanismIntegration;
import com.direwolf20.laserio.util.CardHolderItemStackHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static com.direwolf20.laserio.client.particles.ModParticles.PARTICLE_TYPES;
import static com.direwolf20.laserio.common.LaserIO.MODID;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, LaserIO.MODID);
    public static final Supplier<CardClearRecipe.Serializer> CARD_CLEAR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("cardclear", CardClearRecipe.Serializer::new);
    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LaserIO.MODID);

    public static final DeferredRegister<Item> ITEMS_MEKANISM = DeferredRegister.create(Registries.ITEM, MODID);

    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        if (MekanismIntegration.isLoaded()) {
            ITEMS_MEKANISM.register(eventBus);
        }
        BLOCK_ENTITIES.register(eventBus);
        CONTAINERS.register(eventBus);
        PARTICLE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
        ATTACHMENT_TYPES.register(eventBus);
    }

    // Some common properties for our blocks and items
    //public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);

    //Blocks
    public static final DeferredHolder<Block, LaserConnector> LaserConnector = BLOCKS.register("laser_connector", LaserConnector::new);
    public static final DeferredHolder<Item, BlockItem> LaserConnector_ITEM = ITEMS.register("laser_connector", () -> new BlockItem(LaserConnector.get(), new Item.Properties()));
    public static final DeferredHolder<Block, LaserNode> LaserNode = BLOCKS.register("laser_node", LaserNode::new);
    public static final DeferredHolder<Item, BlockItem> LaserNode_ITEM = ITEMS.register("laser_node", () -> new BlockItem(LaserNode.get(), new Item.Properties()));
    public static final DeferredHolder<Block, Block> LaserConnectorAdv = BLOCKS.register("laser_connector_advanced", LaserConnectorAdv::new);
    public static final DeferredHolder<Item, BlockItem> LaserConnectorAdv_ITEM = ITEMS.register("laser_connector_advanced", () -> new BlockItem(LaserConnectorAdv.get(), new Item.Properties()));


    //BlockEntities (Not TileEntities - Honest)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserNodeBE>> LaserNode_BE = BLOCK_ENTITIES.register("lasernode", () -> BlockEntityType.Builder.of(LaserNodeBE::new, LaserNode.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserConnectorAdvBE>> LaserConnectorAdv_BE = BLOCK_ENTITIES.register("laserconnectoradv", () -> BlockEntityType.Builder.of(LaserConnectorAdvBE::new, LaserConnectorAdv.get()).build(null));

    //Items
    public static final DeferredHolder<Item, LaserWrench> Laser_Wrench = ITEMS.register("laser_wrench", LaserWrench::new);
    public static final DeferredHolder<Item, CardHolder> Card_Holder = ITEMS.register("card_holder", CardHolder::new);
    public static final DeferredHolder<Item, CardCloner> Card_Cloner = ITEMS.register("card_cloner", CardCloner::new);

    //Cards
    public static final DeferredHolder<Item, CardItem> Card_Item = ITEMS.register("card_item", CardItem::new);
    public static final DeferredHolder<Item, CardFluid> Card_Fluid = ITEMS.register("card_fluid", CardFluid::new);
    public static final DeferredHolder<Item, CardEnergy> Card_Energy = ITEMS.register("card_energy", CardEnergy::new);
    public static final DeferredHolder<Item, CardRedstone> Card_Redstone = ITEMS.register("card_redstone", CardRedstone::new);

    //Mekanism
    public static final DeferredHolder<Item, CardChemical> Card_Chemical = ITEMS_MEKANISM.register("card_chemical", CardChemical::new);

    //Filters
    public static final DeferredHolder<Item, FilterBasic> Filter_Basic = ITEMS.register("filter_basic", FilterBasic::new);
    public static final DeferredHolder<Item, FilterCount> Filter_Count = ITEMS.register("filter_count", FilterCount::new);
    public static final DeferredHolder<Item, FilterTag> Filter_Tag = ITEMS.register("filter_tag", FilterTag::new);
    public static final DeferredHolder<Item, FilterMod> Filter_Mod = ITEMS.register("filter_mod", FilterMod::new);
    public static final DeferredHolder<Item, FilterNBT> Filter_NBT = ITEMS.register("filter_nbt", FilterNBT::new);

    //Misc
    public static final DeferredHolder<Item, LogicChipRaw> Logic_Chip_Raw = ITEMS.register("logic_chip_raw", LogicChipRaw::new);
    public static final DeferredHolder<Item, LogicChip> Logic_Chip = ITEMS.register("logic_chip", LogicChip::new);
    public static final DeferredHolder<Item, OverclockerCard> Overclocker_Card = ITEMS.register("overclocker_card", OverclockerCard::new);
    public static final DeferredHolder<Item, OverclockerNode> Overclocker_Node = ITEMS.register("overclocker_node", OverclockerNode::new);

    //Containers
    public static final DeferredHolder<MenuType<?>, MenuType<LaserNodeContainer>> LaserNode_Container = CONTAINERS.register("lasernode",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new LaserNodeContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardItemContainer>> CardItem_Container = CONTAINERS.register("carditem",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardItemContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardFluidContainer>> CardFluid_Container = CONTAINERS.register("cardfluid",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardFluidContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardEnergyContainer>> CardEnergy_Container = CONTAINERS.register("cardenergy",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardEnergyContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardRedstoneContainer>> CardRedstone_Container = CONTAINERS.register("cardredstone",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardRedstoneContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardChemicalContainer>> CardChemical_Container = CONTAINERS.register("cardchemical",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardChemicalContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<CardHolderContainer>> CardHolder_Container = CONTAINERS.register("cardholder",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CardHolderContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<FilterBasicContainer>> FilterBasic_Container = CONTAINERS.register("filterbasic",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new FilterBasicContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<FilterCountContainer>> FilterCount_Container = CONTAINERS.register("filtercount",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new FilterCountContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<FilterTagContainer>> FilterTag_Container = CONTAINERS.register("filtertag",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new FilterTagContainer(windowId, inv, inv.player, data)));
    public static final DeferredHolder<MenuType<?>, MenuType<FilterNBTContainer>> FilterNBT_Container = CONTAINERS.register("filternbt",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new FilterNBTContainer(windowId, inv, inv.player, data)));

    //Data attachments
    public static final Supplier<AttachmentType<CardHolderItemStackHandler>> CARD_HOLDER_HANDLER = ATTACHMENT_TYPES.register(
            "handler", () -> AttachmentType.serializable(() -> new CardHolderItemStackHandler(CardHolderContainer.SLOTS)).build());


    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    /*public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }*/
}
