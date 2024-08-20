package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE.SideConnection;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.ExtractorCardCache;
import com.direwolf20.laserio.util.InserterCardCache;
import com.direwolf20.laserio.util.ItemStackKey;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MekanismCache {
    private record LaserNodeChemicalHandler(LaserNodeBE be, IChemicalHandler<?, ?> handler) {

    }
	
    private final Map<SideConnection, LazyOptional<IGasHandler>> facingHandlerGas = new HashMap<>();
    private final HashMap<ExtractorCardCache, HashMap<ChemicalStackKey, List<InserterCardCache>>> inserterCacheChemical = new HashMap<>();
    
	private final LaserNodeBE laserNodeBE;


    public MekanismCache(LaserNodeBE laserNodeBE) {
        this.laserNodeBE = laserNodeBE;
    }
    
	/** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendChemicals(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(extractorCardCache.direction);
        Level level = laserNodeBE.getLevel();
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        LazyOptional<IGasHandler> chemicalHandlerOptional = getAttachedChemicalTank(extractorCardCache.direction, extractorCardCache.sneaky);
        if (!chemicalHandlerOptional.isPresent()) return false;
        IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerOptional.resolve().get();
        for (int tank = 0; tank < chemicalHandler.getTanks(); tank++) {
            ChemicalStack<?> chemicalStack = chemicalHandler.getChemicalInTank(tank);
            if (chemicalStack.isEmpty() /*|| !extractorCardCache.isStackValidForCard(fluidStack)*/) 
            	continue; //TODO Chemical Filtering
            ChemicalStack<?> extractStack = chemicalStack.copy();
            extractStack.setAmount(extractorCardCache.extractAmt);

            //TODO Chemical Filtering
            /*if (extractorCardCache.filterCard.getItem() instanceof FilterCount) { //If this is a count filter, only try to extract up to the amount in the filter
                int filterCount = extractorCardCache.getFilterAmt(extractStack);
                if (filterCount <= 0) continue; //This should never happen in theory...
                int amtInInv = fluidStack.getAmount();
                int amtAllowedToRemove = amtInInv - filterCount;
                if (amtAllowedToRemove <= 0) continue;
                int amtRemaining = Math.min(extractStack.getAmount(), amtAllowedToRemove);
                extractStack.setAmount(amtRemaining);
            }

            if (extractorCardCache.exact) {
                if (extractFluidStackExact(extractorCardCache, adjacentTank, extractStack))
                    return true;
            } else {
                if (extractFluidStack(extractorCardCache, adjacentTank, extractStack))
                    return true;
            }*/

            if (extractChemicalStack(extractorCardCache, chemicalHandler, extractStack))
                return true;
        }
        return false;
    }
    
    public boolean extractChemicalStack(ExtractorCardCache extractorCardCache, IChemicalHandler<?, ?> fromInventory, ChemicalStack<?> extractStack) {
    	GasStack gasExtractStack;
    	if (extractStack instanceof GasStack gasStack) //TODO Other Chemicals
            gasExtractStack = gasStack.copy();
        else
            return false;
    	
    	long totalAmtNeeded = extractStack.getAmount();
        long amtToExtract = extractStack.getAmount();
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int roundRobin = -1;
        boolean foundAnything = false;
        if (extractorCardCache.roundRobin != 0) {
            roundRobin = laserNodeBE.getRR(extractorCardCache);
            inserterCardCaches = laserNodeBE.applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
        	LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache);
            if (laserNodeChemicalHandler == null) continue;
            IChemicalHandler<?, ?> handler = laserNodeChemicalHandler.handler;
            IGasHandler gasHandler;
            IGasHandler fromGas;
            if (handler instanceof IGasHandler && fromInventory instanceof IGasHandler) {
            	System.out.println("PIPPO");
            	
            	gasHandler = (IGasHandler) handler;
            	fromGas = (IGasHandler) fromInventory;
            } else
            	return false;
            
            //for (int tank = 0; tank < handler.getTanks(); tank++) {
            /*if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    FluidStack fluidStack = handler.getFluidInTank(tank);
                    if (fluidStack.isEmpty() || fluidStack.isFluidEqual(extractStack)) {
                        int currentAmt = fluidStack.getAmount();
                        int neededAmt = filterCount - currentAmt;
                        if (neededAmt < extractStack.getAmount()) {
                            amtToExtract = neededAmt;
                            break;
                        }
                    }
                }
            }*/
            if (amtToExtract == 0) {
                amtToExtract = totalAmtNeeded;
                continue;
            }
            gasExtractStack.setAmount(amtToExtract);
            long amtReturned = gasHandler.insertChemical(gasExtractStack, Action.SIMULATE).getAmount();
            if (amtReturned == amtToExtract) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            gasExtractStack.setAmount(amtToExtract - amtReturned);
            GasStack drainedStack = fromGas.extractChemical(gasExtractStack, Action.EXECUTE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            foundAnything = true;
            gasHandler.insertChemical(drainedStack, Action.EXECUTE);
            //TODO Gas Particles
            //drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
            totalAmtNeeded -= drainedStack.getAmount();
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return true;
        }

        return foundAnything;
    }
    
    /** Finds all inserters that can be extracted to **/
    public List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, ChemicalStack<?> stack) {
        ChemicalStackKey key = new ChemicalStackKey(stack);
        inserterCacheChemical.clear(); //TODO Properly Handle This
        if (inserterCacheChemical.containsKey(extractorCardCache)) { //If this extractor card is already in the cache
            if (inserterCacheChemical.get(extractorCardCache).containsKey(key)) //If this extractor card AND itemKey are already in the cache
                return inserterCacheChemical.get(extractorCardCache).get(key); //Return the cached results
            else { //Find the list of items that can be extracted by this extractor and cache them
                List<InserterCardCache> nodes = laserNodeBE.getInserterNodes().stream().filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.enabled)
                                //&& (p.isStackValidForCard(stack)) //TODO Chemical Exact Mode
                                && (p.cardType.equals(extractorCardCache.cardType))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                        .toList();
                inserterCacheChemical.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else { //Find the list of items that can be extracted by this extractor and cache them along with the extractor card
            List<InserterCardCache> nodes = laserNodeBE.getInserterNodes().stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.enabled)
                            //&& (p.isStackValidForCard(stack)) //TODO Chemical Exact Mode
                            && (p.cardType.equals(extractorCardCache.cardType))
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                    .toList();
            HashMap<ChemicalStackKey, List<InserterCardCache>> tempMap = new HashMap<>();
            tempMap.put(key, nodes);
            inserterCacheChemical.put(extractorCardCache, tempMap);
            return nodes;
        }
    }
    
    public LaserNodeChemicalHandler getLaserNodeHandlerChemical(InserterCardCache inserterCardCache) {
        if (!inserterCardCache.cardType.equals(BaseCard.CardType.CHEMICAL)) return null;
        Level level = laserNodeBE.getLevel();
        DimBlockPos nodeWorldPos = new DimBlockPos(inserterCardCache.relativePos.getLevel(level.getServer()), laserNodeBE.getWorldPos(inserterCardCache.relativePos.blockPos));
        if (!laserNodeBE.chunksLoaded(nodeWorldPos, nodeWorldPos.blockPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = laserNodeBE.getNodeAt(new DimBlockPos(inserterCardCache.relativePos.getLevel(level.getServer()), laserNodeBE.getWorldPos(inserterCardCache.relativePos.blockPos)));
        if (be == null) return null;
        LazyOptional<IGasHandler> chemicalHandlerOptional = be.mekanismCache.getAttachedChemicalTank(inserterCardCache.direction, inserterCardCache.sneaky);
        if (!chemicalHandlerOptional.isPresent()) return null;
        IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerOptional.resolve().get();
        if (chemicalHandler.getTanks() == 0) return null;
        return new LaserNodeChemicalHandler(be, chemicalHandler);
    }
    
    /*public LazyOptional<IChemicalHandler<?, ?>> getAttachedChemicalTank(Direction direction, Byte sneakySide) {
    	facingHandlerGas.clear(); //TODO Fix this
    	Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        Level level = laserNodeBE.getLevel();
        LazyOptional<IFluidHandler> testHandler = (facingHandlerFluid.get(sideConnection));
        if (testHandler != null && testHandler.isPresent()) {
            return testHandler;
        }

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IFluidHandler> handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER, inventorySide);
            if (handler.isPresent()) {
                // add the invalidator
                handler.addListener(getInvalidatorFluid(sideConnection));
                // cache and return
                facingHandlerFluid.put(sideConnection, handler);
                return handler;
            }
        }
        // no item handler, cache empty
        facingHandlerFluid.remove(sideConnection);
        return LazyOptional.empty();
    }*/
    
    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IGasHandler> getAttachedChemicalTank(Direction direction, Byte sneakySide) {
    	facingHandlerGas.clear(); //TODO Fix this
    	Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        Level level = laserNodeBE.getLevel();
        assert level != null;
        BlockEntity be = level.getBlockEntity(laserNodeBE.getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IGasHandler> gasHandler = be.getCapability(MekanismStaticRefs.GAS_CAPABILITY, inventorySide);
            if (gasHandler.isPresent()) {
                // add the invalidator
            	//gasHandler.addListener(getInvalidatorFluid(sideConnection));
                // cache and return
            	//facingHandlerGas.put(sideConnection, gasHandler);
                return gasHandler;
            }
        }
        // no item handler, cache empty
        //facingHandlerFluid.remove(sideConnection);
        return LazyOptional.empty();
    }
    
}