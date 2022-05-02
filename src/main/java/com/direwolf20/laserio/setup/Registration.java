package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.common.blockentities.LaserConnectorBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.LaserConnector;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.items.LaserWrench;
import com.direwolf20.laserio.common.items.LogicChip;
import com.direwolf20.laserio.common.items.LogicChipRaw;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.direwolf20.laserio.common.LaserIO.MODID;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
    }

    // Some common properties for our blocks and items
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);

    //Blocks
    public static final RegistryObject<Block> LaserConnector = BLOCKS.register("laser_connector", LaserConnector::new);
    public static final RegistryObject<Item> LaserConnector_ITEM = fromBlock(LaserConnector);
    public static final RegistryObject<LaserNode> LaserNode = BLOCKS.register("laser_node", LaserNode::new);
    public static final RegistryObject<Item> LaserNode_ITEM = fromBlock(LaserNode);

    //BlockEntities (Not TileEntities - Honest)
    public static final RegistryObject<BlockEntityType<LaserNodeBE>> LaserNode_BE = BLOCK_ENTITIES.register("lasernode", () -> BlockEntityType.Builder.of(LaserNodeBE::new, LaserNode.get()).build(null));
    public static final RegistryObject<BlockEntityType<LaserConnectorBE>> LaserConnector_BE = BLOCK_ENTITIES.register("laserconnector", () -> BlockEntityType.Builder.of(LaserConnectorBE::new, LaserConnector.get()).build(null));

    //Items
    public static final RegistryObject<Item> Laser_Wrench = ITEMS.register("laser_wrench", LaserWrench::new);
    public static final RegistryObject<Item> Card_Energy = ITEMS.register("card_energy", CardEnergy::new);
    public static final RegistryObject<Item> Card_Fluid = ITEMS.register("card_fluid", CardFluid::new);
    public static final RegistryObject<Item> Card_Item = ITEMS.register("card_item", CardItem::new);
    public static final RegistryObject<Item> Filter_Basic = ITEMS.register("filter_basic", FilterBasic::new);
    public static final RegistryObject<Item> Filter_Count = ITEMS.register("filter_count", FilterCount::new);
    public static final RegistryObject<Item> Logic_Chip = ITEMS.register("logic_chip", LogicChip::new);
    public static final RegistryObject<Item> Logic_Chip_Raw = ITEMS.register("logic_chip_raw", LogicChipRaw::new);

    //Containers
    public static final RegistryObject<MenuType<LaserNodeContainer>> LaserNode_Container = CONTAINERS.register("lasernode",
            () -> IForgeMenuType.create((windowId, inv, data) -> new LaserNodeContainer(windowId, inv, inv.player, data)));
    public static final RegistryObject<MenuType<CardItemContainer>> CardItem_Container = CONTAINERS.register("carditem",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CardItemContainer(windowId, inv, inv.player, data)));
    public static final RegistryObject<MenuType<FilterBasicContainer>> FilterBasic_Container = CONTAINERS.register("filterbasic",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FilterBasicContainer(windowId, inv, inv.player, data)));
    public static final RegistryObject<MenuType<FilterCountContainer>> FilterCount_Container = CONTAINERS.register("filtercount",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FilterCountContainer(windowId, inv, inv.player, data)));

    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }
}
