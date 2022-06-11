package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class CardEnergy extends BaseCard {
    public CardEnergy() {
        super();
        CARDTYPE = CardType.ENERGY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardEnergyContainer(windowId, playerInventory, player, itemstack), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
        }));

        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static CardItemHandler getInventory(ItemStack stack) {
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
    }

    public static int setEnergyExtractAmt(ItemStack card, int energyextractamt) {
        if (energyextractamt == 10000)
            card.removeTagKey("energyextractamt");
        else
            card.getOrCreateTag().putInt("energyextractamt", energyextractamt);
        return energyextractamt;
    }

    public static int getEnergyExtractAmt(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("energyextractamt")) return 10000;
        return compound.getInt("energyextractamt");
    }
}
