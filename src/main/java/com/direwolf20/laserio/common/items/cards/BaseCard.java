package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CustomItemHandlers.CardItemHandler;
import com.direwolf20.laserio.common.containers.ItemCardContainer;
import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class BaseCard extends Item {
    protected BaseCard.CardType CARDTYPE;

    public enum CardType {
        ITEM,
        FLUID,
        ENERGY
    }

    public enum TransferMode {
        INSERT,
        EXTRACT,
        STOCK;

        public static TransferMode next(TransferMode transferMode) {
            switch (transferMode) {
                case INSERT:
                    return (EXTRACT);
                case EXTRACT:
                    return (STOCK);
                case STOCK:
                    return (INSERT);
            }
            return INSERT;
        }
    }

    public BaseCard() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        //TODO Fix dupe bug with multiple cards in hand when right clicked.
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        CardItemHandler handler = getInventory(itemstack);
        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new ItemCardContainer(windowId, playerInventory, player, handler, itemstack), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static CardItemHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        CardItemHandler handler = new CardItemHandler(ItemCardContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new CardItemHandler(ItemCardContainer.SLOTS, stack)) : handler;
    }

    public static CardItemHandler setInventory(ItemStack stack, CardItemHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static TransferMode setTransferMode(ItemStack card, TransferMode mode) {
        card.getOrCreateTag().putByte("mode", (byte) mode.ordinal());
        return mode;
    }

    public static TransferMode getTransferMode(ItemStack card) {
        CompoundTag compound = card.getOrCreateTag();
        return !compound.contains("mode") ? setTransferMode(card, TransferMode.INSERT) : TransferMode.values()[compound.getByte("mode")];
    }

    public static TransferMode nextTransferMode(ItemStack card) {
        int k = getTransferMode(card).ordinal();
        TransferMode transferMode = TransferMode.values()[k == 2 ? 0 : k + 1];
        return setTransferMode(card, transferMode);
    }

    public static boolean tickable(ItemStack card) {
        return !getTransferMode(card).equals(TransferMode.INSERT);
    }
}
