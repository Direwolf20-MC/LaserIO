package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.common.containers.CardHolderContainer;
import com.direwolf20.laserio.common.containers.customhandler.DataComponentHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import com.direwolf20.laserio.util.CardHolderItemStackHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardHolder extends Item {
    public CardHolder() {
        super(new Item.Properties()
                .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        if (player.isShiftKeyDown()) {
            setActive(itemstack, !getActive(itemstack));
            return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
        }

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardHolderContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
        }));


        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return getActive(itemStack);
    }

    public IItemHandler getItemHandler(ItemStack stack) {
        return new CardHolderItemStackHandler(CardHolderContainer.SLOTS, stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        //if (world.getDayTime() % 20 == 0) return;
        if (entity instanceof Player player && getActive(stack)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack cardStack = player.getInventory().getItem(i);
                if (cardStack.getItem() instanceof BaseCard || cardStack.getItem() instanceof BaseFilter || cardStack.getItem() instanceof OverclockerCard || cardStack.getItem() instanceof OverclockerNode)
                    addCardToInventory(stack, cardStack);
            }
        }
    }

    public static ItemStack addCardToInventory(ItemStack cardHolder, ItemStack card) {
        if (card.getItem() instanceof BaseFilter && !card.isComponentsPatchEmpty())
            return card;
        DataComponentHandler handler = new DataComponentHandler(cardHolder, CardHolderContainer.SLOTS);
        if (handler == null) return card;
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (stackInSlot.isEmpty()) emptySlots.add(i);
            if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stackInSlot, card)) {
                int j = stackInSlot.getCount() + card.getCount();
                int maxSize = 64;
                if (j <= maxSize) {
                    card.setCount(0);
                    stackInSlot.setCount(j);
                    handler.setStackInSlot(i, stackInSlot);
                } else if (stackInSlot.getCount() < maxSize) {
                    card.shrink(maxSize - stackInSlot.getCount());
                    stackInSlot.setCount(maxSize);
                    handler.setStackInSlot(i, stackInSlot);
                }
                if (card.isEmpty()) {
                    return card;
                }
            }
        }
        if (emptySlots.isEmpty()) return card;
        handler.insertItem(emptySlots.get(0), card.split(card.getCount()), false);
        return card;
    }

    public static UUID getUUID(ItemStack stack) {
        if (!stack.has(LaserIODataComponents.CARD_HOLDER_UUID)) {
            UUID newId = UUID.randomUUID();
            stack.set(LaserIODataComponents.CARD_HOLDER_UUID, newId);
            return newId;
        }
        return stack.get(LaserIODataComponents.CARD_HOLDER_UUID);
    }

    public static boolean getActive(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_HOLDER_ACTIVE, false);
    }

    public static boolean setActive(ItemStack stack, boolean active) {
        if (!active)
            stack.remove(LaserIODataComponents.CARD_HOLDER_ACTIVE);
        else
            stack.set(LaserIODataComponents.CARD_HOLDER_ACTIVE, active);
        return active;
    }
}
