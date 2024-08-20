package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE.SideConnection;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ChemicalFlowParticleData;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleRenderDataChemical;
import com.direwolf20.laserio.util.CardRender;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.ExtractorCardCache;
import com.direwolf20.laserio.util.InserterCardCache;
import com.direwolf20.laserio.util.StockerCardCache;
import com.direwolf20.laserio.util.WeakConsumerWrapper;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender.offsets;
import static com.direwolf20.laserio.integration.mekanism.MekanismStatics.isValidChemicalForHandler;
import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class MekanismCache {
    private record LaserNodeChemicalHandler(LaserNodeBE be, IChemicalHandler<?, ?> handler) {

    }
	
    public final Map<SideConnection, Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>>> facingHandlerChemical = new HashMap<>();
    private final Map<SideConnection, Map<ChemicalType, NonNullConsumer<LazyOptional<IChemicalHandler<?, ?>>>>> connectionInvalidatorChemical = new HashMap<>();
    public final Map<ExtractorCardCache, Map<ChemicalStackKey, List<InserterCardCache>>> inserterCacheChemical = new HashMap<>();
    
	private final LaserNodeBE laserNodeBE;

	private final Random random = new Random();
	
    public MekanismCache(LaserNodeBE laserNodeBE) {
        this.laserNodeBE = laserNodeBE;
    }
    
    public boolean stockChemicals(StockerCardCache stockerCardCache) {
        BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(stockerCardCache.direction);
        Level level = laserNodeBE.getLevel();
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> chemicalHandlerMap = getAttachedChemicalTanks(stockerCardCache.direction, stockerCardCache.sneaky);
        if (chemicalHandlerMap == null || chemicalHandlerMap.isEmpty()) return false;

        ItemStack filter = stockerCardCache.filterCard;
        if (filter.isEmpty() || !stockerCardCache.isAllowList) { //Needs a filter - at least for now? Also must be in whitelist mode
            return false;
        }
        
        for (Entry<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> entry : chemicalHandlerMap.entrySet()) {
            if (!entry.getValue().isPresent())
            	continue;
        	
        	IChemicalHandler<?, ?> chemicalHandler = entry.getValue().resolve().get();
            if (filter.getItem() instanceof FilterBasic || filter.getItem() instanceof FilterCount) {
                if (stockerCardCache.regulate && filter.getItem() instanceof FilterCount) {
                    if (regulateChemicalStocker(stockerCardCache, chemicalHandler, entry.getKey()))
                        return true;
                }
                if (!canAnyChemicalFiltersFit(chemicalHandler, stockerCardCache)) {
                    continue; //If we can't fit any of our filtered items into this inventory, don't bother scanning for them
                }
                boolean foundItems = findChemicalStackForStocker(stockerCardCache, chemicalHandler, entry.getKey()); //Start looking for this item
                if (foundItems)
                    return true;

            } else if (filter.getItem() instanceof FilterTag) {
                //No-Op
            }
        }
        return false;
    }
    
    public boolean regulateChemicalStocker(StockerCardCache stockerCardCache, IChemicalHandler stockerTank, ChemicalType chemicalType) {
        List<ChemicalStack<?>> filteredChemicalsList = stockerCardCache.mekanismCardCache.getFilteredChemicals();
        for (ChemicalStack<?> chemicalStack : filteredChemicalsList) { //Iterate the list of filtered items for extracting purposes
            int desiredAmt = stockerCardCache.mekanismCardCache.getFilterAmt(chemicalStack);
            int amtHad = 0;
            for (int tank = 0; tank < stockerTank.getTanks(); tank++) { //Loop through all the tanks
                ChemicalStack<?> stackInTank = stockerTank.getChemicalInTank(tank);
                if (new ChemicalStackKey(chemicalStack).equals(new ChemicalStackKey(stackInTank)))
                    amtHad += stackInTank.getAmount();
            }
            if (amtHad > desiredAmt) { //If we have too much of this fluid, remove the difference.
                chemicalStack.setAmount(Math.min(amtHad - desiredAmt, stockerCardCache.extractAmt));
                if (extractChemicalStack(stockerCardCache, stockerTank, chemicalStack, chemicalType))
                    return true;
            }
        }
        return false;
    }
    
    public boolean canAnyChemicalFiltersFit(IChemicalHandler chemicalHandler, StockerCardCache stockerCardCache) {
        for (ChemicalStack<?> chemicalStack : stockerCardCache.mekanismCardCache.getFilteredChemicals()) {
            if (!isValidChemicalForHandler(chemicalHandler, chemicalStack)) //Don't Check disparate types
                continue;
            long amtReturned = chemicalHandler.insertChemical(chemicalStack, Action.SIMULATE).getAmount();
            if (amtReturned < chemicalStack.getAmount()) //If any fit
                return true;
        }
        return false;
    }
    
    public boolean findChemicalStackForStocker(StockerCardCache stockerCardCache, IChemicalHandler stockerTank, ChemicalType chemicalType) {
        boolean isCount = stockerCardCache.filterCard.getItem() instanceof FilterCount;
        int extractAmt = stockerCardCache.extractAmt;

        List<ChemicalStack<?>> filteredChemicalsList = new CopyOnWriteArrayList<>(stockerCardCache.mekanismCardCache.getFilteredChemicals());
        filteredChemicalsList.removeIf(chemicalStack -> !canChemicalFitInTank(stockerTank, chemicalStack));//If this fluid can't fit in this tank at all, skip the fluid
        if (filteredChemicalsList.isEmpty()) //If nothing in the filter can fit, return false
            return false;

        if (isCount) { //If this is a filter count, prune the list of items to search for to just what we need
            for (ChemicalStack<?> chemicalStack : filteredChemicalsList) { //Remove all the items from the list that we already have enough of
                for (int tank = 0; tank < stockerTank.getTanks(); tank++) {
                    ChemicalStack<?> tankStack = stockerTank.getChemicalInTank(tank);
                    if (tankStack.isEmpty() || new ChemicalStackKey(chemicalStack).equals(new ChemicalStackKey(tankStack))) {
                        int filterAmt = stockerCardCache.mekanismCardCache.getFilterAmt(chemicalStack);
                        long amtHad = tankStack.getAmount();
                        long amtNeeded = filterAmt - amtHad;
                        if (amtNeeded <= 0) {//if we have enough, move onto the next stack after removing this one from the list
                            filteredChemicalsList.remove(chemicalStack);
                            continue;
                        }
                        chemicalStack.setAmount(Math.min(amtNeeded, extractAmt)); //Adjust the amount we need
                    }
                }
            }
        }

        if (filteredChemicalsList.isEmpty()) //If we have nothing left to look for! Probably only happens when its a count card.
            return false;


        //At this point we should have a list of fluids that we need to satisfy the stock request
        for (ChemicalStack<?> chemicalStack : filteredChemicalsList) {
            Map<InserterCardCache, ChemicalStack<?>> insertHandlers = new HashMap<>();
            if (!isCount)
                chemicalStack.setAmount(extractAmt); //If this isn't a counting card, we want the extractAmt value
            long amtNeeded = chemicalStack.getAmount();

            for (InserterCardCache inserterCardCache : laserNodeBE.getChannelMatchInserters(stockerCardCache)) { //Iterate through ALL inserter nodes on this channel only
                if (!inserterCardCache.mekanismCardCache.isStackValidForCard(chemicalStack))
                    continue;
                LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache, chemicalType);
                if (laserNodeChemicalHandler == null) continue;
                IChemicalHandler handler = laserNodeChemicalHandler.handler;
                chemicalStack.setAmount(amtNeeded);
                ChemicalStack<?> extractStack = handler.extractChemical(chemicalStack, Action.SIMULATE);
                if (extractStack.isEmpty()) continue;
                insertHandlers.put(inserterCardCache, extractStack);
                amtNeeded -= extractStack.getAmount();
                if (amtNeeded == 0) break;
            }
            if (!insertHandlers.isEmpty()) {
                if (!stockerCardCache.exact || amtNeeded == 0) { //If its not exact mode, or it is exact mode and we found all we need to satisfy this
                    for (Map.Entry<InserterCardCache, ChemicalStack<?>> entry : insertHandlers.entrySet()) { //Do all the extracts/inserts
                        InserterCardCache inserterCardCache = entry.getKey();
                        ChemicalStack<?> insertStack = entry.getValue();
                        LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache, chemicalType);
                        IChemicalHandler handler = laserNodeChemicalHandler.handler;
                        long amtReturned = stockerTank.insertChemical(insertStack, Action.SIMULATE).getAmount(); //Test inserting into the target
                        insertStack.setAmount(insertStack.getAmount() - amtReturned); //Change the stack to size to how much can fit
                        ChemicalStack<?> drainedStack = handler.extractChemical(insertStack, Action.EXECUTE);
                        stockerTank.insertChemical(drainedStack, Action.EXECUTE);
                        drawParticlesChemical(drainedStack, inserterCardCache.direction, inserterCardCache.be, stockerCardCache.be, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                    }
                    return true;
                }
            }
        }
        return false; //If we got NOTHING
    }
    
    public boolean canChemicalFitInTank(IChemicalHandler stockerTank, ChemicalStack<?> chemicalStack) {
        if (!isValidChemicalForHandler(stockerTank, chemicalStack)) //Don't Check disparate types
            return false;
        return (stockerTank.insertChemical(chemicalStack, Action.SIMULATE).getAmount() < chemicalStack.getAmount());
    }    
    
	/** Extractor Cards call this, and try to find an inserter card to send their items to **/
	public boolean sendChemicals(ExtractorCardCache extractorCardCache) {
		BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(extractorCardCache.direction);
        Level level = laserNodeBE.getLevel();
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> chemicalHandlerMap = getAttachedChemicalTanks(extractorCardCache.direction, extractorCardCache.sneaky);
        if (chemicalHandlerMap == null || chemicalHandlerMap.isEmpty()) return false;
        
        for (Entry<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> entry : chemicalHandlerMap.entrySet()) {
        	if (!entry.getValue().isPresent())
        		continue;
        	
        	IChemicalHandler<?, ?> chemicalHandler = entry.getValue().resolve().get();
        	for (int tank = 0; tank < chemicalHandler.getTanks(); tank++) {
        		ChemicalStack<?> chemicalStack = chemicalHandler.getChemicalInTank(tank);
        		if (chemicalStack.isEmpty() || !extractorCardCache.mekanismCardCache.isStackValidForCard(chemicalStack)) 
        			continue;
        		ChemicalStack<?> extractStack = chemicalStack.copy();
        		extractStack.setAmount(extractorCardCache.extractAmt);
        		
                if (extractorCardCache.filterCard.getItem() instanceof FilterCount) { //If this is a count filter, only try to extract up to the amount in the filter
                    int filterCount = extractorCardCache.mekanismCardCache.getFilterAmt(extractStack);
                    if (filterCount <= 0) continue; //This should never happen in theory...
                    long amtInInv = chemicalStack.getAmount();
                    long amtAllowedToRemove = amtInInv - filterCount;
                    if (amtAllowedToRemove <= 0) continue;
                    long amtRemaining = Math.min(extractStack.getAmount(), amtAllowedToRemove);
                    extractStack.setAmount(amtRemaining);
                }
                
                if (extractorCardCache.exact) {
                    if (extractChemicalStackExact(extractorCardCache, chemicalHandler, extractStack, entry.getKey()))
                        return true;
                } else {
                    if (extractChemicalStack(extractorCardCache, chemicalHandler, extractStack, entry.getKey()))
                        return true;
                }
        	}
        }
        return false;
	}
        
    
    public boolean extractChemicalStack(ExtractorCardCache extractorCardCache, IChemicalHandler fromInventory, ChemicalStack<?> extractStack, ChemicalType chemicalType) {
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
        	LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache, chemicalType);
            if (laserNodeChemicalHandler == null) continue;
            IChemicalHandler handler = laserNodeChemicalHandler.handler;
            
            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.mekanismCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    ChemicalStack<?> chemicalStack = handler.getChemicalInTank(tank);
                    if (chemicalStack.isEmpty() || new ChemicalStackKey(chemicalStack).equals(new ChemicalStackKey(extractStack))) {
                        long currentAmt = chemicalStack.getAmount();
                        long neededAmt = filterCount - currentAmt;
                        if (neededAmt < extractStack.getAmount()) {
                            amtToExtract = neededAmt;
                            break;
                        }
                    }
                }
            }
            if (amtToExtract == 0) {
                amtToExtract = totalAmtNeeded;
                continue;
            }
            extractStack.setAmount(amtToExtract);
            long amtReturned = handler.insertChemical(extractStack, Action.SIMULATE).getAmount();
            if (amtReturned == amtToExtract) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            extractStack.setAmount(amtToExtract - amtReturned);
            ChemicalStack<?> drainedStack = fromInventory.extractChemical(extractStack, Action.EXECUTE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            foundAnything = true;
            handler.insertChemical(drainedStack, Action.EXECUTE);
            drawParticlesChemical(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);            totalAmtNeeded -= drainedStack.getAmount();
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return true;
        }

        return foundAnything;
    }
    
    public boolean extractChemicalStackExact(ExtractorCardCache extractorCardCache, IChemicalHandler fromInventory, ChemicalStack<?> extractStack, ChemicalType chemicalType) {
        long totalAmtNeeded = extractStack.getAmount();
        long amtToExtract = extractStack.getAmount();

        ChemicalStack<?> testDrain = fromInventory.extractChemical(extractStack, Action.SIMULATE);
        if (testDrain.getAmount() < totalAmtNeeded)
            return false; //If we don't have enough in the extractTank we can't pull out this exact amount!
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int roundRobin = -1;

        if (extractorCardCache.roundRobin != 0) {
            roundRobin = laserNodeBE.getRR(extractorCardCache);
            inserterCardCaches = laserNodeBE.applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        Map<InserterCardCache, Long> insertHandlers = new Object2LongOpenHashMap<>();

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache, chemicalType);
            if (laserNodeChemicalHandler == null) continue;
            IChemicalHandler handler = laserNodeChemicalHandler.handler;
            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.mekanismCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    ChemicalStack<?> chemicalStack = handler.getChemicalInTank(tank);
                    if (chemicalStack.isEmpty() || new ChemicalStackKey(chemicalStack).equals(new ChemicalStackKey(extractStack))) {
                        long currentAmt = chemicalStack.getAmount();
                        long neededAmt = filterCount - currentAmt;
                        if (neededAmt < totalAmtNeeded) {
                            amtToExtract = neededAmt;
                            break;
                        }
                    }
                }
            }
            if (amtToExtract == 0) {
                amtToExtract = totalAmtNeeded;
                continue;
            }
            extractStack.setAmount(amtToExtract);
            long amtReturned = handler.insertChemical(extractStack, Action.SIMULATE).getAmount();
            if (amtReturned == amtToExtract) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            extractStack.setAmount(amtToExtract - amtReturned);
            ChemicalStack<?> drainedStack = fromInventory.extractChemical(extractStack, Action.SIMULATE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            insertHandlers.put(inserterCardCache, drainedStack.getAmount()); //Add the handler to the list of handlers we found fluid in
            totalAmtNeeded -= drainedStack.getAmount(); //Keep track of how much we have left to insert
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) break;
        }

        if (totalAmtNeeded > 0) return false;

        for (Map.Entry<InserterCardCache, Long> entry : insertHandlers.entrySet()) {
            InserterCardCache inserterCardCache = entry.getKey();
            LaserNodeChemicalHandler laserNodeChemicalHandler = getLaserNodeHandlerChemical(inserterCardCache, chemicalType);
            IChemicalHandler handler = laserNodeChemicalHandler.handler;
            extractStack.setAmount(entry.getValue());
            ChemicalStack<?> drainedStack = fromInventory.extractChemical(extractStack, Action.EXECUTE);
            handler.insertChemical(drainedStack, Action.EXECUTE);
            drawParticlesChemical(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        return true;
    }
    
    /** Finds all inserters that can be extracted to **/
    public List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, ChemicalStack<?> stack) {
    	ChemicalStackKey key = new ChemicalStackKey(stack);
        if (inserterCacheChemical.containsKey(extractorCardCache)) { //If this extractor card is already in the cache
            if (inserterCacheChemical.get(extractorCardCache).containsKey(key)) //If this extractor card AND itemKey are already in the cache
                return inserterCacheChemical.get(extractorCardCache).get(key); //Return the cached results
            else { //Find the list of items that can be extracted by this extractor and cache them
                List<InserterCardCache> nodes = laserNodeBE.getInserterNodes().stream().filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.enabled)
                                && (p.mekanismCardCache.isStackValidForCard(stack))
                                && (p.cardType.equals(extractorCardCache.cardType))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                        .toList();
                inserterCacheChemical.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else { //Find the list of items that can be extracted by this extractor and cache them along with the extractor card
            List<InserterCardCache> nodes = laserNodeBE.getInserterNodes().stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.enabled)
                            && (p.mekanismCardCache.isStackValidForCard(stack))
                            && (p.cardType.equals(extractorCardCache.cardType))
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                    .toList();
            HashMap<ChemicalStackKey, List<InserterCardCache>> tempMap = new HashMap<>();
            tempMap.put(key, nodes);
            inserterCacheChemical.put(extractorCardCache, tempMap);
            return nodes;
        }
    }
    
    public LaserNodeChemicalHandler getLaserNodeHandlerChemical(InserterCardCache inserterCardCache, ChemicalType chemicalType) {
        if (!inserterCardCache.cardType.equals(BaseCard.CardType.CHEMICAL)) return null;
        Level level = laserNodeBE.getLevel();
        DimBlockPos nodeWorldPos = new DimBlockPos(inserterCardCache.relativePos.getLevel(level.getServer()), laserNodeBE.getWorldPos(inserterCardCache.relativePos.blockPos));
        if (!laserNodeBE.chunksLoaded(nodeWorldPos, nodeWorldPos.blockPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = laserNodeBE.getNodeAt(new DimBlockPos(inserterCardCache.relativePos.getLevel(level.getServer()), laserNodeBE.getWorldPos(inserterCardCache.relativePos.blockPos)));
        if (be == null) return null;
        Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> chemicalHandlerMap = be.mekanismCache.getAttachedChemicalTanks(inserterCardCache.direction, inserterCardCache.sneaky);
        if (chemicalHandlerMap == null || chemicalHandlerMap.isEmpty()) return null;
        if (!chemicalHandlerMap.containsKey(chemicalType)) return null;
        
        if (!chemicalHandlerMap.get(chemicalType).isPresent()) return null;
        IChemicalHandler<?, ?> chemicalHandler = chemicalHandlerMap.get(chemicalType).resolve().get();
        if (chemicalHandler.getTanks() == 0) return null;
        return new LaserNodeChemicalHandler(be, chemicalHandler);
    }
    
    private void addChemicalHandlerToMapGeneric(Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> map, SideConnection sideConnection, BlockEntity be, Direction inventorySide, ChemicalType chemicalType) {
    	@SuppressWarnings("unchecked")
		LazyOptional<IChemicalHandler<?, ?>> chemicalHandlerOptional = (LazyOptional<IChemicalHandler<?, ?>>) be.getCapability(MekanismStatics.getCapabilityForChemical(chemicalType), inventorySide);
    	
        if (chemicalHandlerOptional.isPresent()) {
            // add the invalidator
        	if (sideConnection != null)
        		chemicalHandlerOptional.addListener(getInvalidatorChemical(sideConnection, chemicalType));
            // cache and return
        	map.put(chemicalType, chemicalHandlerOptional);
        }
    }
    
    private void addChemicalHandlerToMap(SideConnection sideConnection, BlockEntity be, Direction inventorySide, ChemicalType chemicalType) {
    	addChemicalHandlerToMapGeneric(facingHandlerChemical.get(sideConnection), sideConnection, be, inventorySide, chemicalType);
    }
    
    private void addChemicalHandlerToMapNoCache(Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> map, BlockEntity be, Direction inventorySide, ChemicalType chemicalType) {
    	addChemicalHandlerToMapGeneric(map, null, be, inventorySide, chemicalType);
    }
    
    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> getAttachedChemicalTanks(Direction direction, Byte sneakySide) {
    	Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        
        Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> testHandlerMap = facingHandlerChemical.get(sideConnection);
        if (testHandlerMap != null && !testHandlerMap.isEmpty())
        	return testHandlerMap;

        Level level = laserNodeBE.getLevel();
        assert level != null;
        BlockEntity be = level.getBlockEntity(laserNodeBE.getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            if (testHandlerMap == null)
            	facingHandlerChemical.put(sideConnection, new HashMap<>());
            
            for (ChemicalType chemicalType : ChemicalType.values())
	            addChemicalHandlerToMap(sideConnection, be, inventorySide, chemicalType);
            
            if (!facingHandlerChemical.get(sideConnection).isEmpty())
            	return facingHandlerChemical.get(sideConnection);
        }
        
        // no item handler, cache empty
        facingHandlerChemical.remove(sideConnection);
        return null;
    }
    
    public Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> getAttachedChemicalTanksNoCache(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        Level level = laserNodeBE.getLevel();
        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(laserNodeBE.getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
        	Map<ChemicalType, LazyOptional<IChemicalHandler<?, ?>>> returnMap = new HashMap<>();
        	
        	for (ChemicalType chemicalType : ChemicalType.values())
        		addChemicalHandlerToMapNoCache(returnMap, be, inventorySide, chemicalType);
        	
        	if (!returnMap.isEmpty())
        		return returnMap;
        }
        
        return null;
    }
    
    private NonNullConsumer<LazyOptional<IChemicalHandler<?, ?>>> getInvalidatorChemical(SideConnection sideConnection, ChemicalType chemicalType) {
    	if (connectionInvalidatorChemical.get(sideConnection) == null)
    		connectionInvalidatorChemical.put(sideConnection, new HashMap<ChemicalType, NonNullConsumer<LazyOptional<IChemicalHandler<?, ?>>>> ());
    	
    	return connectionInvalidatorChemical.get(sideConnection).computeIfAbsent(chemicalType, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
        	if (te.facingHandlerChemical.get(sideConnection) != null) {
	            if (te.facingHandlerChemical.get(sideConnection).get(chemicalType) == handler) {     
	                laserNodeBE.clearCachedInventories(sideConnection, chemicalType);
	            }
    		}
        }));
    }
    
    public void drawParticlesChemical(ChemicalStack<?> chemicalStack, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
    	ServerTickHandler.addToListChemical(new ParticleDataChemical(chemicalStack, new DimBlockPos(sourceBE.getLevel(), sourceBE.getBlockPos()), (byte) fromDirection.ordinal(), new DimBlockPos(destinationBE.getLevel(), destinationBE.getBlockPos()), (byte) destinationDirection.ordinal(), (byte) extractPosition, (byte) insertPosition));
    }
    
    public void drawParticlesClient(ParticleRenderDataChemical partData) {
        //if (particlesDrawnThisTick > 64) return;
        Level level = laserNodeBE.getLevel();
        ClientLevel clientLevel = (ClientLevel) level;
        ChemicalStack<?> chemicalStack = partData.chemicalStack;
        if (chemicalStack.isEmpty()) return; //I managed to crash without this, so added it :)
        BlockPos toPos = partData.toPos;
        BlockPos fromPos = partData.fromPos;
        Direction direction = Direction.values()[partData.direction];
        BlockState targetState = level.getBlockState(toPos);
        float randomSpread = 0.01f;
        int min = 100;
        int max = 8000;
        int minPart = 8;
        int maxPart = 64;
        long count = ((maxPart - minPart) * (chemicalStack.getAmount() - min)) / (max - min) + minPart;

        if (targetState.getBlock() instanceof LaserNode) {
            targetState = level.getBlockState(fromPos);
            VoxelShape voxelShape = targetState.getShape(level, toPos);
            Vector3f extractOffset = findOffset(direction, partData.position, offsets);
            Vector3f insertOffset = CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState);
            ChemicalFlowParticleData data = new ChemicalFlowParticleData(chemicalStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10, chemicalStack.getType().toString());
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, toPos.getX() + insertOffset.x() + d1, toPos.getY() + insertOffset.y() + d3, toPos.getZ() + insertOffset.z() + d5, 0, 0, 0);
            }
        } else {
            VoxelShape voxelShape = targetState.getShape(level, toPos);
            Vector3f extractOffset = findOffset(direction, partData.position, offsets);
            Vector3f insertOffset = CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState);
            ChemicalFlowParticleData data = new ChemicalFlowParticleData(chemicalStack, fromPos.getX() + insertOffset.x(), fromPos.getY() + insertOffset.y(), fromPos.getZ() + insertOffset.z(), 10, chemicalStack.getType().toString());
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
            }
        }
    }
}