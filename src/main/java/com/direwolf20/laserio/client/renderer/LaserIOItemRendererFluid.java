package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.client.screens.CardFluidScreen;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

/** This class is used to make the numbers on items in the FilterCountContainer smaller when greater than 100 **/
public class LaserIOItemRendererFluid extends ItemRenderer {
    private final TextureManager textureManager;
    protected final AbstractContainerScreen screen;

    public LaserIOItemRendererFluid(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer, AbstractContainerScreen screen) {
        super(textureManager, modelManager, itemColors, blockEntityWithoutLevelRenderer);
        this.textureManager = textureManager;
        this.screen = screen;
    }

    private void fillRect(BufferBuilder p_115153_, int p_115154_, int p_115155_, int p_115156_, int p_115157_, int p_115158_, int p_115159_, int p_115160_, int p_115161_) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        p_115153_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        p_115153_.vertex((double) (p_115154_ + 0), (double) (p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + 0), (double) (p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + p_115156_), (double) (p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double) (p_115154_ + p_115156_), (double) (p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        //p_115153_.end();
        BufferUploader.drawWithShader(p_115153_.end());
    }

    @Override
    public void renderGuiItemDecorations(Font font, ItemStack itemstack, int x, int y, @Nullable String altText) {
        if (shouldRenderFluid(itemstack, x, y, true, false)) {
            CardFluidScreen cardFluidScreen = (CardFluidScreen) screen;
            int sloty = (int) Math.floor((y - cardFluidScreen.filterStartY) / 18);
            int slotx = (int) Math.floor((x - cardFluidScreen.filterStartX) / 18);
            int slot = ((5 * sloty) + slotx);
            ItemStack filter = ((CardFluidScreen) screen).filter;
            int totalmbAmt = FilterCount.getSlotAmount(filter, slot);
            int count = (int) Math.floor(totalmbAmt / 1000);
            int mbAmt = totalmbAmt % 1000;
            PoseStack posestack = new PoseStack();
            if (count != 0 || mbAmt != 0) {
                String textToDraw;
                textToDraw = count + "b";
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                posestack.pushPose();
                posestack.translate(x, y, 300);
                posestack.scale(0.5f, 0.5f, 0.5f);
                //mbAmt +=999;
                if (mbAmt == 0) {
                    font.drawInBatch(textToDraw, (float) (17 - font.width(textToDraw) * 0.5f), (float) (24), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                } else {
                    String textToDraw2 = mbAmt + "mb";
                    font.drawInBatch(textToDraw, (float) (17 - font.width(textToDraw) * 0.5f), (float) (14), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                    font.drawInBatch(textToDraw2, (float) (17 - font.width(textToDraw2) * 0.5f), (float) (24), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                }
                posestack.popPose();

                multibuffersource$buffersource.endBatch();
            }
        } else {
            if (!itemstack.isEmpty()) {
                PoseStack posestack = new PoseStack();
                if (itemstack.getCount() != 1 || altText != null) {
                    String textToDraw = altText == null ? String.valueOf(itemstack.getCount()) : altText;
                    posestack.translate(0.0D, 0.0D, (double) (this.blitOffset + 200.0F));
                    MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    if (itemstack.getCount() > 99) {
                        posestack.pushPose();
                        posestack.translate(x, y, 300);
                        posestack.scale(0.65f, 0.65f, 0.65f);
                        font.drawInBatch(textToDraw, (float) (17 - font.width(textToDraw) * 0.65f), (float) (17), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                        posestack.popPose();
                    } else {
                        font.drawInBatch(textToDraw, (float) (x + 19 - 2 - font.width(textToDraw)), (float) (y + 6 + 3), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                    }
                    multibuffersource$buffersource.endBatch();
                }

                if (!shouldRenderFluid(itemstack, x, y, true, true)) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    Tesselator tesselator1 = Tesselator.getInstance();
                    BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                    this.fillRect(bufferbuilder1, x, y, 16, Mth.ceil(16.0F), 255, 0, 0, 127);
                    RenderSystem.enableTexture();
                    RenderSystem.enableDepthTest();
                }

                if (itemstack.isBarVisible()) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.disableTexture();
                    RenderSystem.disableBlend();
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    int i = itemstack.getBarWidth();
                    int j = itemstack.getBarColor();
                    this.fillRect(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                    this.fillRect(bufferbuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                    RenderSystem.enableBlend();
                    RenderSystem.enableTexture();
                    RenderSystem.enableDepthTest();
                }

                LocalPlayer localplayer = Minecraft.getInstance().player;
                float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), Minecraft.getInstance().getFrameTime());
                if (f > 0.0F) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    Tesselator tesselator1 = Tesselator.getInstance();
                    BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                    this.fillRect(bufferbuilder1, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                    RenderSystem.enableTexture();
                    RenderSystem.enableDepthTest();
                }
            }
        }
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemstack, int x, int y, @Nullable String altText, float scale) {
        if (!itemstack.isEmpty()) {
            PoseStack posestack = new PoseStack();
            if (itemstack.getCount() != 1 || altText != null) {
                String textToDraw = altText == null ? String.valueOf(itemstack.getCount()) : altText;
                posestack.translate(0.0D, 0.0D, (double) (this.blitOffset + 600.0F));
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                posestack.pushPose();
                posestack.translate(0, y, 300);
                posestack.scale(scale, scale, scale);
                font.drawInBatch(textToDraw, (float) (x + 13 - font.width(textToDraw) * scale) / scale, (float) (17), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                posestack.popPose();
                multibuffersource$buffersource.endBatch();
            }

            if (itemstack.isBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                int i = itemstack.getBarWidth();
                int j = itemstack.getBarColor();
                this.fillRect(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(bufferbuilder, x + (int) (2 / scale), y + 13, (int) (i * scale), 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer localplayer = Minecraft.getInstance().player;
            float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                this.fillRect(bufferbuilder1, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
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

    @Override
    public void renderGuiItem(ItemStack pStack, int pX, int pY, BakedModel pBakedmodel) {
        if (!shouldRenderFluid(pStack, pX, pY, true, false)) {
            super.renderGuiItem(pStack, pX, pY, pBakedmodel);
            return;
        }
        LazyOptional<IFluidHandlerItem> fluidHandlerLazyOptional = FluidUtil.getFluidHandler(pStack);
        FluidStack fluidStack = FluidStack.EMPTY;
        IFluidHandler fluidHandler = fluidHandlerLazyOptional.resolve().get();
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            fluidStack = fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty())
                break;
        }
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
        vertexBuffer.vertex(pX, pY + 16.0D, zLevel).uv(uMin, vMax).endVertex();
        vertexBuffer.vertex(pX + 16.0D, pY + 16.0D, zLevel).uv(uMax, vMax).endVertex();
        vertexBuffer.vertex(pX + 16.0D, pY, zLevel).uv(uMax, vMin).endVertex();
        vertexBuffer.vertex(pX, pY, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
        posestack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
    }
}
