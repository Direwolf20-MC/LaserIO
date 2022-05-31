package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.common.containers.CardHolderContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardHolderHandler;
import com.direwolf20.laserio.setup.ModSetup;
import com.direwolf20.laserio.util.ItemStackHandlerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardHolder extends Item {
    public CardHolder() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP)
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

        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new CardHolderContainer(windowId, playerInventory, player, itemstack), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
        }));

        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemStackHandlerProvider(9);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        if (world.getDayTime() % 20 != 0) return;
        if (entity instanceof Player player && getActive(stack)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack cardStack = player.getInventory().getItem(i);
                addCardToInventory(stack, cardStack);
            }
        }
    }

    public static ItemStack addCardToInventory(ItemStack cardHolder, ItemStack card) {
        CardHolderHandler handler = getInventory(cardHolder);
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(0);
            if (stackInSlot.isEmpty()) emptySlots.add(i);
            if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameTags(stackInSlot, card)) {
                int j = stackInSlot.getCount() + card.getCount();
                int maxSize = 64;
                if (j <= maxSize) {
                    card.setCount(0);
                    stackInSlot.setCount(j);
                } else if (stackInSlot.getCount() < maxSize) {
                    card.shrink(maxSize - stackInSlot.getCount());
                    stackInSlot.setCount(maxSize);
                }
                if (card.isEmpty()) {
                    setInventory(cardHolder, handler);
                    return card;
                }
            }
        }
        if (emptySlots.isEmpty()) return card;
        handler.setStackInSlot(emptySlots.get(0), card.split(card.getCount()));
        return card;
    }

    public static CardHolderHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        CardHolderHandler handler = new CardHolderHandler(CardHolderContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        if (handler.getSlots() < CardHolderContainer.SLOTS)
            handler.reSize(CardHolderContainer.SLOTS);
        return !compound.contains("inv") ? setInventory(stack, new CardHolderHandler(CardHolderContainer.SLOTS, stack)) : handler;
    }

    public static CardHolderHandler setInventory(ItemStack stack, CardHolderHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static UUID getUUID(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.hasUUID("UUID")) {
            UUID newId = UUID.randomUUID();
            nbt.putUUID("UUID", newId);
            return newId;
        }
        return nbt.getUUID("UUID");
    }

    public static boolean getActive(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("active")) return false;
        return compound.getBoolean("active");
    }

    public static boolean setActive(ItemStack stack, boolean active) {
        if (!active)
            stack.removeTagKey("active");
        else
            stack.getOrCreateTag().putBoolean("active", active);
        return active;
    }
}
