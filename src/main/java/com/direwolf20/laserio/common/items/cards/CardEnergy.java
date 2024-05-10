package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
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

public class CardEnergy extends BaseCard {
    public CardEnergy() {
        super();
        CARDTYPE = CardType.ENERGY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardEnergyContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            buf.writeByte(-1);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static int setEnergyExtractAmt(ItemStack card, int energyextractamt) {
        if (energyextractamt == Config.MAX_FE_TICK.get())
            card.remove(LaserIODataComponents.ENERGY_CARD_EXTRACT_AMT);
        else
            card.set(LaserIODataComponents.ENERGY_CARD_EXTRACT_AMT, energyextractamt);
        return energyextractamt;
    }

    public static int getEnergyExtractAmt(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.ENERGY_CARD_EXTRACT_AMT, Config.MAX_FE_TICK.get());
    }

    public static int setExtractSpeed(ItemStack card, int energyextractspeed) {
        if (energyextractspeed == 1)
            card.remove(LaserIODataComponents.ENERGY_CARD_EXTRACT_SPEED);
        else
            card.set(LaserIODataComponents.ENERGY_CARD_EXTRACT_SPEED, energyextractspeed);
        return energyextractspeed;
    }

    public static int getExtractSpeed(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.ENERGY_CARD_EXTRACT_SPEED, 1);
    }

    public static int setInsertLimitPercent(ItemStack card, int limitpercent) {
        if (limitpercent == 100)
            card.remove(LaserIODataComponents.ENERGY_CARD_INSERT_LIMIT);
        else
            card.set(LaserIODataComponents.ENERGY_CARD_INSERT_LIMIT, limitpercent);
        return limitpercent;
    }

    public static int getInsertLimitPercent(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.ENERGY_CARD_INSERT_LIMIT, 100);
    }

    public static int setExtractLimitPercent(ItemStack card, int limitextractpercent) {
        if (limitextractpercent == 0)
            card.remove(LaserIODataComponents.ENERGY_CARD_EXTRACT_LIMIT);
        else
            card.set(LaserIODataComponents.ENERGY_CARD_EXTRACT_LIMIT, limitextractpercent);
        return limitextractpercent;
    }

    public static int getExtractLimitPercent(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.ENERGY_CARD_EXTRACT_LIMIT, 0);
    }
}
