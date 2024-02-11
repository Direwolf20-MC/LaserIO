package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.*;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MekanismStatics {
    public static BlockCapability<IGasHandler, @Nullable Direction> GAS_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "gas_handler"), IGasHandler.class);
    public static BlockCapability<IInfusionHandler, @Nullable Direction> INFUSION_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static BlockCapability<IPigmentHandler, @Nullable Direction> PIGMENT_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static BlockCapability<ISlurryHandler, @Nullable Direction> SLURRY_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "slurry_handler"), ISlurryHandler.class);

    public static ItemCapability<IGasHandler, Void> GAS_CAPABILITY_ITEM = ItemCapability.createVoid(new ResourceLocation("mekanism", "gas_handler"), IGasHandler.class);
    public static ItemCapability<IInfusionHandler, Void> INFUSION_CAPABILITY_ITEM = ItemCapability.createVoid(new ResourceLocation("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static ItemCapability<IPigmentHandler, Void> PIGMENT_CAPABILITY_ITEM = ItemCapability.createVoid(new ResourceLocation("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static ItemCapability<ISlurryHandler, Void> SLURRY_CAPABILITY_ITEM = ItemCapability.createVoid(new ResourceLocation("mekanism", "slurry_handler"), ISlurryHandler.class);


    public static BlockCapability<? extends IChemicalHandler<?, ?>, @Nullable Direction> getCapabilityForChemical(ChemicalType chemicalType) {
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

    public static List<ChemicalStack<?>> getAllChemicalsOnItemStack(ItemStack itemStack) {
        List<ChemicalStack<?>> chemicalStackList = new ArrayList<>();
        if (itemStack.isEmpty()) return chemicalStackList;
        IGasHandler gasHandler = itemStack.getCapability(MekanismStatics.GAS_CAPABILITY_ITEM);
        if (gasHandler != null) {
            for (int tank = 0; tank < gasHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = gasHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalStackList.add(chemicalStack);
            }
        }
        IInfusionHandler infusionHandler = itemStack.getCapability(MekanismStatics.INFUSION_CAPABILITY_ITEM);
        if (infusionHandler != null) {
            for (int tank = 0; tank < infusionHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = infusionHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalStackList.add(chemicalStack);
            }
        }
        IPigmentHandler pigmentHandler = itemStack.getCapability(MekanismStatics.PIGMENT_CAPABILITY_ITEM);
        if (pigmentHandler != null) {
            for (int tank = 0; tank < pigmentHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = pigmentHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalStackList.add(chemicalStack);
            }
        }
        ISlurryHandler slurryHandler = itemStack.getCapability(MekanismStatics.SLURRY_CAPABILITY_ITEM);
        if (slurryHandler != null) {
            for (int tank = 0; tank < slurryHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = slurryHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    chemicalStackList.add(chemicalStack);
            }
        }
        return chemicalStackList;
    }

    public static ChemicalStack<?> getFirstChemicalOnItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return GasStack.EMPTY; //TODO Should I change this to something more generic?
        IGasHandler gasHandler = itemStack.getCapability(MekanismStatics.GAS_CAPABILITY_ITEM);
        if (gasHandler != null) {
            for (int tank = 0; tank < gasHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = gasHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        IInfusionHandler infusionHandler = itemStack.getCapability(MekanismStatics.INFUSION_CAPABILITY_ITEM);
        if (infusionHandler != null) {
            for (int tank = 0; tank < infusionHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = infusionHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        IPigmentHandler pigmentHandler = itemStack.getCapability(MekanismStatics.PIGMENT_CAPABILITY_ITEM);
        if (pigmentHandler != null) {
            for (int tank = 0; tank < pigmentHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = pigmentHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        ISlurryHandler slurryHandler = itemStack.getCapability(MekanismStatics.SLURRY_CAPABILITY_ITEM);
        if (slurryHandler != null) {
            for (int tank = 0; tank < slurryHandler.getTanks(); tank++) {
                ChemicalStack<?> chemicalStack = slurryHandler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty())
                    return chemicalStack;
            }
        }
        return GasStack.EMPTY;
    }

    public static List<String> getTagsFromItemStack(ItemStack itemStack) {
        List<String> tagsList = new ArrayList<>();
        List<ChemicalStack<?>> chemicalStackList = getAllChemicalsOnItemStack(itemStack);
        for (ChemicalStack<?> chemicalStack : chemicalStackList) {
            chemicalStack.getType().getTags().forEach(t -> {
                String tag = t.location().toString().toLowerCase(Locale.ROOT);
                if (!tagsList.contains(tag) && !tagsList.contains(tag))
                    tagsList.add(tag);
            });
        }
        return tagsList;
    }

    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>, DIRECTION extends Direction> BlockCapability<HANDLER, DIRECTION> getCapabilityForChemical(CHEMICAL chemical) {
        return (BlockCapability<HANDLER, DIRECTION>) getCapabilityForChemical(ChemicalType.getTypeFor(chemical));
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>, DIRECTION extends Direction>
    BlockCapability<HANDLER, DIRECTION> getCapabilityForChemical(STACK stack) {
        return getCapabilityForChemical(stack.getType());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>, DIRECTION extends Direction>
    BlockCapability<HANDLER, DIRECTION> getCapabilityForChemical(IChemicalTank<CHEMICAL, STACK> tank) {
        //Note: We just use getEmptyStack as it still has enough information
        return getCapabilityForChemical(tank.getEmptyStack());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean hasChemical(ItemStack stack, ItemCapability<HANDLER, Void> capability) {
        HANDLER handler = stack.getCapability(capability);
        if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); ++tank) {
                STACK chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
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
