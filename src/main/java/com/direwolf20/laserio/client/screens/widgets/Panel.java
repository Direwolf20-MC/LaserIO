package com.direwolf20.laserio.client.screens.widgets;

import java.awt.Color;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Panel extends AbstractWidget {
    public static final Panel.OnTooltip NO_TOOLTIP = (p_93740_, p_93741_, p_93742_, p_93743_) -> {
    };
    protected final Panel.OnTooltip onTooltip;

    public Panel(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pMessage, NO_TOOLTIP);
    }

    public Panel(int pX, int pY, int pWidth, int pHeight, Component pMessage, Panel.OnTooltip pOnTooltip) {
        super(pX, pY, pWidth, pHeight, pMessage);
        onTooltip = NO_TOOLTIP;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!visible)
            return;
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height;

        int outline = Color.BLACK.getRGB();
        int highLight = Color.WHITE.getRGB();
        int shadow = Color.DARK_GRAY.getRGB();
        int background = Color.LIGHT_GRAY.getRGB();
        int style = 1;
        if (style == 0){
            int outerInset = 1;
            int innerInset = 3;
            fill(pPoseStack, x, y, x + width, y + height, outline);
            fill(pPoseStack, x + outerInset, y + outerInset, x + width - innerInset, y + height - innerInset, highLight);
            fill(pPoseStack, x + innerInset, y + innerInset, x + width - outerInset, y + height - outerInset, shadow);
            fill(pPoseStack, x + innerInset, y + innerInset, x + width - innerInset, y + height - innerInset, background);
        } else {
            hLine(pPoseStack, x + 2, x + width - 4, y, outline);
            hLine(pPoseStack, x + 3, x + width - 3, y + height - 1, outline);
            vLine(pPoseStack, x, y + 1, y + height - 3, outline);
            vLine(pPoseStack, x + width - 1, y + 2, y + height - 2, outline);
            pixel(pPoseStack, x + width - 3, y + 1, outline);
            pixel(pPoseStack, x + width - 2, y + 2, outline);
            pixel(pPoseStack, x + 1, y + height - 3, outline);
            pixel(pPoseStack, x + 2, y + height - 2, outline);
            pixel(pPoseStack, x + 1, y + 1, outline);
            pixel(pPoseStack, x + width - 2, y + height - 2, outline);

            pixel(pPoseStack, x + width - 3, y + 2, background);
            pixel(pPoseStack, x + 2, y + height - 3, background);

            fill(pPoseStack, x + 2, y + 1, x + width - 3, y + 3, highLight);
            fill(pPoseStack, x + 1, y + 2, x + 3, y + height - 3, highLight);
            
            fill(pPoseStack, x + 3, y + height - 3, x + width - 2, y + height - 1, shadow);
            fill(pPoseStack, x + width - 3, y + 3, x + width - 1, y + height - 2, shadow);

            fill(pPoseStack, x + 3, y + 3, x + width - 3, y + height - 3, background);

            pixel(pPoseStack, x + 3, y + 3, highLight);
            pixel(pPoseStack, x + width - 4, y + width - 4, shadow);
        }
    }

    private void pixel(PoseStack pPoseStack, int x, int y, int color){
        fill(pPoseStack, x, y, x + 1, y + 1, color);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
        this.onTooltip.narrateTooltip((p_168841_) -> {
            pNarrationElementOutput.add(NarratedElementType.HINT, p_168841_);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnTooltip {
       void onTooltip(Panel panel, PoseStack pPoseStack, int pMouseX, int pMouseY);
 
       default void narrateTooltip(Consumer<Component> pContents) {
       }
    }
    
}
