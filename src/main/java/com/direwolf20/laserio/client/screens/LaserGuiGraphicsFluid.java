package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.util.MiscTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.joml.Matrix3x2fStack;

public final class LaserGuiGraphicsFluid {
    private LaserGuiGraphicsFluid() {
    }

    public static boolean isFluidFilterSlot(CardFluidScreen screen, int x, int y) {
        return MiscTools.inBounds(screen.filterStartX, screen.filterStartY, screen.filterEndX - screen.filterStartX, screen.filterEndY - screen.filterStartY, x, y);
    }

    public static FluidStack getFluidForStack(ItemStack stack) {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        return FluidUtil.getFirstStackContained(stack);
    }

    public static void renderFluidSprite(GuiGraphicsExtractor guiGraphics, FluidStack fluidStack, int x, int y, int size) {
        if (fluidStack.isEmpty()) return;
        Fluid fluid = fluidStack.getFluid();
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
        FluidTintSource tintSource = fluidModel.fluidTintSource();
        int tint = tintSource != null ? tintSource.colorAsStack(fluidStack) : -1;
        if ((tint & 0xFF000000) == 0) tint |= 0xFF000000;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, size, size, tint);
    }

    public static void renderFilterCountOverlay(GuiGraphicsExtractor guiGraphics, ItemStack filter, int slot, int x, int y) {
        int totalmbAmt = FilterCount.getSlotAmount(filter, slot);
        int count = FilterCount.getSlotCount(filter, slot);
        int mbAmt = totalmbAmt % 1000;
        if (count == 0 && mbAmt == 0) return;
        Font font = Minecraft.getInstance().font;
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(0.5f, 0.5f);
        String bucketText = count + "b";
        if (mbAmt == 0) {
            guiGraphics.text(font, bucketText, (int) (17 - font.width(bucketText) * 0.5f), 24, 0xFFFFFFFF, true);
        } else {
            String mbText = mbAmt + "mb";
            guiGraphics.text(font, bucketText, (int) (17 - font.width(bucketText) * 0.5f), 14, 0xFFFFFFFF, true);
            guiGraphics.text(font, mbText, (int) (17 - font.width(mbText) * 0.5f), 24, 0xFFFFFFFF, true);
        }
        pose.popMatrix();
    }

    public static int slotIndexFromCoords(CardFluidScreen screen, int x, int y) {
        int sloty = (int) Math.floor((y - screen.filterStartY) / 18.0);
        int slotx = (int) Math.floor((x - screen.filterStartX) / 18.0);
        return (5 * sloty) + slotx;
    }
}
