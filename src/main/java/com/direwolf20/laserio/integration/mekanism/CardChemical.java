package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.containers.CardChemicalContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Config;

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

public class CardChemical extends BaseCard {
	public CardChemical() {
		super();
        CARDTYPE = CardType.CHEMICAL;
    }
	
	@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardChemicalContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeByte(-1);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }
	
	public static int setChemicalExtractAmt(ItemStack card, int chemicalextractamt) {
        if (chemicalextractamt == Config.BASE_MILLI_BUCKETS_CHEMICAL.get())
            card.removeTagKey("chemicalextractamt");
        else
            card.getOrCreateTag().putInt("chemicalextractamt", chemicalextractamt);
        return chemicalextractamt;
    }
	
	public static int getChemicalExtractAmt(ItemStack card) {
		CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("chemicalextractamt")) return Config.BASE_MILLI_BUCKETS_CHEMICAL.get();
        return compound.getInt("chemicalextractamt");    
	}
}