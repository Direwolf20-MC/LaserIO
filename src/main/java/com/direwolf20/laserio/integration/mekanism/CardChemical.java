package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.containers.CardChemicalContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Config;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CardChemical extends BaseCard {
    public CardChemical() {
        CARDTYPE = CardType.CHEMICAL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardChemicalContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            buf.writeByte(-1);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static int setChemicalExtractAmt(ItemStack card, int chemicalextractamt) {
        if (chemicalextractamt == Config.BASE_MILLI_BUCKETS_CHEMICAL.get())
            card.remove(LaserIODataComponents.CHEMICAL_CARD_EXTRACT_AMT);
        else
            card.set(LaserIODataComponents.CHEMICAL_CARD_EXTRACT_AMT, chemicalextractamt);
        return chemicalextractamt;
    }

    public static int getChemicalExtractAmt(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CHEMICAL_CARD_EXTRACT_AMT, Config.BASE_MILLI_BUCKETS_CHEMICAL.get());
    }
}
