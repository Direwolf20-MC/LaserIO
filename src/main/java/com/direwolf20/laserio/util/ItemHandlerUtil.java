package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.filters.FilterCount;
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
import java.util.ArrayList;
import java.util.List;


public class ItemHandlerUtil {
    public record ExtractResult(ItemStack itemStack, int slot) {

    }

    @Nonnull
    public static ExtractResult extractItem(IItemHandler source, @Nonnull ItemStack incstack, boolean simulate, boolean isCompareNBT) {
        return extractItem(source, incstack, incstack.getCount(), simulate, isCompareNBT);
    }

    @Nonnull
    public static ExtractResult extractItemOnce(IItemHandler source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemStack tempStack = ItemStack.EMPTY;
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amount, stackInSlot.getCount());
                tempStack = source.extractItem(i, extractAmt, simulate);
                return new ExtractResult(tempStack, i); // If we found all we need, return the stack and the last slot we got it from
            }
        }
        return new ExtractResult(tempStack, -1); // If we didn't get all we need, return the stack we did get and no slot cache
    }

    @Nonnull
    public static ExtractResult extractItem(IItemHandler source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemStack tempStack = ItemStack.EMPTY;
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amount, stackInSlot.getCount());
                if (tempStack.isEmpty()) //If this is our first pass, make the temp stack == the extracted stack
                    tempStack = source.extractItem(i, extractAmt, simulate);
                else if (ItemHandlerHelper.canItemStacksStack(tempStack, stackInSlot)) //If this is our 2nd pass, the 2 itemstacks should stack, so do a grow()
                    tempStack.grow(source.extractItem(i, extractAmt, simulate).getCount());
                else //This in theory should never happen but who knows
                    return new ExtractResult(tempStack, i);
                if (tempStack.isEmpty()) continue; //This happens with some 'fake item' inventories like RFTools crafter
                amount -= extractAmt;
                if (amount == 0)
                    return new ExtractResult(tempStack, i); // If we found all we need, return the stack and the last slot we got it from
            }
        }
        return new ExtractResult(tempStack, -1); // If we didn't get all we need, return the stack we did get and no slot cache
    }

    /** Like ExtractItem but iterates Backwards **/
    @Nonnull
    public static ExtractResult extractItemBackwards(IItemHandler source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemStack tempStack = ItemStack.EMPTY;
        for (int i = source.getSlots() - 1; i >= 0; i--) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amount, stackInSlot.getCount());
                if (tempStack.isEmpty()) //If this is our first pass, make the temp stack == the extracted stack
                    tempStack = source.extractItem(i, extractAmt, simulate);
                else if (ItemHandlerHelper.canItemStacksStack(tempStack, stackInSlot)) //If this is our 2nd pass, the 2 itemstacks should stack, so do a grow()
                    tempStack.grow(source.extractItem(i, extractAmt, simulate).getCount());
                else //This in theory should never happen but who knows
                    return new ExtractResult(tempStack, i);
                if (tempStack.isEmpty()) continue; //This happens with some 'fake item' inventories like RFTools crafter
                amount -= extractAmt;
                if (amount == 0)
                    return new ExtractResult(tempStack, i); // If we found all we need, return the stack and the last slot we got it from
            }
        }
        return new ExtractResult(tempStack, -1); // If we didn't get all we need, return the stack we did get and no slot cache
    }


    @Nonnull
    public static TransferResult extractItemWithSlots(LaserNodeBE be, IItemHandler source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT, BaseCardCache cardCache) {
        TransferResult extractResults = new TransferResult();
        if (source == null || incstack.isEmpty()) {
            return extractResults;
        }
        int amtRemaining = amount;
        ItemStack remainingStack = incstack.copy();
        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                ItemStack extractStack = source.extractItem(i, extractAmt, simulate);
                if (extractStack.isEmpty())
                    continue; //This happens with some 'fake item' inventories like RFTools crafter
                amtRemaining -= extractAmt;
                extractResults.addResult(new TransferResult.Result(source, i, cardCache, extractStack, be, true));
                remainingStack.setCount(amtRemaining);
                if (amtRemaining == 0)
                    return extractResults; // If we found all we need, return the stack and the last slot we got it from
            }
        }
        //If we got here, it means we have some remaining itemStack we didn't extract
        extractResults.addRemainingStack(remainingStack);
        return extractResults; // If we didn't get all we need, return the stack we did get and no slot cache
    }

    @Nonnull
    public static TransferResult extractItemWithSlotsBackwards(LaserNodeBE be, IItemHandler source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT, ExtractorCardCache extractorCardCache) {
        TransferResult extractResults = new TransferResult();
        if (source == null || incstack.isEmpty()) {
            return extractResults;
        }
        int amtRemaining = amount;
        ItemStack remainingStack = incstack.copy();
        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        for (int i = source.getSlots() - 1; i >= 0; i--) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                ItemStack extractStack = source.extractItem(i, extractAmt, simulate);
                amtRemaining -= extractAmt;
                extractResults.addResult(new TransferResult.Result(source, i, extractorCardCache, extractStack, be, true));
                remainingStack.setCount(amtRemaining);
                if (amtRemaining == 0)
                    return extractResults; // If we found all we need, return the stack and the last slot we got it from
            }
        }
        //If we got here, it means we have some remaining itemStack we didn't extract
        extractResults.addRemainingStack(remainingStack);
        return extractResults; // If we didn't get all we need, return the stack we did get and no slot cache
    }

    @Nonnull
    public static TransferResult insertItemWithSlots(LaserNodeBE be, IItemHandler source, @Nonnull ItemStack incstack, int startAt, boolean simulate, boolean isCompareNBT, boolean stacksFirst, InserterCardCache inserterCardCache) {
        return insertItemWithSlots(be, source, incstack, incstack.getCount(), startAt, simulate, isCompareNBT, stacksFirst, inserterCardCache);
    }


    @Nonnull
    public static TransferResult insertItemWithSlots(LaserNodeBE be, IItemHandler source, @Nonnull ItemStack incstack, int amount, int startAt, boolean simulate, boolean isCompareNBT, boolean stacksFirst, InserterCardCache inserterCardCache) {
        TransferResult insertResults = new TransferResult();
        List<Integer> emptySlots = new ArrayList<>();
        if (source == null || incstack.isEmpty()) {
            return insertResults;
        }
        int amtRemaining = amount;
        ItemStack remainingStack = incstack.copy();
        remainingStack.setCount(amtRemaining);
        if (inserterCardCache.filterCard.getItem() instanceof FilterCount) { //If this is a count filter, only try to insert how many more items we need
            int filterCount = inserterCardCache.getFilterAmt(incstack);
            if (filterCount <= 0) return insertResults; //This should never happen in theory...
            ItemHandlerUtil.InventoryCounts inventoryCounts = new InventoryCounts(source, isCompareNBT);
            int amtInInv = inventoryCounts.getCount(remainingStack);
            int amtNeeded = filterCount - amtInInv;
            if (amtNeeded <= 0) return insertResults;
            amtRemaining = Math.min(remainingStack.getCount(), amtNeeded);
            remainingStack.setCount(amtRemaining);
        }

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        if (stacksFirst) { //Loop through the slots looking for like item stacks first
            for (int i = startAt; i < source.getSlots(); i++) {
                ItemStack stackInSlot = source.getStackInSlot(i);
                if (stackInSlot.isEmpty())
                    emptySlots.add(i); //If this slot is empty, add to the list of empty slots first
                if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) { //Look for like itemstacks to add to first.
                    remainingStack = source.insertItem(i, remainingStack, simulate); //Insert as many as we can
                    int amtInserted = amtRemaining - remainingStack.getCount();
                    if (amtInserted <= 0) continue;
                    insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(amtInserted), be, false));
                    amtRemaining = remainingStack.getCount(); //Update amtRemaining

                    if (amtRemaining == 0)
                        return insertResults;
                }
            }
            for (Integer i : emptySlots) { //Loop through the empty slots we found (above) or skip if empty
                remainingStack = source.insertItem(i, remainingStack, simulate); //Insert as many as we can
                if (remainingStack.getCount() == amtRemaining)
                    continue; //If we couldn't insert anything into this slot (as in the slot doesn't accept this item!)
                insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(amtRemaining - remainingStack.getCount()), be, false)); //Add the amount that fit to the list //Add the amount that fit to the list
                amtRemaining = remainingStack.getCount(); //Update amtRemaining

                if (amtRemaining == 0)
                    return insertResults;
            }
        } else {
            for (int i = 0; i < source.getSlots(); i++) { //Loop through all slots, who cares about matching item stacks anyway!
                remainingStack = source.insertItem(i, remainingStack, simulate); //Insert as many as we can
                insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(amtRemaining - remainingStack.getCount()), be, false)); //Add the amount that fit to the list //Add the amount that fit to the list
                amtRemaining = remainingStack.getCount(); //Update amtRemaining

                if (amtRemaining == 0)
                    return insertResults;
            }
        }
        //If we get here, we have an itemstack remaining
        insertResults.addRemainingStack(remainingStack);
        return insertResults; // If we didn't get all we need, return the stack we did get and no slot cache
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

    public static boolean doItemsMatch(ItemStack a, ItemStack b, boolean isCompareNBT) {
        return isCompareNBT ? ItemHandlerHelper.canItemStacksStack(a, b) : a.sameItem(b);
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
        private boolean isCompareNBT;

        public InventoryCounts() {

        }

        public InventoryCounts(IItemHandler handler, boolean compareNBT) {
            isCompareNBT = compareNBT;
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
                boolean sameItems = isCompareNBT ? ItemHandlerHelper.canItemStacksStack(cacheStack, stack) : cacheStack.sameItem(stack);
                if (sameItems) {
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
                boolean sameItems = isCompareNBT ? ItemHandlerHelper.canItemStacksStack(cacheStack, stack) : cacheStack.sameItem(stack);
                if (sameItems)
                    return cacheStack.getCount();
            }
            return 0;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }
}
