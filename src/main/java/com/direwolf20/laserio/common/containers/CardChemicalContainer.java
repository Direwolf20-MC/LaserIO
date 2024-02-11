package com.direwolf20.laserio.common.containers;


import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import static com.direwolf20.laserio.common.blocks.LaserNode.findCardHolders;

public class CardChemicalContainer extends CardItemContainer {

    public CardChemicalContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, extraData.readItem());
        this.direction = extraData.readByte();
        cardHolder = findCardHolders(player);
    }

    public CardChemicalContainer(int windowId, Inventory playerInventory, Player player, ItemStack cardItem) {
        super(Registration.CardChemical_Container.get(), windowId);
        playerEntity = player;
        this.handler = BaseCard.getInventory(cardItem);
        this.playerInventory = new InvWrapper(playerInventory);
        this.cardItem = cardItem;
        if (handler != null) {
            addSlotRange(handler, 0, 80, 5, 1, 18);
            addSlotRange(handler, 1, 153, 5, 1, 18);
            addSlotBox(filterHandler, 0, 44, 25, 5, 18, 3, 18);
            toggleFilterSlots();
        }
        cardHolder = findCardHolders(player);
        if (!cardHolder.isEmpty()) {
            this.cardHolderHandler = cardHolder.getCapability(Capabilities.ItemHandler.ITEM, null);
            addSlotBox(cardHolderHandler, 0, -92, 32, 5, 18, 3, 18);
            cardHolderUUID = CardHolder.getUUID(cardHolder);
        }
        layoutPlayerInventorySlots(8, 84);
    }

    public CardChemicalContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack cardItem, byte direction) {
        this(windowId, playerInventory, player, cardItem);
        this.sourceContainer = sourcePos;
        this.direction = direction;
    }
}
