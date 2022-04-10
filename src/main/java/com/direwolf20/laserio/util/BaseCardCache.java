package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.filters.FilterBasic;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BaseCardCache {
    public final Direction direction;
    public final byte channel;
    public final ItemStack filterCard;
    public final Set<ItemStack> filteredItems;
    public final boolean isAllowList;
    public final HashMap<ItemStackKey, Boolean> filterCache = new HashMap<>();

    public BaseCardCache(Direction direction, byte channel, ItemStack filterCard) {
        this.direction = direction;
        this.channel = channel;
        this.filterCard = filterCard;
        if (filterCard.equals(ItemStack.EMPTY)) {
            filteredItems = new HashSet<>();
            isAllowList = false;
        } else {
            this.filteredItems = getFilteredItems();
            isAllowList = true; //TODO Card sets allow list.
        }
    }

    public Set<ItemStack> getFilteredItems() {
        Set<ItemStack> filteredItems = new HashSet<>();
        ItemStackHandler filterSlotHandler = FilterBasic.getInventory(filterCard);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty())
                filteredItems.add(itemStack);
        }
        return filteredItems;
    }

    public boolean isStackValidForCard(ItemStack testStack) {
        if (filterCard.equals(ItemStack.EMPTY)) return true; //If theres no filter in the card
        ItemStackKey key = new ItemStackKey(testStack, false); //TODO Allow cards to check NBT
        if (filterCache.containsKey(key)) return filterCache.get(key);
        /*if (filterCard.getItem() instanceof CardInserterTag) {
            List<String> tags = new ArrayList<>(CardInserterTag.getTags(filterCard));
            for (ResourceLocation tag : testStack.getItem().getTags()) {
                if (tags.contains(tag.toString()))
                    return whiteList;
            }
        } else { */
        for (ItemStack stack : filteredItems) {
            /*if (filterCard.getItem() instanceof CardInserterMod) {
                if (Objects.equals(stack.getItem().getCreatorModId(stack), testStack.getItem().getCreatorModId(testStack)))
                    return whiteList;
            } else if (BaseCard.getNBTFilter(filterCard)) {
                if (ItemHandlerHelper.canItemStacksStack(stack, testStack))
                    return whiteList;
            } else {*/
            if (stack.sameItem(testStack)) {
                filterCache.put(key, isAllowList);
                return isAllowList;
            }
        }
        //}
        //}
        filterCache.put(key, !isAllowList);
        return !isAllowList;

    }
}
