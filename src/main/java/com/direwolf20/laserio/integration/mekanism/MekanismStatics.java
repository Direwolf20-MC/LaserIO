package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MekanismStatics {
	//TODO: Use Mekanism capabilities here
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
        	return GasStack.EMPTY; //TODO Should I change this to something more generic?
    	
    	for (ChemicalType chemicalType : ChemicalType.values()) {
			LazyOptional<? extends IChemicalHandler<?, ?>> chemicalHandlerOptional = itemStack.getCapability(getCapabilityForChemical(chemicalType));
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
        
        for (ChemicalType chemicalType : ChemicalType.values()) {
			LazyOptional<? extends IChemicalHandler<?, ?>> chemicalHandlerOptional = itemStack.getCapability(getCapabilityForChemical(chemicalType));
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
    
//    @SuppressWarnings("unchecked")
//    public static <CHEMICAL extends Chemical<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, ?>> Capability<HANDLER> getCapabilityForChemical(CHEMICAL chemical) {
//        return (Capability<HANDLER>) getCapabilityForChemical(ChemicalType.getTypeFor(chemical));
//    }
//    
//    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(STACK stack) {
//        return getCapabilityForChemical(stack.getType());
//    }
//    
//    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> Capability<HANDLER> getCapabilityForChemical(IChemicalTank<CHEMICAL, STACK> tank) {
//        //Note: We just use getEmptyStack as it still has enough information
//        return getCapabilityForChemical(tank.getEmptyStack());
//    }
// 
//    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean hasChemical(ItemStack stack, Capability<HANDLER> capability) {
//        LazyOptional<HANDLER> handlerOptional = stack.getCapability(capability);
//        if (handlerOptional.isPresent()) {
//        	HANDLER handler = handlerOptional.resolve().get();
//            for (int tank = 0; tank < handler.getTanks(); ++tank) {
//                STACK chemicalStack = handler.getChemicalInTank(tank);
//                if (!chemicalStack.isEmpty()) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//    
//    /**
//     * Helper to copy a chemical stack when we don't know what implementation it is.
//     *
//     * @param stack Stack to copy
//     * @return Copy of the input stack with the desired size
//     * @apiNote Should only be called if we know that copy returns STACK
//     */
//    @SuppressWarnings("unchecked")
//    public static <STACK extends ChemicalStack<?>> STACK copy(STACK stack) {
//        return (STACK) stack.copy();
//    }
    
}