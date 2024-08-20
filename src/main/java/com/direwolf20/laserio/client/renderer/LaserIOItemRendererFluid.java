package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.client.screens.CardFluidScreen;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/** This class is used to make the numbers on items in the FilterCountContainer smaller when greater than 100 **/
public class LaserIOItemRendererFluid extends ItemRenderer {
    private final TextureManager textureManager;
    protected final AbstractContainerScreen screen;

    public LaserIOItemRendererFluid(Minecraft minecraft, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer, AbstractContainerScreen screen) {
        super(minecraft, textureManager, modelManager, itemColors, blockEntityWithoutLevelRenderer);
        this.textureManager = textureManager;
        this.screen = screen;
    }

    public boolean shouldRenderFluid(ItemStack pStack, int pX, int pY, boolean includeCarried, boolean reverseBounds) {
        if (!(screen instanceof CardFluidScreen)) {
            return reverseBounds;
        }
        CardFluidScreen cardFluidScreen = (CardFluidScreen) screen;
        if (cardFluidScreen.getMenu().getCarried().equals(pStack)) {
            if (includeCarried)
                return reverseBounds;
        }
        if (reverseBounds) {
            return !(MiscTools.inBounds(cardFluidScreen.filterStartX, cardFluidScreen.filterStartY, cardFluidScreen.filterEndX - cardFluidScreen.filterStartX, cardFluidScreen.filterEndY - cardFluidScreen.filterStartY, pX, pY));
        } else {
            if (!MiscTools.inBounds(cardFluidScreen.filterStartX, cardFluidScreen.filterStartY, cardFluidScreen.filterEndX - cardFluidScreen.filterStartX, cardFluidScreen.filterEndY - cardFluidScreen.filterStartY, pX, pY)) {
                return reverseBounds;
            }
        }
        LazyOptional<IFluidHandlerItem> fluidHandlerLazyOptional = FluidUtil.getFluidHandler(pStack);
        if (!fluidHandlerLazyOptional.isPresent()) {
            return reverseBounds;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        IFluidHandler fluidHandler = fluidHandlerLazyOptional.resolve().get();
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            fluidStack = fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty())
                break;
        }
        if (fluidStack.isEmpty()) {
            return reverseBounds;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return reverseBounds;
        }
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite fluidStillSprite = null;
        if (fluidStill != null) {
            fluidStillSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        }

        if (fluidStillSprite == null) {
            return reverseBounds;
        }
        return !reverseBounds;
    }

    public void renderFluid(FluidStack fluidStack, int pX, int pY, int size) {
        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
        int zLevel = 100;
        float uMin = fluidStillSprite.getU0();
        float uMax = fluidStillSprite.getU1();
        float vMin = fluidStillSprite.getV0();
        float vMax = fluidStillSprite.getV1();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuilder();

        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        vertexBuffer.vertex(pX, pY + size, zLevel).uv(uMin, vMax).endVertex();
        vertexBuffer.vertex(pX + size, pY + size, zLevel).uv(uMax, vMax).endVertex();
        vertexBuffer.vertex(pX + size, pY, zLevel).uv(uMax, vMin).endVertex();
        vertexBuffer.vertex(pX, pY, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
        posestack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}