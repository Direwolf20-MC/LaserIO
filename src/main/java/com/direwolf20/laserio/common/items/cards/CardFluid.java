package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.setup.Config;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CardFluid extends BaseCard {
    public CardFluid() {
        super();
        CARDTYPE = CardType.FLUID;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardFluidContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            buf.writeByte(-1);
        }));

        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static int setFluidExtractAmt(ItemStack card, int fluidextractamt) {
        if (fluidextractamt == Config.BASE_MILLI_BUCKETS_FLUID.get())
            card.remove(LaserIODataComponents.FLUID_CARD_EXTRACT_AMT);
        else
            card.set(LaserIODataComponents.FLUID_CARD_EXTRACT_AMT, fluidextractamt);
        return fluidextractamt;
    }

    public static int getFluidExtractAmt(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.FLUID_CARD_EXTRACT_AMT, Config.BASE_MILLI_BUCKETS_FLUID.get());
    }
}
