package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BaseCardCache {
    public final Direction direction;
    public final byte channel;
    public final ItemStack filterCard;
    public final int cardSlot;
    public final Set<ItemStack> filteredItems;

    public final boolean isAllowList;
    public final boolean isCompareNBT;
    public final Map<ItemStackKey, Boolean> filterCache = new Object2BooleanOpenHashMap<>();
    public final Map<ItemStackKey, Integer> filterCounts = new Object2IntOpenHashMap<>();

    public BaseCardCache(Direction direction, byte channel, ItemStack filterCard, int cardSlot) {
        this.direction = direction;
        this.channel = channel;
        this.filterCard = filterCard;
        this.cardSlot = cardSlot;
        if (filterCard.equals(ItemStack.EMPTY)) {
            filteredItems = new HashSet<>();
            isAllowList = false;
            isCompareNBT = false;
        } else {
            this.filteredItems = getFilteredItems();
            isAllowList = BaseFilter.getAllowList(filterCard);
            isCompareNBT = BaseFilter.getCompareNBT(filterCard);
        }
    }

    public int getFilterAmt(ItemStack testStack) {
        if (filterCard.equals(ItemStack.EMPTY))
            return 0; //If theres no filter in the card (This should never happen in theory)
        if (filterCard.getItem() instanceof FilterBasic) { //If this is a basicCard return -1 which will mean infinite amount
            return -1;
        }
        ItemStackKey key = new ItemStackKey(testStack, isCompareNBT);
        if (filterCounts.containsKey(key)) //If we've already tested this, get it from the cache
            return filterCounts.get(key);
        for (ItemStack stack : filteredItems) { //If the item is not in the cache, loop through filtered items list
            if (key.equals(new ItemStackKey(stack, isCompareNBT))) {
                filterCounts.put(key, stack.getCount());
                return stack.getCount();
            }
        }
        filterCounts.put(key, 0);
        return 0; //Should never get here in theory
    }

    public Set<ItemStack> getFilteredItems() {
        Set<ItemStack> filteredItems = new HashSet<>();
        ItemStackHandler filterSlotHandler;
        if (filterCard.getItem() instanceof FilterBasic)
            filterSlotHandler = FilterBasic.getInventory(filterCard);
        else
            filterSlotHandler = FilterCount.getInventory(filterCard);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty())
                filteredItems.add(itemStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
        }
        return filteredItems;
    }

    public boolean isStackValidForCard(ItemStack testStack) {
        if (filterCard.equals(ItemStack.EMPTY)) return true; //If theres no filter in the card
        ItemStackKey key = new ItemStackKey(testStack, isCompareNBT);
        if (filterCache.containsKey(key)) return filterCache.get(key);
        for (ItemStack stack : filteredItems) {
            if (key.equals(new ItemStackKey(stack, isCompareNBT))) {
                filterCache.put(key, isAllowList);
                return isAllowList;
            }
        }
        filterCache.put(key, !isAllowList);
        return !isAllowList;
    }
}
