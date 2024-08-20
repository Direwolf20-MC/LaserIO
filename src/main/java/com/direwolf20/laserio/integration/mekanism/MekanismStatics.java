package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

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
    
    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>> Capability<HANDLER> getCapabilityForChemical(CHEMICAL chemical) {
        return (Capability<HANDLER>) getCapabilityForChemical(ChemicalType.getTypeFor(chemical));
    }
    
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(STACK stack) {
        return getCapabilityForChemical(stack.getType());
    }
    
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(IChemicalTank<CHEMICAL, STACK> tank) {
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