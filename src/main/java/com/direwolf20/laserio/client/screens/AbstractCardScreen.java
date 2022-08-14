package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.AbstractCardContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCardScreen<T extends AbstractCardContainer> extends AbstractContainerScreen<T>  {

    public final static String ReturnButton = "return";

    public final BaseCard.CardType CardType;

    protected final T baseContainer;
    protected final ItemStack card;

    protected Map<String, Button> buttons = new HashMap<>();
    public final List<Widget> backgroundRenderables = new ArrayList<>();

    public AbstractCardScreen(T container, Inventory pPlayerInventory, Component pTitle) {
        super(container, pPlayerInventory, pTitle);
        this.baseContainer = container;
        card = container.cardItem;
        Item cardItem = card.getItem();
        BaseCard baseCard = cardItem instanceof BaseCard ? (BaseCard) cardItem : null;
        CardType = baseCard != null ? baseCard.getCardType() : BaseCard.CardType.MISSING;
    }

    public abstract void openNode();

    public void addCommonWidgets() {
        if (baseContainer.direction == -1)
            return;
        buttons.put(ReturnButton, new Button(getGuiLeft() - 25, getGuiTop() + 1, 25, 20, Component.literal("<--"), (button) -> {
            openNode();
        }));
    }

    protected void renderTooltip(PoseStack pPoseStack, int mouseX, int mouseY) {
        super.renderTooltip(pPoseStack, mouseX, mouseY);
        Button returnButton = buttons.get(ReturnButton);
        if (MiscTools.inBounds(returnButton, mouseX, mouseY)) {
            this.renderTooltip(pPoseStack, Component.translatable(LaserNode.SCREEN_LASERNODE), mouseX, mouseY);
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);   
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        for(Widget widget : backgroundRenderables) {
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
    
}
