package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.AbstractCardContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public abstract class AbstractCardScreen<T extends AbstractCardContainer> extends AbstractContainerScreen<T>  {

    public final BaseCard.CardType CardType;

    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/redstonecard.png");

    private final int HeaderOffset = 20;
    private final int ColorInset = 5;

    protected final T baseContainer;
    protected final ItemStack card;

    public AbstractCardScreen(T container, Inventory pPlayerInventory, Component pTitle) {
        super(container, pPlayerInventory, pTitle);
        this.baseContainer = container;
        card = container.cardItem;
        Item cardItem = card.getItem();
        BaseCard baseCard = cardItem instanceof BaseCard ? (BaseCard) cardItem : null;
        CardType = baseCard != null ? baseCard.getCardType() : BaseCard.CardType.MISSING;
    }

    public abstract Component cardTypeName();

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Color color = RenderUtils.getColor(CardType);
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY - HeaderOffset, 0, 0, this.imageWidth, this.imageHeight);
        fill(matrixStack, relX + ColorInset, relY - HeaderOffset + ColorInset,
            relX + this.imageWidth - ColorInset, relY, color.getRGB());

        Font font = Minecraft.getInstance().font;
        matrixStack.pushPose();
        float scale = 1f;
        matrixStack.scale(scale, scale, scale);
        FormattedCharSequence text = this.cardTypeName().getVisualOrderText();
        float x = (relX + this.imageWidth / 2f - font.width(text) / 2f) / scale;
        float y = (relY - HeaderOffset + ColorInset + 4) / scale;
        font.drawShadow(matrixStack, text, x, y, Color.WHITE.getRGB());
        matrixStack.popPose();
    }
    
}
