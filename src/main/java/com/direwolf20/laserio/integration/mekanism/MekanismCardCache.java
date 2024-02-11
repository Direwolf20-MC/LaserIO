package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterMod;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.util.BaseCardCache;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                IGasHandler gasHandler = itemStack.getCapability(MekanismStatics.GAS_CAPABILITY_ITEM);
                if (gasHandler != null) {
                    for (int tank = 0; tank < gasHandler.getTanks(); tank++) {
                        ChemicalStack<?> chemicalStack = gasHandler.getChemicalInTank(tank);
                        if (!chemicalStack.isEmpty())
                            filteredChemicals.add(chemicalStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
                    }
                }
                IInfusionHandler infusionHandler = itemStack.getCapability(MekanismStatics.INFUSION_CAPABILITY_ITEM);
                if (infusionHandler != null) {
                    for (int tank = 0; tank < infusionHandler.getTanks(); tank++) {
                        ChemicalStack<?> chemicalStack = infusionHandler.getChemicalInTank(tank);
                        if (!chemicalStack.isEmpty())
                            filteredChemicals.add(chemicalStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
                    }
                }
                IPigmentHandler pigmentHandler = itemStack.getCapability(MekanismStatics.PIGMENT_CAPABILITY_ITEM);
                if (pigmentHandler != null) {
                    for (int tank = 0; tank < pigmentHandler.getTanks(); tank++) {
                        ChemicalStack<?> chemicalStack = pigmentHandler.getChemicalInTank(tank);
                        if (!chemicalStack.isEmpty())
                            filteredChemicals.add(chemicalStack); //If this is a basic card it'll always be one, but getFilterAmt handles the proper logic of returning a value
                    }
                }
                ISlurryHandler slurryHandler = itemStack.getCapability(MekanismStatics.SLURRY_CAPABILITY_ITEM);
                if (slurryHandler != null) {
                    for (int tank = 0; tank < slurryHandler.getTanks(); tank++) {
                        ChemicalStack<?> chemicalStack = slurryHandler.getChemicalInTank(tank);
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
            for (TagKey tagKey : testStack.getType().getTags().toList()) {
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
}
