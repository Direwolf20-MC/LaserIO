package com.direwolf20.laserio.client.events;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.util.DimBlockPos;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import java.awt.*;

public class RenderGUIOverlay {
    public static void renderLocation(Font font, GuiGraphics guiGraphics, LaserConnectorAdvBE laserConnectorAdvBE) {
        DimBlockPos dimBlockPos = laserConnectorAdvBE.getPartnerDimBlockPos();
        if (dimBlockPos == null) return;
        String dimString = I18n.get(dimBlockPos.levelKey.location().getPath()) + ": [" + dimBlockPos.blockPos.getX() + "," + dimBlockPos.blockPos.getY() + "," + dimBlockPos.blockPos.getZ() + "]";
        guiGraphics.drawString(font, dimString, guiGraphics.guiWidth()/2 - dimString.length()*2, guiGraphics.guiHeight()-155, Color.WHITE.getRGB(), false);
    }
}
