package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.*;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BaseCardCache {
    public final Direction direction;
    public final ItemStack cardItem;
    public final byte channel;
    public final ItemStack filterCard;
    public final int cardSlot;
    public final List<ItemStack> filteredItems;
    public final List<String> filterTags;
    public final byte sneaky;
    public final LaserNodeBE be;

    public final boolean isAllowList;
    public final boolean isCompareNBT;
    public final Map<ItemStackKey, Boolean> filterCache = new Object2BooleanOpenHashMap<>();
    public final Map<ItemStackKey, Integer> filterCounts = new Object2IntOpenHashMap<>();

    public BaseCardCache(Direction direction, ItemStack cardItem, int cardSlot, LaserNodeBE be) {
        this.cardItem = cardItem;
        this.direction = direction;
        this.sneaky = BaseCard.getSneaky(cardItem);
        this.channel = BaseCard.getChannel(cardItem);
        this.filterCard = BaseCard.getFilter(cardItem);
        this.cardSlot = cardSlot;
        this.be = be;
        if (filterCard.equals(ItemStack.EMPTY)) {
            filteredItems = new ArrayList<>();
            filterTags = new ArrayList<>();
            isAllowList = false;
            isCompareNBT = false;
        } else {
            this.filteredItems = getFilteredItems();
            this.filterTags = getFilterTags();
            isAllowList = BaseFilter.getAllowList(filterCard);
            isCompareNBT = BaseFilter.getCompareNBT(filterCard);
        }
    }

    public int getFilterAmt(ItemStack testStack) {
        if (filterCard.equals(ItemStack.EMPTY))
            return 0; //If theres no filter in the card (This should never happen in theory)
        if (!(filterCard.getItem() instanceof FilterCount)) { //If this is a basic or tag Card return -1 which will mean infinite amount
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

    public List<ItemStack> getFilteredItems() {
        List<ItemStack> filteredItems = new ArrayList<>();
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

    public List<String> getFilterTags() {
        List<String> filterTags = new ArrayList<>();
        if (filterCard.getItem() instanceof FilterTag) {
            filterTags = FilterTag.getTags(filterCard);
        }
        return filterTags;
    }

    public boolean isStackValidForCard(ItemStack testStack) {
        if (filterCard.equals(ItemStack.EMPTY)) return true; //If theres no filter in the card
        ItemStackKey key = new ItemStackKey(testStack, isCompareNBT);
        if (filterCache.containsKey(key)) return filterCache.get(key);
        if (filterCard.getItem() instanceof FilterMod) {
            for (ItemStack stack : filteredItems) {
                if (stack.getItem().getCreatorModId(stack).equals(testStack.getItem().getCreatorModId(testStack))) {
                    filterCache.put(key, isAllowList);
                    return isAllowList;
                }
            }
        } else if (filterCard.getItem() instanceof FilterTag) {
            for (TagKey tagKey : testStack.getItem().builtInRegistryHolder().tags().toList()) {
                String tag = tagKey.location().toString().toLowerCase(Locale.ROOT);
                if (filterTags.contains(tag)) {
                    filterCache.put(key, isAllowList);
                    return isAllowList;
                }
            }
        } else {
            for (ItemStack stack : filteredItems) {
                if (key.equals(new ItemStackKey(stack, isCompareNBT))) {
                    filterCache.put(key, isAllowList);
                    return isAllowList;
                }
            }
        }
        filterCache.put(key, !isAllowList);
        return !isAllowList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseCardCache) {
            return ((BaseCardCache) obj).be.equals(this.be) && ((BaseCardCache) obj).direction.equals(this.direction) && ((BaseCardCache) obj).cardSlot == this.cardSlot;
        }
        return false;
    }
}
