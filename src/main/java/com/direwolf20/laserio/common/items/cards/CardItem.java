package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardItemContainer;
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
import net.minecraftforge.network.NetworkHooks;

public class CardItem extends BaseCard {
    public CardItem() {
        super();
        CARDTYPE = CardType.ITEM;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardItemContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeByte(-1);
        }));

        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static byte setItemExtractAmt(ItemStack card, byte itemextractamt) {
        if (itemextractamt == 1)
            card.removeTagKey("itemextractamt");
        else
            card.getOrCreateTag().putByte("itemextractamt", itemextractamt);
        return itemextractamt;
    }

    public static byte getItemExtractAmt(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("itemextractamt")) return (byte) 1;
        return compound.getByte("itemextractamt");
    }
}
