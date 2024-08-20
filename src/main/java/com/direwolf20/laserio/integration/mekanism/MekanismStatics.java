package com.direwolf20.laserio.integration.mekanism;

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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MekanismStatics {
    public static Capability<IGasHandler> GAS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IInfusionHandler> INFUSION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IPigmentHandler> PIGMENT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<ISlurryHandler> SLURRY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static Capability<? extends IChemicalHandler<?, ?>> getCapabilityForChemical(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> GAS_CAPABILITY;
            case INFUSION -> INFUSION_CAPABILITY;
            case PIGMENT -> PIGMENT_CAPABILITY;
            case SLURRY -> SLURRY_CAPABILITY;
        };
    }

    public static boolean doesItemStackHoldChemicals(ItemStack itemStack) {
        return !getFirstChemicalOnItemStack(itemStack).isEmpty();
    }

    public static ChemicalStack<?> getFirstChemicalOnItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return GasStack.EMPTY;

        ItemStack testStack = itemStack.copy();
        if (testStack.getCount() > 1)
            testStack.setCount(1);
        for (ChemicalType chemicalType : ChemicalType.values()) {
            LazyOptional<? extends IChemicalHandler<?, ?>> chemicalHandlerOptional = testStack.getCapability(getCapabilityForChemical(chemicalType));
            if (!chemicalHandlerOptional.isPresent())
                continue;
            IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerOptional.resolve().get();
            for (int tank = 0; tank < chemicalHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = chemicalHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }

        return GasStack.EMPTY;
    }

    public static List<ChemicalStack<?>> getAllChemicalsOnItemStack(ItemStack itemStack) {
        List<ChemicalStack<?>> chemicalStackList = new ArrayList<>();
        if (itemStack.isEmpty())
            return chemicalStackList;

        ItemStack testStack = itemStack.copy();
        if (testStack.getCount() > 1)
            testStack.setCount(1);
        for (ChemicalType chemicalType : ChemicalType.values()) {
            LazyOptional<? extends IChemicalHandler<?, ?>> chemicalHandlerOptional = testStack.getCapability(getCapabilityForChemical(chemicalType));
            if (!chemicalHandlerOptional.isPresent())
                continue;
            IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerOptional.resolve().get();
            for (int tank = 0; tank < chemicalHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = chemicalHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalStackList.add(chemicalStack);
            }
        }

        return chemicalStackList;
    }

    public static List<String> getTagsFromItemStack(ItemStack itemStack) {
        List<String> tagsList = new ArrayList<>();
        List<ChemicalStack<?>> chemicalStackList = getAllChemicalsOnItemStack(itemStack);
        for (ChemicalStack<?> chemicalStack : chemicalStackList) {
            chemicalStack.getType().getTags().forEach(t -> {
                String tag = t.location().toString().toLowerCase(Locale.ROOT);
                if (!tagsList.contains(tag))
                    tagsList.add(tag);
            });
        }

        return tagsList;
    }

    public static boolean isValidChemicalForHandler(IChemicalHandler<?, ?> handler, ChemicalStack<?> chemicalStack) {
        // Check if the handler is a gas handler
        if (handler instanceof IGasHandler) {
            // Ensure the chemical stack is also a gas stack
            return chemicalStack instanceof GasStack;
        }
        if (handler instanceof ISlurryHandler) {
            // Ensure the chemical stack is also a gas stack
            return chemicalStack instanceof SlurryStack;
        }
        if (handler instanceof IPigmentHandler) {
            // Ensure the chemical stack is also a gas stack
            return chemicalStack instanceof PigmentStack;
        }
        if (handler instanceof IInfusionHandler) {
            // Ensure the chemical stack is also a gas stack
            return chemicalStack instanceof InfusionStack;
        }

        return false;
    }
}