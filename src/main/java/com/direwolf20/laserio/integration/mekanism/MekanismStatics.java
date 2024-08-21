package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MekanismStatics {
    public static BlockCapability<IChemicalHandler, @Nullable Direction> CHEMICAL_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);
    public static ItemCapability<IChemicalHandler, Void> CHEMICAL_CAPABILITY_ITEM = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);

    public static BlockCapability<IChemicalHandler, @Nullable Direction> getCapabilityForChemical() {
        return CHEMICAL_CAPABILITY;
    }

    public static ItemCapability<IChemicalHandler, Void> getItemCapabilityForChemical() {
        return CHEMICAL_CAPABILITY_ITEM;
    }

    public static boolean doesItemStackHoldChemicals(ItemStack itemStack) {
        return !getFirstChemicalOnItemStack(itemStack).isEmpty();
    }

    private static Set<Chemical> getAllChemicalsOnItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return Set.of();
        Set<Chemical> chemicalList = new LinkedHashSet<>();
        IChemicalHandler handler = itemStack.getCapability(getItemCapabilityForChemical());
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalList.add(chemicalStack.getChemical());
            }
        }
        return chemicalList;
    }

    public static ChemicalStack getFirstChemicalOnItemStack(ItemStack itemStack) {
        ItemStack testStack = itemStack.copy();
        testStack.setCount(1);
        IChemicalHandler handler = testStack.getCapability(getItemCapabilityForChemical());
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        return ChemicalStack.EMPTY;
    }

    public static Stream<? extends TagKey<?>> getTagsFromItemStack(ItemStack itemStack) {
        return getAllChemicalsOnItemStack(itemStack).stream().flatMap(Chemical::getTags);
    }
}
