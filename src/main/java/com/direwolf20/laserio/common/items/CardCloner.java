package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CardCloner extends Item {

    public CardCloner() {
        super(new Properties()
                .stacksTo(1));
    }

    public static void setItemType(ItemStack stack, String itemType) {
        stack.getOrCreateTag().putString("itemType", itemType);
    }

    public static String getItemType(ItemStack stack) {
        return stack.getOrCreateTag().getString("itemType");
    }

    public static void saveSettings(ItemStack stack, CompoundTag tag) {
        stack.getOrCreateTag().put("settings", tag);
    }

    public static CompoundTag getSettings(ItemStack stack) {
        return stack.getOrCreateTag().getCompound("settings");
    }

    public static String getFilterType(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack filterStack = itemStackHandler.getStackInSlot(0);
        if (filterStack.isEmpty()) return "";

        return filterStack.getItem().toString();
    }

    public static ItemStack getFilter(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack filterStack = itemStackHandler.getStackInSlot(0);
        return filterStack;
    }

    public static int getOverclockCount(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack overclockStack = itemStackHandler.getStackInSlot(1);
        if (overclockStack.isEmpty()) return 0;

        return overclockStack.getCount();
    }

    public static ItemStack getOverclocker(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack overclockStack = itemStackHandler.getStackInSlot(1);
        return overclockStack;
    }
}
