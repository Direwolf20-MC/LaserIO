package com.direwolf20.laserio.util;

import com.google.common.collect.ArrayListMultimap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;


public class ItemHandlerUtil {
    @Nonnull
    public static ItemStack extractItem(IItemHandler source, @Nonnull ItemStack incstack, boolean simulate) {
        if (source == null || incstack.isEmpty())
            return incstack;

        int amtGotten = 0;
        int amtRemaining = incstack.getCount();
        ItemStack stack = incstack.copy();
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                ItemStack tempStack = source.extractItem(i, extractAmt, simulate);
                amtGotten += tempStack.getCount();
                amtRemaining -= tempStack.getCount();
                if (amtRemaining == 0) break;
            }
        }
        stack.setCount(amtGotten);
        return stack;
    }

    public static ItemStack extractIngredient(IItemHandler source, @Nonnull Ingredient ingredient, boolean simulate) {
        if (source == null || ingredient.checkInvalidation())
            return ItemStack.EMPTY;

        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (ingredient.test(stackInSlot)) { //If this ingredient matches
                ItemStack tempStack = source.extractItem(i, 1, simulate);
                return tempStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(inSlot, toInsert);
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    public static class InventoryInfo {

        private final NonNullList<ItemStack> inventory;
        private final IntList stackSizes = new IntArrayList();

        public InventoryInfo(IItemHandler handler) {
            inventory = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                inventory.set(i, stack);
                stackSizes.add(stack.getCount());
            }
        }
    }

    public static class InventoryCounts {
        private final ArrayListMultimap<Item, ItemStack> itemMap = ArrayListMultimap.create();
        private int totalCount = 0;

        public InventoryCounts() {

        }

        public InventoryCounts(IItemHandler handler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    setCount(stack);
                }
            }
        }

        public InventoryCounts(ListTag nbtList) {
            for (int i = 0; i < nbtList.size(); i++) {
                CompoundTag nbt = nbtList.getCompound(i);
                ItemStack stack = ItemStack.of(nbt.getCompound("itemStack"));
                stack.setCount(nbt.getInt("count"));
                setCount(stack);
            }
        }

        public ListTag serialize() {
            ListTag nbtList = new ListTag();
            int i = 0;
            for (ItemStack stack : itemMap.values()) {
                CompoundTag nbt = new CompoundTag();
                nbt.put("itemStack", stack.serializeNBT());
                nbt.putInt("count", stack.getCount());
                nbtList.add(i, nbt);
                i++;
            }
            return nbtList;
        }

        public void addHandler(IItemHandler handler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    setCount(stack);
                }
            }
        }

        public void addHandlerWithFilter(IItemHandler handler, BaseCardCache filterCard) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && filterCard.isStackValidForCard(stack)) {
                    setCount(stack);
                }
            }
        }

        public ArrayListMultimap<Item, ItemStack> getItemCounts() {
            return itemMap;
        }

        public void setCount(ItemStack stack) {
            if (stack.isEmpty()) return;
            for (ItemStack cacheStack : itemMap.get(stack.getItem())) {
                if (ItemHandlerHelper.canItemStacksStack(cacheStack, stack)) {
                    cacheStack.grow(stack.getCount());
                    totalCount += stack.getCount();
                    return;
                }
            }
            itemMap.put(stack.getItem(), stack.copy());
            totalCount += stack.getCount();
        }

        public ItemStack removeStack(ItemStack stack, int count) {
            ItemStack returnStack = ItemStack.EMPTY;
            for (ItemStack cacheStack : itemMap.get(stack.getItem())) {
                if (ItemHandlerHelper.canItemStacksStack(cacheStack, stack)) {
                    returnStack = cacheStack.split(count);
                    break;
                }
            }
            if (returnStack.equals(ItemStack.EMPTY)) return returnStack;

            itemMap.get(returnStack.getItem()).removeIf(o -> o.isEmpty());
            totalCount -= returnStack.getCount();
            return returnStack;
        }

        public int getCount(ItemStack stack) {
            for (ItemStack cacheStack : itemMap.get(stack.getItem())) {
                if (ItemHandlerHelper.canItemStacksStack(cacheStack, stack))
                    return cacheStack.getCount();
            }
            return 0;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }
}
