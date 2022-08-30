package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOItemModels extends ItemModelProvider {
    public LaserIOItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, LaserIO.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Block Items
        withExistingParent(Registration.LaserConnector_ITEM.getId().getPath(), modLoc("block/laser_connector"));
        withExistingParent(Registration.LaserNode_ITEM.getId().getPath(), modLoc("block/laser_node"));

        //Item items
        singleTexture(Registration.Laser_Wrench.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/laser_wrench"));
        singleTexture(Registration.Card_Holder.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_holder"));
        //singleTexture(Registration.Card_Item.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_item"));
        //singleTexture(Registration.Card_Fluid.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_fluid"));
        //singleTexture(Registration.Card_Energy.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_energy"));
        singleTexture(Registration.Filter_Basic.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/filter_basic"));
        singleTexture(Registration.Filter_Count.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/filter_count"));
        singleTexture(Registration.Filter_Tag.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/filter_tag"));
        singleTexture(Registration.Filter_Mod.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/filter_mod"));
        singleTexture(Registration.Logic_Chip.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/logic_chip"));
        singleTexture(Registration.Logic_Chip_Raw.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/logic_chip_raw"));
        singleTexture(Registration.Overclocker_Card.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/overclocker_card"));
        singleTexture(Registration.Overclocker_Node.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/overclocker_node"));
    }
}
