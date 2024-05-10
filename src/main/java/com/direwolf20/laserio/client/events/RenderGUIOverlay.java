package com.direwolf20.laserio.client.events;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.GlobalPos;

import java.awt.*;

public class RenderGUIOverlay {
    public static void renderLocation(Font font, GuiGraphics guiGraphics, LaserConnectorAdvBE laserConnectorAdvBE) {
        GlobalPos dimBlockPos = laserConnectorAdvBE.getPartnerGlobalPos();
        if (dimBlockPos == null) return;
        String dimString = I18n.get(dimBlockPos.dimension().location().getPath()) + ": [" + dimBlockPos.pos().getX() + "," + dimBlockPos.pos().getY() + "," + dimBlockPos.pos().getZ() + "]";
        guiGraphics.drawString(font, dimString, guiGraphics.guiWidth() / 2 - dimString.length() * 2, guiGraphics.guiHeight() / 20 + guiGraphics.guiHeight() / 2, Color.WHITE.getRGB(), false);
    }
}
