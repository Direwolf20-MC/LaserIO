package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterMod;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.util.BaseCardCache;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class MekanismCardCache {
    private final BaseCardCache baseCardCache;
    private final List<ChemicalStack> filteredChemicals;
    private final Reference2BooleanMap<Chemical> filterCacheChemical = new Reference2BooleanOpenHashMap<>();
    private final Reference2IntMap<Chemical> filterCountsChemical = new Reference2IntOpenHashMap<>();

    public MekanismCardCache(BaseCardCache baseCardCache) {
        this.baseCardCache = baseCardCache;
        if (this.baseCardCache.filterCard.isEmpty()) {
            filteredChemicals = new ArrayList<>();
        } else {
            this.filteredChemicals = getFilteredChemicals();
        }
    }

    public List<ChemicalStack> getFilteredChemicals() {
        //Note: We can use an enum map instead of having to use a linked map because the iteration order is already the same as what we want
        List<ChemicalStack> filteredChemicals = new ArrayList<>();
        ComponentItemHandler filterSlotHandler;
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.getItem() instanceof FilterBasic)
            filterSlotHandler = FilterBasic.getInventory(filterCard);
        else
            filterSlotHandler = FilterCount.getInventory(filterCard);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                IChemicalHandler handler = itemStack.getCapability(MekanismStatics.getItemCapabilityForChemical());
                if (handler != null) {
                    for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                        ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                        if (!chemicalStack.isEmpty())
                            filteredChemicals.add(chemicalStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
                    }
                }

            }
        }
        return filteredChemicals;
    }

    public boolean isStackValidForCard(ChemicalStack testStack) {
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.isEmpty()) return true; //If theres no filter in the card
        return filterCacheChemical.computeIfAbsent(testStack.getChemical(), (Chemical key) -> {
            boolean matches;
            if (filterCard.getItem() instanceof FilterTag) {
                matches = key.getTags().map(tagKey -> tagKey.location().toString().toLowerCase(Locale.ROOT)).anyMatch(baseCardCache.filterTags::contains);
            } else {
                Predicate<ChemicalStack> validityPredicate;
                if (filterCard.getItem() instanceof FilterMod) {
                    validityPredicate = stack -> stack.getTypeRegistryName().getNamespace().equals(key.getRegistryName().getNamespace());
                } else {
                    validityPredicate = stack -> key == stack.getChemical();
                }
                matches = filteredChemicals.stream().anyMatch(validityPredicate);
            }
            return matches == baseCardCache.isAllowList;
        });
    }

    public int getFilterAmt(ChemicalStack testStack) {
        ItemStack filterCard = baseCardCache.filterCard;
        if (filterCard.isEmpty())
            return 0; //If theres no filter in the card (This should never happen in theory)
        if (!(filterCard.getItem() instanceof FilterCount)) { //If this is a basic or tag Card return -1 which will mean infinite amount
            return -1;
        }
        //If we've already tested this, get it from the cache
        return filterCountsChemical.computeIfAbsent(testStack.getChemical(), (Chemical key) -> {
            FilterCountHandler filterSlotHandler = FilterCount.getInventory(filterCard);
            for (int i = 0; i < filterSlotHandler.getSlots(); i++) { //Gotta iterate the card's NBT because of the way we store amounts (in the MBAmt tag)
                ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    ChemicalStack chemicalStack = MekanismStatics.getFirstChemicalOnItemStack(itemStack);
                    if (!chemicalStack.isEmpty() && key == chemicalStack.getChemical()) {
                        return FilterCount.getSlotAmount(filterCard, i) + (FilterCount.getSlotCount(filterCard, i) * 1000);
                    }
                }
            }
            return 0;//Should never get here in theory
        });
    }
}
