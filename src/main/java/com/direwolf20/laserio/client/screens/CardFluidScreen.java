package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CardFluidScreen extends CardItemScreen {

    public CardFluidScreen(CardItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
