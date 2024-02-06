package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CardEnergy extends BaseCard {
    public static final int max_energy_transfer = 1000000;
    public CardEnergy() {
        super();
        CARDTYPE = CardType.ENERGY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardEnergyContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeByte(-1);
        }));

        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    /*public static CardItemHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("inv")) return new CardItemHandler(CardEnergyContainer.SLOTS, stack);
        CardItemHandler handler = new CardItemHandler(CardEnergyContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        if (handler.getSlots() < CardEnergyContainer.SLOTS)
            handler.reSize(CardEnergyContainer.SLOTS);
        return handler;
    }

    public static CardItemHandler setInventory(ItemStack stack, CardItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                stack.getOrCreateTag().put("inv", handler.serializeNBT());
                return handler;
            }
        }
        stack.removeTagKey("inv");
        return handler;
    }*/

    public static int setEnergyExtractAmt(ItemStack card, int energyextractamt) {
        if (energyextractamt == max_energy_transfer)
            card.removeTagKey("energyextractamt");
        else
            card.getOrCreateTag().putInt("energyextractamt", energyextractamt);
        return energyextractamt;
    }

    public static int getEnergyExtractAmt(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("energyextractamt")) return max_energy_transfer;
        return compound.getInt("energyextractamt");
    }

    public static int setExtractSpeed(ItemStack card, int itemextractspeed) {
        if (itemextractspeed == 1)
            card.removeTagKey("itemextractspeed");
        else
            card.getOrCreateTag().putInt("itemextractspeed", itemextractspeed);
        return itemextractspeed;
    }

    public static int getExtractSpeed(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("itemextractspeed")) return 1;
        return compound.getInt("itemextractspeed");
    }

    public static int setInsertLimitPercent(ItemStack card, int limitpercent) {
        if (limitpercent == 100)
            card.removeTagKey("limitinsertpercent");
        else
            card.getOrCreateTag().putInt("limitinsertpercent", limitpercent);
        return limitpercent;
    }

    public static int getInsertLimitPercent(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("limitinsertpercent")) return 100;
        return compound.getInt("limitinsertpercent");
    }

    public static int setExtractLimitPercent(ItemStack card, int limitextractpercent) {
        if (limitextractpercent == 0)
            card.removeTagKey("limitextractpercent");
        else
            card.getOrCreateTag().putInt("limitextractpercent", limitextractpercent);
        return limitextractpercent;
    }

    public static int getExtractLimitPercent(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("limitextractpercent")) return 0;
        return compound.getInt("limitextractpercent");
    }
}
