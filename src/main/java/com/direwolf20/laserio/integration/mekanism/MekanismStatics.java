package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.*;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public class MekanismStatics {
    public static BlockCapability<IGasHandler, @Nullable Direction> GAS_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "gas_handler"), IGasHandler.class);
    public static BlockCapability<IInfusionHandler, @Nullable Direction> INFUSION_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "infusion_handler"), IInfusionHandler.class);
    public static BlockCapability<IPigmentHandler, @Nullable Direction> PIGMENT_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "pigment_handler"), IPigmentHandler.class);
    public static BlockCapability<ISlurryHandler, @Nullable Direction> SLURRY_CAPABILITY = BlockCapability.createSided(new ResourceLocation("mekanism", "slurry_handler"), ISlurryHandler.class);

    public static BlockCapability<? extends IChemicalHandler<?, ?>, @Nullable Direction> getCapabilityForChemical(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> GAS_CAPABILITY;
            case INFUSION -> INFUSION_CAPABILITY;
            case PIGMENT -> PIGMENT_CAPABILITY;
            case SLURRY -> SLURRY_CAPABILITY;
        };
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
