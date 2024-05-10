package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CardItem extends BaseCard {
    public CardItem() {
        super();
        CARDTYPE = CardType.ITEM;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardItemContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            buf.writeByte(-1);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static byte setItemExtractAmt(ItemStack card, byte itemextractamt) {
        if (itemextractamt == 1)
            card.remove(LaserIODataComponents.ITEM_CARD_EXTRACT_AMT);
        else
            card.set(LaserIODataComponents.ITEM_CARD_EXTRACT_AMT, itemextractamt);
        return itemextractamt;
    }

    public static byte getItemExtractAmt(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.ITEM_CARD_EXTRACT_AMT, 1).byteValue();
    }
}
