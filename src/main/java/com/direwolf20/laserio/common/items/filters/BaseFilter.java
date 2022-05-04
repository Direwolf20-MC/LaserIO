package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BaseFilter extends Item {
    public BaseFilter() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    public static boolean getAllowList(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("allowList") ? setAllowList(stack, true) : compound.getBoolean("allowList");
    }

    public static boolean setAllowList(ItemStack stack, boolean allowList) {
        stack.getOrCreateTag().putBoolean("allowList", allowList);
        return allowList;
    }

    public static boolean getCompareNBT(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("compareNBT") ? setCompareNBT(stack, false) : compound.getBoolean("compareNBT");
    }

    public static boolean setCompareNBT(ItemStack stack, boolean compareNBT) {
        stack.getOrCreateTag().putBoolean("compareNBT", compareNBT);
        return compareNBT;
    }
}
