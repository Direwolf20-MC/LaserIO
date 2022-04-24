package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import static com.direwolf20.laserio.common.blocks.LaserNode.SCREEN_LASERNODE;
import static com.direwolf20.laserio.setup.ModSetup.TAB_NAME;

public class LaserIOLanguageProvider extends LanguageProvider {
    public LaserIOLanguageProvider(DataGenerator gen, String locale) {
        super(gen, LaserIO.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TAB_NAME, "Tutorial");
        add(Registration.LaserConnector.get(), "Laser Connector");
        add(Registration.LaserNode.get(), "Laser Node");
        add(Registration.Laser_Wrench.get(), "Laser Wrench");
        add(Registration.Card_Item.get(), "Item Card");
        add(Registration.Card_Fluid.get(), "Fluid Card");
        add(Registration.Card_Energy.get(), "Energy Card");
        add(Registration.Filter_Basic.get(), "Basic Filter");
        add("message.logisticslasers.wrenchrange", "This block is too far away. Max range is 8");
        add("screen.laserio.extractamt", "Extract");
        add("screen.laserio.allowlist", "Allow");
        add("screen.laserio.comparenbt", "NBT");
        add("screen.laserio.lasernode", "Laser Node");
        add(SCREEN_LASERNODE, "Power generator");
    }
}
