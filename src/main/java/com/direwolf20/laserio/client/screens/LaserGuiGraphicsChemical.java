package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.direwolf20.laserio.integration.mekanism.MekanismStatics.getFirstChemicalOnItemStack;

public class LaserGuiGraphicsChemical extends GuiGraphics {
    public Minecraft minecraft;
    protected final AbstractContainerScreen<?> screen;

    public LaserGuiGraphicsChemical(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource, AbstractContainerScreen<?> screen) {
        super(minecraft, bufferSource);
        this.minecraft = minecraft;
        this.screen = screen;
    }

    @Override
    public void renderItemDecorations(Font font, ItemStack itemstack, int x, int y, @Nullable String altText) {
        if (shouldRenderChemical(itemstack, x, y, true, false)) {
            CardChemicalScreen cardChemicalScreen = (CardChemicalScreen) screen;
            int sloty = Mth.floor((y - cardChemicalScreen.filterStartY) / 18D);
            int slotx = Mth.floor((x - cardChemicalScreen.filterStartX) / 18D);
            int slot = ((5 * sloty) + slotx);
            ItemStack filter = ((CardChemicalScreen) screen).filter;
            int totalmbAmt = FilterCount.getSlotAmount(filter, slot);
            int count = FilterCount.getSlotCount(filter, slot);
            int mbAmt = totalmbAmt % 1000;
            PoseStack posestack = pose();
            if (count != 0 || mbAmt != 0) {
                String textToDraw;
                textToDraw = count + "b";
                posestack.pushPose();
                posestack.translate(x, y, 200);
                posestack.scale(0.5f, 0.5f, 0.5f);
                if (mbAmt == 0) {
                    this.drawString(font, textToDraw, 17 - font.width(textToDraw) * 0.5f, 24F, 16777215, true);
                } else {
                    String textToDraw2 = mbAmt + "mb";
                    this.drawString(font, textToDraw, 17 - font.width(textToDraw) * 0.5f, 14F, 16777215, true);
                    this.drawString(font, textToDraw2, 17 - font.width(textToDraw2) * 0.5f, 24F, 16777215, true);
                }
                posestack.popPose();
            }
        } else {
            if (!itemstack.isEmpty()) {
                PoseStack posestack = pose();
                if (itemstack.getCount() != 1 || altText != null) {
                    String textToDraw = altText == null ? String.valueOf(itemstack.getCount()) : altText;
                    posestack.translate(0.0D, 0.0D, 200.0F);
                    if (itemstack.getCount() > 99) {
                        posestack.pushPose();
                        posestack.translate(x, y, 300);
                        posestack.scale(0.65f, 0.65f, 0.65f);
                        this.drawString(font, textToDraw, 17 - font.width(textToDraw) * 0.65f, 17F, 0xFFFFFF, true);
                        posestack.popPose();
                    } else {
                        this.drawString(font, textToDraw, x + 19 - 2 - font.width(textToDraw), y + 6 + 3, 0xFFFFFF, true);
                    }
                }

                if (!shouldRenderChemical(itemstack, x, y, true, true)) {
                    fill(RenderType.guiOverlay(), x, y, x + 16, y + Mth.ceil(16.0F), 0x7FFF0000);
                }

                if (itemstack.isBarVisible()) {
                    int i = itemstack.getBarWidth();
                    int j = itemstack.getBarColor();
                    fill(RenderType.guiOverlay(), x + 2, y + 13, x + 15, y + 15, 0xFF000000);
                    fill(RenderType.guiOverlay(), x + 2, y + 13, x + 2 + i, y + 14, j | 0xFF000000);
                }

                LocalPlayer localplayer = Minecraft.getInstance().player;
                float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), Minecraft.getInstance().getFrameTimeNs());
                if (f > 0.0F) {
                    int yMin = y + Mth.floor(16.0F * (1.0F - f));
                    int yMax = yMin + Mth.ceil(16.0F * f);
                    fill(RenderType.guiOverlay(), x, yMin, x + 16, yMax, Integer.MAX_VALUE);
                }
            }
        }
    }

    private boolean shouldRenderChemical(ItemStack pStack, int pX, int pY, boolean includeCarried, boolean reverseBounds) {
        if (!(screen instanceof CardChemicalScreen cardChemicalScreen)) {
            return reverseBounds;
        }
        if (includeCarried && cardChemicalScreen.getMenu().getCarried().equals(pStack)) {
            return reverseBounds;
        }
        if (reverseBounds) {
            return !(MiscTools.inBounds(cardChemicalScreen.filterStartX, cardChemicalScreen.filterStartY, cardChemicalScreen.filterEndX - cardChemicalScreen.filterStartX, cardChemicalScreen.filterEndY - cardChemicalScreen.filterStartY, pX, pY));
        } else if (!MiscTools.inBounds(cardChemicalScreen.filterStartX, cardChemicalScreen.filterStartY, cardChemicalScreen.filterEndX - cardChemicalScreen.filterStartX, cardChemicalScreen.filterEndY - cardChemicalScreen.filterStartY, pX, pY)) {
            return false;
        }
        ChemicalStack chemicalStack = getFirstChemicalOnItemStack(pStack);
        return !chemicalStack.isEmpty() && Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(chemicalStack.getChemical().getIcon()) != null;
    }

    @Override
    public void renderItem(ItemStack pStack, int pX, int pY, int something) {
        if (!shouldRenderChemical(pStack, pX, pY, true, false)) {
            super.renderItem(pStack, pX, pY, something);
            return;
        }
        ChemicalStack chemicalStack = getFirstChemicalOnItemStack(pStack); //We checked above to ensure this isn't empty
        TextureAtlasSprite chemicalSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(chemicalStack.getChemical().getIcon());
        int chemicalColor = chemicalStack.getChemicalColorRepresentation();

        float red = (float) (chemicalColor >> 16 & 255) / 255.0F;
        float green = (float) (chemicalColor >> 8 & 255) / 255.0F;
        float blue = (float) (chemicalColor & 255) / 255.0F;
        blit(pX, pY, 100, 16, 16, chemicalSprite, red, green, blue, 1.0f);
    }
}
