package com.direwolf20.laserio.integration.mekanism;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterMod;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.util.BaseCardCache;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class MekanismCardCache {
    public final BaseCardCache baseCardCache;
    public final List<ChemicalStack<?>> filteredChemicals;
    public final Map<ChemicalStackKey, Boolean> filterCacheChemical = new Object2BooleanOpenHashMap<>();
    public final Map<ChemicalStackKey, Integer> filterCountsChemical = new Object2IntOpenHashMap<>();

    public MekanismCardCache(BaseCardCache baseCardCache) {
        this.baseCardCache = baseCardCache;
        if (this.baseCardCache.filterCard.equals(ItemStack.EMPTY)) {
            filteredChemicals = new ArrayList<>();
        } else {
            this.filteredChemicals = getFilteredChemicals();
        }
    }

    public List<ChemicalStack<?>> getFilteredChemicals() {
        List<ChemicalStack<?>> filteredChemicals = new ArrayList<>();
        ItemStackHandler filterSlotHandler;
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.getItem() instanceof FilterBasic)
            filterSlotHandler = FilterBasic.getInventory(filterCard);
        else
            filterSlotHandler = FilterCount.getInventory(filterCard);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                for (ChemicalType chemicalType : ChemicalType.values()) {
                    LazyOptional<? extends IChemicalHandler<?, ?>> chemicalHandlerOptional = itemStack.getCapability(MekanismStatics.getCapabilityForChemical(chemicalType));
                    if (!chemicalHandlerOptional.isPresent())
                        continue;
                    IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerOptional.resolve().get();
                    for (int tank = 0; tank < chemicalHandler.getTanks(); tank++) {
                        ChemicalStack<?> chemicalStack = chemicalHandler.getChemicalInTank(tank);
                        if (!chemicalStack.isEmpty())
                            filteredChemicals.add(chemicalStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
                    }
                }
            }
        }
        return filteredChemicals;
    }

    public boolean isStackValidForCard(ChemicalStack<?> testStack) {
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.equals(ItemStack.EMPTY)) return true; //If theres no filter in the card
        ChemicalStackKey key = new ChemicalStackKey(testStack);
        if (filterCacheChemical.containsKey(key)) return filterCacheChemical.get(key);
        if (filterCard.getItem() instanceof FilterMod) {
            for (ChemicalStack<?> stack : filteredChemicals) {
                if (stack.getTypeRegistryName().getNamespace().equals(testStack.getTypeRegistryName().getNamespace())) {
                    filterCacheChemical.put(key, baseCardCache.isAllowList);
                    return baseCardCache.isAllowList;
                }
            }
        } else if (filterCard.getItem() instanceof FilterTag) {
            for (TagKey<?> tagKey : testStack.getType().getTags().toList()) {
                String tag = tagKey.location().toString().toLowerCase(Locale.ROOT);
                if (baseCardCache.filterTags.contains(tag)) {
                    filterCacheChemical.put(key, baseCardCache.isAllowList);
                    return baseCardCache.isAllowList;
                }
            }
        } else {
            for (ChemicalStack<?> stack : filteredChemicals) {
                if (key.equals(new ChemicalStackKey(stack))) {
                    filterCacheChemical.put(key, baseCardCache.isAllowList);
                    return baseCardCache.isAllowList;
                }
            }
        }
        filterCacheChemical.put(key, !baseCardCache.isAllowList);
        return !baseCardCache.isAllowList;
    }

    public int getFilterAmt(ChemicalStack<?> testStack) {
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.equals(ItemStack.EMPTY))
            return 0; //If theres no filter in the card (This should never happen in theory)
        if (!(filterCard.getItem() instanceof FilterCount)) { //If this is a basic or tag Card return -1 which will mean infinite amount
            return -1;
        }
        ChemicalStackKey key = new ChemicalStackKey(testStack);
        if (filterCountsChemical.containsKey(key)) //If we've already tested this, get it from the cache
            return filterCountsChemical.get(key);

        FilterCountHandler filterSlotHandler = FilterCount.getInventory(filterCard);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) { //Gotta iterate the card's NBT because of the way we store amounts (in the MBAmt tag)
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                ChemicalStack<?> chemicalStack = MekanismStatics.getFirstChemicalOnItemStack(itemStack);
                if (chemicalStack.isEmpty()) continue;
                if (key.equals(new ChemicalStackKey(chemicalStack))) {
                    int mbAmt = FilterCount.getSlotAmount(filterCard, i);
                    filterCountsChemical.put(key, mbAmt);
                    return mbAmt;
                }

            }
        }
        filterCountsChemical.put(key, 0);
        return 0; //Should never get here in theory
    }

}