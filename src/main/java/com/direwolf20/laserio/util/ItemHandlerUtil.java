package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.google.common.collect.ArrayListMultimap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ItemHandlerUtil {
    public record ExtractResult(ItemStack itemStack, int slot) {

    }

    private static ItemStack stackAt(ResourceHandler<ItemResource> handler, int slot) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        return resource.toStack(handler.getAmountAsInt(slot));
    }

    @Nonnull
    public static ExtractResult extractItem(ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, boolean simulate, boolean isCompareNBT) {
        return extractItem(source, incstack, incstack.getCount(), simulate, isCompareNBT);
    }

    @Nonnull
    public static ExtractResult extractItemOnce(ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        for (int i = 0; i < source.size(); i++) {
            ItemStack stackInSlot = stackAt(source, i);
            if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                int extractAmt = Math.min(amount, stackInSlot.getCount());
                try (Transaction tx = Transaction.openRoot()) {
                    int extracted = source.extract(i, ItemResource.of(stackInSlot), extractAmt, tx);
                    if (!simulate) tx.commit();
                    ItemStack tempStack = ItemResource.of(stackInSlot).toStack(extracted);
                    return new ExtractResult(tempStack, i);
                }
            }
        }
        return new ExtractResult(ItemStack.EMPTY, -1); // If we didn't get all we need, return the stack we did get and no slot cache
    }

    @Nonnull
    public static ExtractResult extractItem(ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemStack tempStack = ItemStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            for (int i = 0; i < source.size(); i++) {
                ItemStack stackInSlot = stackAt(source, i);
                if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                    int extractAmt = Math.min(amount, stackInSlot.getCount());
                    int extracted = source.extract(i, ItemResource.of(stackInSlot), extractAmt, tx);
                    if (extracted == 0) continue; //This happens with some 'fake item' inventories like RFTools crafter
                    if (tempStack.isEmpty()) //If this is our first pass, make the temp stack == the extracted stack
                        tempStack = ItemResource.of(stackInSlot).toStack(extracted);
                    else if (ItemStack.isSameItemSameComponents(tempStack, stackInSlot)) //If this is our 2nd pass, the 2 itemstacks should stack, so do a grow()
                        tempStack.grow(extracted);
                    else { //This in theory should never happen but who knows
                        if (!simulate) tx.commit();
                        return new ExtractResult(tempStack, i);
                    }
                    amount -= extracted;
                    if (amount == 0) {
                        if (!simulate) tx.commit();
                        return new ExtractResult(tempStack, i); // If we found all we need, return the stack and the last slot we got it from
                    }
                }
            }
            if (!simulate) tx.commit();
        }
        return new ExtractResult(tempStack, -1); // If we didn't get all we need, return the stack we did get and no slot cache
    }

    /** Like ExtractItem but iterates Backwards **/
    @Nonnull
    public static ExtractResult extractItemBackwards(ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT) {
        if (source == null || incstack.isEmpty())
            return new ExtractResult(incstack, -1);

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemStack tempStack = ItemStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            for (int i = source.size() - 1; i >= 0; i--) {
                ItemStack stackInSlot = stackAt(source, i);
                if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                    int extractAmt = Math.min(amount, stackInSlot.getCount());
                    int extracted = source.extract(i, ItemResource.of(stackInSlot), extractAmt, tx);
                    if (extracted == 0) continue;
                    if (tempStack.isEmpty())
                        tempStack = ItemResource.of(stackInSlot).toStack(extracted);
                    else if (ItemStack.isSameItemSameComponents(tempStack, stackInSlot))
                        tempStack.grow(extracted);
                    else {
                        if (!simulate) tx.commit();
                        return new ExtractResult(tempStack, i);
                    }
                    amount -= extracted;
                    if (amount == 0) {
                        if (!simulate) tx.commit();
                        return new ExtractResult(tempStack, i);
                    }
                }
            }
            if (!simulate) tx.commit();
        }
        return new ExtractResult(tempStack, -1);
    }


    @Nonnull
    public static TransferResult extractItemWithSlots(LaserNodeBE be, ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT, BaseCardCache cardCache) {
        TransferResult extractResults = new TransferResult();
        if (source == null || incstack.isEmpty()) {
            return extractResults;
        }
        int amtRemaining = amount;
        ItemStack remainingStack = incstack.copy();
        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        try (Transaction tx = Transaction.openRoot()) {
            for (int i = 0; i < source.size(); i++) {
                ItemStack stackInSlot = stackAt(source, i);
                if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                    int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                    int extracted = source.extract(i, ItemResource.of(stackInSlot), extractAmt, tx);
                    if (extracted == 0)
                        continue; //This happens with some 'fake item' inventories like RFTools crafter
                    ItemStack extractStack = ItemResource.of(stackInSlot).toStack(extracted);
                    amtRemaining -= extracted;
                    extractResults.addResult(new TransferResult.Result(source, i, cardCache, extractStack, be, true));
                    remainingStack.setCount(amtRemaining);
                    if (amtRemaining == 0) {
                        if (!simulate) tx.commit();
                        return extractResults;
                    }
                }
            }
            if (!simulate) tx.commit();
        }
        //If we got here, it means we have some remaining itemStack we didn't extract
        extractResults.addRemainingStack(remainingStack);
        return extractResults;
    }

    @Nonnull
    public static TransferResult extractItemWithSlotsBackwards(LaserNodeBE be, ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, boolean simulate, boolean isCompareNBT, ExtractorCardCache extractorCardCache) {
        TransferResult extractResults = new TransferResult();
        if (source == null || incstack.isEmpty()) {
            return extractResults;
        }
        int amtRemaining = amount;
        ItemStack remainingStack = incstack.copy();
        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        try (Transaction tx = Transaction.openRoot()) {
            for (int i = source.size() - 1; i >= 0; i--) {
                ItemStack stackInSlot = stackAt(source, i);
                if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) {
                    int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                    int extracted = source.extract(i, ItemResource.of(stackInSlot), extractAmt, tx);
                    ItemStack extractStack = ItemResource.of(stackInSlot).toStack(extracted);
                    amtRemaining -= extracted;
                    extractResults.addResult(new TransferResult.Result(source, i, extractorCardCache, extractStack, be, true));
                    remainingStack.setCount(amtRemaining);
                    if (amtRemaining == 0) {
                        if (!simulate) tx.commit();
                        return extractResults;
                    }
                }
            }
            if (!simulate) tx.commit();
        }
        //If we got here, it means we have some remaining itemStack we didn't extract
        extractResults.addRemainingStack(remainingStack);
        return extractResults;
    }

    @Nonnull
    public static TransferResult insertItemWithSlots(LaserNodeBE be, ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int startAt, boolean simulate, boolean isCompareNBT, boolean stacksFirst, InserterCardCache inserterCardCache) {
        return insertItemWithSlots(be, source, incstack, incstack.getCount(), startAt, simulate, isCompareNBT, stacksFirst, inserterCardCache);
    }


    @Nonnull
    public static TransferResult insertItemWithSlots(LaserNodeBE be, ResourceHandler<ItemResource> source, @Nonnull ItemStack incstack, int amount, int startAt, boolean simulate, boolean isCompareNBT, boolean stacksFirst, InserterCardCache inserterCardCache) {
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
            ItemHandlerUtil.InventoryCounts inventoryCounts = new InventoryCounts(source, inserterCardCache.isCompareNBT);
            int amtInInv = inventoryCounts.getCount(remainingStack);
            int amtNeeded = filterCount - amtInInv;
            if (amtNeeded <= 0) return insertResults;
            amtRemaining = Math.min(remainingStack.getCount(), amtNeeded);
            remainingStack.setCount(amtRemaining);
        }

        ItemStackKey key = new ItemStackKey(incstack, isCompareNBT);
        ItemResource incResource = ItemResource.of(incstack);
        try (Transaction tx = Transaction.openRoot()) {
            if (stacksFirst) { //Loop through the slots looking for like item stacks first
                for (int i = startAt; i < source.size(); i++) {
                    ItemStack stackInSlot = stackAt(source, i);
                    if (stackInSlot.isEmpty())
                        emptySlots.add(i); //If this slot is empty, add to the list of empty slots first
                    if (key.equals(new ItemStackKey(stackInSlot, isCompareNBT))) { //Look for like itemstacks to add to first.
                        int inserted = source.insert(i, incResource, amtRemaining, tx);
                        if (inserted <= 0) continue;
                        insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(inserted), be, false));
                        amtRemaining -= inserted;
                        remainingStack.setCount(amtRemaining);

                        if (amtRemaining == 0) {
                            if (!simulate) tx.commit();
                            return insertResults;
                        }
                    }
                }
                for (Integer i : emptySlots) { //Loop through the empty slots we found (above) or skip if empty
                    int inserted = source.insert(i, incResource, amtRemaining, tx);
                    if (inserted == 0)
                        continue; //If we couldn't insert anything into this slot (as in the slot doesn't accept this item!)
                    insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(inserted), be, false));
                    amtRemaining -= inserted;
                    remainingStack.setCount(amtRemaining);

                    if (amtRemaining == 0) {
                        if (!simulate) tx.commit();
                        return insertResults;
                    }
                }
            } else {
                for (int i = 0; i < source.size(); i++) { //Loop through all slots, who cares about matching item stacks anyway!
                    int inserted = source.insert(i, incResource, amtRemaining, tx);
                    if (inserted == 0) continue;
                    insertResults.addResult(new TransferResult.Result(source, i, inserterCardCache, incstack.split(inserted), be, false));
                    amtRemaining -= inserted;
                    remainingStack.setCount(amtRemaining);

                    if (amtRemaining == 0) {
                        if (!simulate) tx.commit();
                        return insertResults;
                    }
                }
            }
            if (!simulate) tx.commit();
        }
        //If we get here, we have an itemstack remaining
        insertResults.addRemainingStack(remainingStack);
        return insertResults;
    }

    public static boolean doItemsMatch(ItemStack a, ItemStack b, boolean isCompareNBT) {
        return isCompareNBT ? ItemStack.isSameItemSameComponents(a, b) : ItemStack.isSameItem(a, b);
    }

    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(inSlot, toInsert);
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(size);
    }

    public static class InventoryInfo {

        private final NonNullList<ItemStack> inventory;
        private final IntList stackSizes = new IntArrayList();

        public InventoryInfo(ResourceHandler<ItemResource> handler) {
            inventory = NonNullList.withSize(handler.size(), ItemStack.EMPTY);
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = stackAt(handler, i);
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

        public InventoryCounts(ResourceHandler<ItemResource> handler, boolean compareNBT) {
            isCompareNBT = compareNBT;
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = stackAt(handler, i);
                if (!stack.isEmpty()) {
                    setCount(stack);
                }
            }
        }

        public void addHandler(ResourceHandler<ItemResource> handler) {
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = stackAt(handler, i);
                if (!stack.isEmpty()) {
                    setCount(stack);
                }
            }
        }

        public void addHandlerWithFilter(ResourceHandler<ItemResource> handler, BaseCardCache filterCard) {
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = stackAt(handler, i);
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
                boolean sameItems = isCompareNBT ? ItemStack.isSameItemSameComponents(cacheStack, stack) : ItemStack.isSameItem(cacheStack, stack);
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
                if (ItemStack.isSameItemSameComponents(cacheStack, stack)) {
                    returnStack = cacheStack.split(count);
                    break;
                }
            }
            if (returnStack.isEmpty()) return returnStack;

            itemMap.get(returnStack.getItem()).removeIf(o -> o.isEmpty());
            totalCount -= returnStack.getCount();
            return returnStack;
        }

        public int getCount(ItemStack stack) {
            for (ItemStack cacheStack : itemMap.get(stack.getItem())) {
                boolean sameItems = isCompareNBT ? ItemStack.isSameItemSameComponents(cacheStack, stack) : ItemStack.isSameItem(cacheStack, stack);
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
