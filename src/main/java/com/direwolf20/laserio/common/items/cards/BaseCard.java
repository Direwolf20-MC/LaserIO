package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
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
        STOCK
    }

    public BaseCard() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        //TODO Fix dupe bug with multiple cards in hand when right clicked.
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        CardItemHandler handler = getInventory(itemstack);
        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardItemContainer(windowId, playerInventory, player, handler, itemstack), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    @Override
    /**
     * ItemStack sensitive version of getContainerItem. Returns a full ItemStack
     * instance of the result.
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    public ItemStack getContainerItem(ItemStack itemStack) {
        if (!hasContainerItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        return getInventory(itemStack).getStackInSlot(0);
    }

    /**
     * ItemStack sensitive version of hasContainerItem
     *
     * @param stack The current item stack
     * @return True if this item has a 'container'
     */
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return !getInventory(stack).getStackInSlot(0).equals(ItemStack.EMPTY);
    }

    public static CardItemHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        CardItemHandler handler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new CardItemHandler(CardItemContainer.SLOTS, stack)) : handler;
    }

    public static CardItemHandler setInventory(ItemStack stack, CardItemHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static byte setTransferMode(ItemStack card, byte mode) {
        card.getOrCreateTag().putByte("mode", mode);
        return mode;
    }

    public static byte getTransferMode(ItemStack card) {
        CompoundTag compound = card.getOrCreateTag();
        return !compound.contains("mode") ? setTransferMode(card, (byte) 0) : compound.getByte("mode");
    }

    public static byte nextTransferMode(ItemStack card) {
        byte mode = getTransferMode(card);
        return setTransferMode(card, (byte) (mode == 2 ? 0 : mode + 1));
    }

    public static TransferMode getNamedTransferMode(ItemStack card) {
        return TransferMode.values()[getTransferMode(card)];
    }

    public static byte setChannel(ItemStack card, byte channel) {
        card.getOrCreateTag().putByte("channel", channel);
        return channel;
    }

    public static byte getChannel(ItemStack card) {
        CompoundTag compound = card.getOrCreateTag();
        return !compound.contains("channel") ? setChannel(card, (byte) 0) : compound.getByte("channel");
    }

    public static byte nextChannel(ItemStack card) {
        byte k = getChannel(card);
        return setChannel(card, (byte) (k == 15 ? 0 : k + 1));
    }

    public static byte setItemExtractAmt(ItemStack card, byte itemextractamt) {
        card.getOrCreateTag().putByte("itemextractamt", itemextractamt);
        return itemextractamt;
    }

    public static byte getItemExtractAmt(ItemStack card) {
        CompoundTag compound = card.getOrCreateTag();
        return !compound.contains("itemextractamt") ? setItemExtractAmt(card, (byte) 1) : compound.getByte("itemextractamt");
    }

    public static ItemStack getFilter(ItemStack card) {
        CardItemHandler cardItemHandler = getInventory(card);
        return cardItemHandler.getStackInSlot(0);
    }

    public static boolean tickable(ItemStack card) {
        return !getNamedTransferMode(card).equals(TransferMode.INSERT);
    }
}
