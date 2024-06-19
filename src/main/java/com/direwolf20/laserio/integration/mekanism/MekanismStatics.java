package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.SlurryStack;
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
    public static BlockCapability<IGasHandler, @Nullable Direction> GAS_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "gas_handler"), IGasHandler.class);
    public static BlockCapability<IInfusionHandler, @Nullable Direction> INFUSION_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static BlockCapability<IPigmentHandler, @Nullable Direction> PIGMENT_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static BlockCapability<ISlurryHandler, @Nullable Direction> SLURRY_CAPABILITY = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "slurry_handler"), ISlurryHandler.class);

    public static ItemCapability<IGasHandler, Void> GAS_CAPABILITY_ITEM = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "gas_handler"), IGasHandler.class);
    public static ItemCapability<IInfusionHandler, Void> INFUSION_CAPABILITY_ITEM = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static ItemCapability<IPigmentHandler, Void> PIGMENT_CAPABILITY_ITEM = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static ItemCapability<ISlurryHandler, Void> SLURRY_CAPABILITY_ITEM = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "slurry_handler"), ISlurryHandler.class);


    public static BlockCapability<? extends IChemicalHandler<?, ?>, @Nullable Direction> getCapabilityForChemical(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> GAS_CAPABILITY;
            case INFUSION -> INFUSION_CAPABILITY;
            case PIGMENT -> PIGMENT_CAPABILITY;
            case SLURRY -> SLURRY_CAPABILITY;
        };
    }

    public static ItemCapability<? extends IChemicalHandler<?, ?>, Void> getItemCapabilityForChemical(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> GAS_CAPABILITY_ITEM;
            case INFUSION -> INFUSION_CAPABILITY_ITEM;
            case PIGMENT -> PIGMENT_CAPABILITY_ITEM;
            case SLURRY -> SLURRY_CAPABILITY_ITEM;
        };
    }

    public static boolean doesItemStackHoldChemicals(ItemStack itemStack) {
        return !getFirstChemicalOnItemStack(itemStack).isEmpty();
    }

    private static Set<Chemical<?>> getAllChemicalsOnItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return Set.of();
        Set<Chemical<?>> chemicalList = new LinkedHashSet<>();
        for (ChemicalType chemicalType : ChemicalType.values()) {
            IChemicalHandler<?, ?> handler = itemStack.getCapability(getItemCapabilityForChemical(chemicalType));
            if (handler != null) {
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    ChemicalStack<?> chemicalStack = handler.getChemicalInTank(tank);
                    if (!chemicalStack.isEmpty())
                        chemicalList.add(chemicalStack.getChemical());
                }
            }
        }
        return chemicalList;
    }

    public static ChemicalStack<?> getFirstChemicalOnItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return GasStack.EMPTY;
        for (ChemicalType chemicalType : ChemicalType.values()) {
            ChemicalStack<?> chemicalStack = getFirstChemicalOnItemStack(itemStack, chemicalType);
            if (!chemicalStack.isEmpty())
                return chemicalStack;
        }
        return GasStack.EMPTY;
    }

    public static ChemicalStack<?> getFirstChemicalOnItemStack(ItemStack itemStack, ChemicalType chemicalType) {
        IChemicalHandler<?, ?> handler = itemStack.getCapability(getItemCapabilityForChemical(chemicalType));
        if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        return switch (chemicalType) {
            case GAS -> GasStack.EMPTY;
            case INFUSION -> InfusionStack.EMPTY;
            case PIGMENT -> PigmentStack.EMPTY;
            case SLURRY -> SlurryStack.EMPTY;
        };
    }

    public static Stream<? extends TagKey<?>> getTagsFromItemStack(ItemStack itemStack) {
        return getAllChemicalsOnItemStack(itemStack).stream().flatMap(Chemical::getTags);
    }

    /**
     * Helper to copy a chemical stack when we don't know what implementation it is.
     *
     * @param stack Stack to copy
     * @return Copy of the input stack with the desired size
     * @apiNote Should only be called if we know that copy returns STACK
     */
    @SuppressWarnings("unchecked")
    public static <STACK extends ChemicalStack<?>> STACK copy(STACK stack) {
        return (STACK) stack.copy();
    }
}
