package com.direwolf20.laserio.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;

/** This class is used to make the numbers on items in the FilterCountContainer smaller when greater than 100 **/
public class LaserIOItemRenderer extends ItemRenderer {
    public LaserIOItemRenderer(Minecraft minecraft, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer) {
        super(minecraft, textureManager, modelManager, itemColors, blockEntityWithoutLevelRenderer);
    }

}