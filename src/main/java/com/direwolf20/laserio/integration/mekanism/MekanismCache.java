package com.direwolf20.laserio.integration.mekanism;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ChemicalFlowParticleData;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleDataChemical;
import com.direwolf20.laserio.integration.mekanism.client.chemicalparticle.ParticleRenderDataChemical;
import com.direwolf20.laserio.util.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender.offsets;
import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class MekanismCache {

    public final Map<LaserNodeBE.SideConnection, BlockCapabilityCache<IChemicalHandler, Direction>> facingHandlerChemical = new HashMap<>();
    public final Map<ExtractorCardCache, Map<Chemical, List<InserterCardCache>>> inserterCacheChemical = new HashMap<>();

    private final LaserNodeBE laserNodeBE;
    private final Random random = new Random();

    public MekanismCache(LaserNodeBE laserNodeBE) {
        this.laserNodeBE = laserNodeBE;
    }

    public boolean senseChemicals(SensorCardCache sensorCardCache) {
        Level level = laserNodeBE.getLevel();
        BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(sensorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        NodeSideCache nodeSideCache = laserNodeBE.nodeSideCaches[sensorCardCache.direction.ordinal()];
        IChemicalHandler chemicalHandler = getAttachedChemicalTanks(sensorCardCache.direction, sensorCardCache.sneaky);
        if (chemicalHandler == null) {
            if (laserNodeBE.updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                laserNodeBE.rendersChecked = false;
                laserNodeBE.clearCachedInventories();
                laserNodeBE.redstoneChecked = false;
            }
            return false;
        }

        ItemStack filter = sensorCardCache.filterCard;
        boolean andMode = BaseCard.getAnd(sensorCardCache.cardItem);
        boolean filterMatched = false;

        if (filter.isEmpty()) { //Needs a filter
            if (laserNodeBE.updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                laserNodeBE.rendersChecked = false;
                laserNodeBE.clearCachedInventories();
                laserNodeBE.redstoneChecked = false;
            }
            return false;
        }
        if (filter.getItem() instanceof FilterBasic) {
            List<ChemicalStack> filteredChemicals = sensorCardCache.mekanismCardCache.getFilteredChemicals();

            outloop:
            for (Iterator<ChemicalStack> iter = filteredChemicals.iterator(); iter.hasNext(); ) {
                ChemicalStack chemicalStack = iter.next();
                for (int tank = 0; tank < chemicalHandler.getChemicalTanks(); tank++) { //Loop through all the tanks
                    ChemicalStack stackInTank = chemicalHandler.getChemicalInTank(tank);
                    if (chemicalStack.getChemical() == stackInTank.getChemical()) {
                        iter.remove();
                        if (!andMode) {
                            filterMatched = true;
                            break outloop;
                        }
                    }
                }
            }

            if (andMode)
                filterMatched = filteredChemicals.isEmpty();
        } else if (filter.getItem() instanceof FilterCount) {
            List<ChemicalStack> filteredChemicals = sensorCardCache.mekanismCardCache.getFilteredChemicals();

            outloop:
            for (Iterator<ChemicalStack> iter = filteredChemicals.iterator(); iter.hasNext(); ) {
                ChemicalStack chemicalStack = iter.next();
                int desiredAmt = sensorCardCache.mekanismCardCache.getFilterAmt(chemicalStack);
                for (int tank = 0; tank < chemicalHandler.getChemicalTanks(); tank++) { //Loop through all the tanks
                    ChemicalStack stackInTank = chemicalHandler.getChemicalInTank(tank);
                    if (chemicalStack.getChemical() == stackInTank.getChemical()) {
                        long amtHad = stackInTank.getAmount();
                        if (amtHad >= desiredAmt && (!sensorCardCache.exact || amtHad <= desiredAmt)) {
                            iter.remove();
                            if (!andMode) {
                                filterMatched = true;
                                break outloop;
                            }
                        }
                    }
                }
            }
            if (andMode)
                filterMatched = filteredChemicals.isEmpty();
        } else if (filter.getItem() instanceof FilterTag) {
            List<String> tags = sensorCardCache.getFilterTags();

            outloop:
            for (int tank = 0; tank < chemicalHandler.getChemicalTanks(); tank++) { //Loop through all the tanks
                ChemicalStack stackInTank = chemicalHandler.getChemicalInTank(tank);
                for (TagKey<?> tagKey : stackInTank.getChemical().getTags().toList()) {
                    String chemicalTag = tagKey.location().toString().toLowerCase(Locale.ROOT);
                    if (tags.contains(chemicalTag)) {
                        tags.remove(chemicalTag);
                        if (!andMode) {
                            filterMatched = true;
                            break outloop;
                        }
                    }
                }
            }

            //In and mode, the list of tags needs to be empty, in or mode it just has to be 1 smaller.
            if (andMode)
                filterMatched = tags.isEmpty();
        }
        if (laserNodeBE.updateRedstoneFromSensor(filterMatched, sensorCardCache.redstoneChannel, nodeSideCache)) {
            //System.out.println("Redstone network change detected");
            laserNodeBE.rendersChecked = false;
            laserNodeBE.clearCachedInventories();
            laserNodeBE.redstoneChecked = false;
        }
        return true;
    }

    public boolean stockChemicals(StockerCardCache stockerCardCache) {
        ItemStack filter = stockerCardCache.filterCard;
        if (filter.isEmpty() || !stockerCardCache.isAllowList) { //Needs a filter - at least for now? Also must be in whitelist mode
            return false;
        }
        Level level = laserNodeBE.getLevel();
        BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(stockerCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        IChemicalHandler chemicalHandler = getAttachedChemicalTanks(stockerCardCache.direction, stockerCardCache.sneaky);
        if (chemicalHandler == null) return false;

        if (filter.getItem() instanceof FilterBasic || filter.getItem() instanceof FilterCount) {
            if (stockerCardCache.regulate && filter.getItem() instanceof FilterCount) {
                if (regulateChemicalStocker(stockerCardCache, chemicalHandler))
                    return true;
            }
            if (!canAnyChemicalFiltersFit(chemicalHandler, stockerCardCache)) {
                return false; //If we can't fit any of our filtered items into this inventory, don't bother scanning for them
            }
            boolean foundItems = findChemicalStackForStocker(stockerCardCache, chemicalHandler); //Start looking for this item
            if (foundItems)
                return true;

        } else if (filter.getItem() instanceof FilterTag) {
            //No-Op
        }

        return false;
    }

    private boolean canAnyChemicalFiltersFit(IChemicalHandler chemicalHandler, StockerCardCache stockerCardCache) {
        for (ChemicalStack chemicalStack : stockerCardCache.mekanismCardCache.getFilteredChemicals()) {
            long amtReturned = chemicalHandler.insertChemical(chemicalStack, Action.SIMULATE).getAmount();
            if (amtReturned < chemicalStack.getAmount()) //If any fit
                return true;
        }
        return false;
    }

    private boolean canChemicalFitInTank(IChemicalHandler stockerTank, ChemicalStack chemicalStack) {
        return (stockerTank.insertChemical(chemicalStack, Action.SIMULATE).getAmount() < chemicalStack.getAmount());
    }

    private boolean findChemicalStackForStocker(StockerCardCache stockerCardCache, IChemicalHandler stockerTank) {
        boolean isCount = stockerCardCache.filterCard.getItem() instanceof FilterCount;
        int extractAmt = stockerCardCache.extractAmt;

        List<ChemicalStack> filteredChemicalsList = new CopyOnWriteArrayList<>(stockerCardCache.mekanismCardCache.getFilteredChemicals());
        filteredChemicalsList.removeIf(chemicalStack -> !canChemicalFitInTank(stockerTank, chemicalStack));//If this fluid can't fit in this tank at all, skip the fluid
        if (filteredChemicalsList.isEmpty()) //If nothing in the filter can fit, return false
            return false;

        if (isCount) { //If this is a filter count, prune the list of items to search for to just what we need
            for (ChemicalStack chemicalStack : filteredChemicalsList) { //Remove all the items from the list that we already have enough of
                for (int tank = 0; tank < stockerTank.getChemicalTanks(); tank++) {
                    ChemicalStack tankStack = stockerTank.getChemicalInTank(tank);
                    if (tankStack.isEmpty() || chemicalStack.is(tankStack.getChemical())) {
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
        for (ChemicalStack chemicalStack : filteredChemicalsList) {
            Map<InserterCardCache, ChemicalStack> insertHandlers = new HashMap<>();
            if (!isCount)
                chemicalStack.setAmount(extractAmt); //If this isn't a counting card, we want the extractAmt value
            long amtNeeded = chemicalStack.getAmount();

            for (InserterCardCache inserterCardCache : laserNodeBE.getChannelMatchInserters(stockerCardCache)) { //Iterate through ALL inserter nodes on this channel only
                if (!inserterCardCache.mekanismCardCache.isStackValidForCard(chemicalStack))
                    continue;
                IChemicalHandler handler = getLaserNodeHandlerChemical(inserterCardCache);
                if (handler == null) continue;
                chemicalStack.setAmount(amtNeeded);
                ChemicalStack extractStack = handler.extractChemical(chemicalStack, Action.SIMULATE);
                if (extractStack.isEmpty()) continue;
                insertHandlers.put(inserterCardCache, extractStack);
                amtNeeded -= extractStack.getAmount();
                if (amtNeeded == 0) break;
            }
            if (!insertHandlers.isEmpty()) {
                if (!stockerCardCache.exact || amtNeeded == 0) { //If its not exact mode, or it is exact mode, and we found all we need to satisfy this
                    for (Map.Entry<InserterCardCache, ChemicalStack> entry : insertHandlers.entrySet()) { //Do all the extracts/inserts
                        InserterCardCache inserterCardCache = entry.getKey();
                        ChemicalStack insertStack = entry.getValue();
                        long amtReturned = stockerTank.insertChemical(insertStack, Action.SIMULATE).getAmount(); //Test inserting into the target
                        insertStack.setAmount(insertStack.getAmount() - amtReturned); //Change the stack to size to how much can fit
                        IChemicalHandler handler = getLaserNodeHandlerChemical(inserterCardCache);
                        if (handler == null) continue; //Should Never Happen because it was tested above?
                        ChemicalStack drainedStack = handler.extractChemical(insertStack, Action.EXECUTE);
                        stockerTank.insertChemical(drainedStack, Action.EXECUTE);
                        drawParticlesChemical(drainedStack, inserterCardCache.direction, inserterCardCache.be, stockerCardCache.be, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                    }
                    return true;
                }
            }
        }
        return false; //If we got NOTHING
    }

    private boolean regulateChemicalStocker(StockerCardCache stockerCardCache, IChemicalHandler stockerTank) {
        List<ChemicalStack> filteredChemicalsList = stockerCardCache.mekanismCardCache.getFilteredChemicals();
        for (ChemicalStack chemicalStack : filteredChemicalsList) { //Iterate the list of filtered items for extracting purposes
            int desiredAmt = stockerCardCache.mekanismCardCache.getFilterAmt(chemicalStack);
            long amtHad = 0;
            for (int tank = 0; tank < stockerTank.getChemicalTanks(); tank++) { //Loop through all the tanks
                ChemicalStack stackInTank = stockerTank.getChemicalInTank(tank);
                if (chemicalStack.is(stackInTank.getChemical()))
                    amtHad += stackInTank.getAmount();
            }
            if (amtHad > desiredAmt) { //If we have too much of this fluid, remove the difference.
                chemicalStack.setAmount(Math.min(amtHad - desiredAmt, stockerCardCache.extractAmt));
                if (extractChemicalStack(stockerCardCache, stockerTank, chemicalStack))
                    return true;
            }
        }
        return false;
    }

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendChemicals(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = laserNodeBE.getBlockPos().relative(extractorCardCache.direction);
        Level level = laserNodeBE.getLevel();
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        if (extractChemicalStack(extractorCardCache, getAttachedChemicalTanks(extractorCardCache.direction, extractorCardCache.sneaky))) {
            return true;
        }

        return false;
    }

    private boolean extractChemicalStack(ExtractorCardCache extractorCardCache, @Nullable IChemicalHandler chemicalHandler) {
        if (chemicalHandler == null) return false;
        for (int tank = 0; tank < chemicalHandler.getChemicalTanks(); tank++) {
            ChemicalStack chemicalStack = chemicalHandler.getChemicalInTank(tank);
            if (chemicalStack.isEmpty() || !extractorCardCache.mekanismCardCache.isStackValidForCard(chemicalStack))
                continue;
            ChemicalStack extractStack = chemicalStack.copy();
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
                if (extractChemicalStackExact(extractorCardCache, chemicalHandler, extractStack))
                    return true;
            } else if (extractChemicalStack(extractorCardCache, chemicalHandler, extractStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean extractChemicalStack(ExtractorCardCache extractorCardCache, IChemicalHandler fromInventory, ChemicalStack extractStack) {
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
            IChemicalHandler handler = getLaserNodeHandlerChemical(inserterCardCache);
            if (handler == null) continue;

            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.mekanismCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                    if (chemicalStack.isEmpty() || chemicalStack.is(extractStack.getChemical())) {
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
            ChemicalStack drainedStack = fromInventory.extractChemical(extractStack, Action.EXECUTE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            foundAnything = true;
            handler.insertChemical(drainedStack, Action.EXECUTE);
            drawParticlesChemical(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
            totalAmtNeeded -= drainedStack.getAmount();
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) laserNodeBE.getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return true;
        }

        return foundAnything;
    }

    private boolean extractChemicalStackExact(ExtractorCardCache extractorCardCache, IChemicalHandler fromInventory, ChemicalStack extractStack) {
        long totalAmtNeeded = extractStack.getAmount();
        long amtToExtract = extractStack.getAmount();

        ChemicalStack testDrain = fromInventory.extractChemical(extractStack, Action.SIMULATE);
        if (testDrain.getAmount() < totalAmtNeeded)
            return false; //If we don't have enough in the extractTank we can't pull out this exact amount!
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);

        if (extractorCardCache.roundRobin != 0) {
            int roundRobin = laserNodeBE.getRR(extractorCardCache);
            inserterCardCaches = laserNodeBE.applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        Map<InserterCardCache, Long> insertHandlers = new Object2LongOpenHashMap<>();

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            IChemicalHandler handler = getLaserNodeHandlerChemical(inserterCardCache);
            if (handler == null) continue;
            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.mekanismCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    ChemicalStack chemicalStack = handler.getChemicalInTank(tank);
                    if (chemicalStack.isEmpty() || chemicalStack.is(extractStack.getChemical())) {
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
            ChemicalStack drainedStack = fromInventory.extractChemical(extractStack, Action.SIMULATE);
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
            extractStack.setAmount(entry.getValue());
            IChemicalHandler handler = getLaserNodeHandlerChemical(inserterCardCache);
            if (handler == null) continue; //Should never happen because tested above?
            ChemicalStack drainedStack = fromInventory.extractChemical(extractStack, Action.EXECUTE);
            handler.insertChemical(drainedStack, Action.EXECUTE);
            drawParticlesChemical(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        return true;
    }

    /** Finds all inserters that can be extracted to **/
    private List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, ChemicalStack stack) {
        return inserterCacheChemical.computeIfAbsent(extractorCardCache, cache -> new Reference2ObjectOpenHashMap<>())
                .computeIfAbsent(stack.getChemical(), k -> laserNodeBE.getInserterNodes().stream()
                        .filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.cardType == extractorCardCache.cardType)
                                && (p.enabled)
                                && (p.mekanismCardCache.isStackValidForCard(stack))
                                && (!(p.relativePos.pos().equals(BlockPos.ZERO) && p.direction == extractorCardCache.direction)))
                        .toList()
                );
    }

    @Nullable
    private IChemicalHandler getLaserNodeHandlerChemical(InserterCardCache inserterCardCache) {
        LaserNodeBE be = laserNodeBE.getLaserNodeBE(inserterCardCache, BaseCard.CardType.CHEMICAL);
        if (be == null) return null;

        IChemicalHandler chemicalHandler = be.mekanismCache.getAttachedChemicalTanks(inserterCardCache.direction, inserterCardCache.sneaky);

        if (chemicalHandler == null || chemicalHandler.getChemicalTanks() == 0) return null;
        return chemicalHandler;
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    private IChemicalHandler getAttachedChemicalTanks(Direction direction, Byte sneakySide) {
        Direction inventorySide;
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        else
            inventorySide = direction.getOpposite();
        LaserNodeBE.SideConnection sideConnection = new LaserNodeBE.SideConnection(direction, inventorySide);
        Level level = laserNodeBE.getLevel();

        assert level != null;
        BlockPos targetPos = laserNodeBE.getBlockPos().relative(direction);
        if (facingHandlerChemical.get(sideConnection) == null)
            facingHandlerChemical.put(sideConnection, BlockCapabilityCache.create(
                    MekanismStatics.getCapabilityForChemical(), // capability to cache
                    (ServerLevel) level, // level
                    targetPos, // target position
                    inventorySide // context (The side of the block we're trying to pull/push from?)
            ));

        return facingHandlerChemical.get(sideConnection).getCapability();
    }

    public IChemicalHandler getAttachedChemicalTanksNoCache(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        Level level = laserNodeBE.getLevel();
        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(laserNodeBE.getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            BlockPos relativePos = laserNodeBE.getBlockPos().relative(direction);
            IChemicalHandler handler = level.getCapability(MekanismStatics.getCapabilityForChemical(), relativePos, inventorySide);
            if (handler != null) return handler;
        }
        return null;
    }

    public void drawParticlesChemical(ChemicalStack chemicalStack, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
        if (!sourceBE.getShowParticles() || !destinationBE.getShowParticles()) return;
        ServerTickHandler.addToListFluid(new ParticleDataChemical(chemicalStack, GlobalPos.of(sourceBE.getLevel().dimension(), sourceBE.getBlockPos()), (byte) fromDirection.ordinal(), GlobalPos.of(destinationBE.getLevel().dimension(), destinationBE.getBlockPos()), (byte) destinationDirection.ordinal(), (byte) extractPosition, (byte) insertPosition));
    }

    public void drawParticlesClient(ParticleRenderDataChemical partData) {
        //if (particlesDrawnThisTick > 64) return;
        Level level = laserNodeBE.getLevel();
        ClientLevel clientLevel = (ClientLevel) level;
        ChemicalStack chemicalStack = partData.chemicalStack;
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
            VoxelShape voxelShape = targetState.getShape(level, fromPos);
            Vector3f extractOffset = findOffset(direction, partData.position, offsets);
            Vector3f insertOffset = CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState);
            ChemicalFlowParticleData data = new ChemicalFlowParticleData(chemicalStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
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
            Vec3 insertOffset = new Vec3(CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState));
            ChemicalFlowParticleData data = new ChemicalFlowParticleData(chemicalStack, insertOffset.add(Vec3.atLowerCornerOf(fromPos)), 10);
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
