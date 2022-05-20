package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.*;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class LaserNodeBE extends BaseLaserBE {
    private static final Vector3f[] offsets = { //Used for where to draw particles from
            new Vector3f(0.65f, 0.65f, 0.5f),
            new Vector3f(0.5f, 0.65f, 0.5f),
            new Vector3f(0.35f, 0.65f, 0.5f),
            new Vector3f(0.65f, 0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(0.35f, 0.5f, 0.5f),
            new Vector3f(0.65f, 0.35f, 0.5f),
            new Vector3f(0.5f, 0.35f, 0.5f),
            new Vector3f(0.35f, 0.35f, 0.5f)
    };
    /** A cache of this blocks sides - data we need to reference frequently **/
    private final NodeSideCache[] nodeSideCaches = new NodeSideCache[6];
    private final IItemHandler EMPTY = new ItemStackHandler(0);

    /** Adjacent Inventory Handlers **/
    private record SideConnection(Direction nodeSide, Direction sneakySide) {
    }

    /** BE and ItemHandler used for checking if a note/container is valid **/
    private record LaserNodeHandler(LaserNodeBE be, IItemHandler handler) {

    }

    public Map<ExtractorCardCache, Integer> roundRobinMap = new Object2IntOpenHashMap<>();

    private Map<SideConnection, LazyOptional<IItemHandler>> facingHandler = new HashMap<>();
    private final Map<SideConnection, NonNullConsumer<LazyOptional<IItemHandler>>> connectionInvalidator = new HashMap<>();

    /** Variables for tracking and sending items/filters/etc **/
    private Set<BlockPos> otherNodesInNetwork = new HashSet<>();
    private final List<InserterCardCache> inserterNodes = new CopyOnWriteArrayList<>(); //All Inventory nodes that contain an inserter card
    private final HashMap<ExtractorCardCache, HashMap<ItemStackKey, List<InserterCardCache>>> inserterCache = new HashMap<>();
    private final HashMap<ExtractorCardCache, List<InserterCardCache>> channelOnlyCache = new HashMap<>();
    private List<ParticleRenderData> particleRenderData = new ArrayList<>();
    private Random random = new Random();

    private record StockerRequest(StockerCardCache stockerCardCache, ItemStackKey itemStackKey) {
    }

    private record StockerSource(InserterCardCache inserterCardCache, int slot) {
    }

    private final Map<StockerRequest, StockerSource> stockerDestinationCache = new HashMap<>();

    /** Misc Variables **/
    private boolean discoveredNodes = false; //The first time this block entity loads, it'll run discovery to refresh itself

    public LaserNodeBE(BlockPos pos, BlockState state) {
        super(Registration.LaserNode_BE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            final int j = direction.ordinal();
            LaserNodeItemHandler tempHandler = new LaserNodeItemHandler(LaserNodeContainer.SLOTS, this);
            nodeSideCaches[j] = new NodeSideCache(tempHandler, LazyOptional.of(() -> tempHandler), 0);
        }
    }

    /** This is called by nodes when a connection is added/removed - the other node does the discovery and then tells this one about it **/
    public void setOtherNodesInNetwork(Set<BlockPos> otherNodesInNetwork) {
        this.otherNodesInNetwork.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            this.otherNodesInNetwork.add(getRelativePos(pos));
        }
        refreshAllInvNodes(); //Seeing as the otherNodes list just got updated, we should refresh the InventoryNode content caches
    }

    public void updateOverclockers() {
        for (Direction direction : Direction.values()) {
            int slot = 9; //The Overclockers Slot
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            ItemStack overclockerStack = nodeSideCache.itemHandler.getStackInSlot(slot);
            if (overclockerStack.isEmpty())
                nodeSideCache.overClocker = 0;
            if (overclockerStack.getItem() instanceof OverclockerNode) {
                nodeSideCache.overClocker = overclockerStack.getCount();
            }
        }
    }

    /** Build a list of stocker cards this node has in it, for looping through **/
    public void findMyStockers() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            nodeSideCache.stockerCardCaches.clear();
            this.stockerDestinationCache.clear();
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        nodeSideCache.stockerCardCaches.add(new StockerCardCache(direction, card, slot, this));
                    }
                }
            }
        }
    }

    /** Build a list of extractor cards this node has in it, for looping through **/
    public void findMyExtractors() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            nodeSideCache.extractorCardCaches.clear();
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        nodeSideCache.extractorCardCaches.add(new ExtractorCardCache(direction, card, slot, this));
                    }
                }
            }
        }
    }

    /** Loop through all the extractorCards/stockerCards and run the extractions **/
    public void extractItems() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            int countCardsHandled = 0;
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                if (extractorCardCache.decrementSleep() == 0) {
                    if (countCardsHandled > nodeSideCache.overClocker) return;
                    if (sendItems(extractorCardCache))
                        countCardsHandled++;
                }
            }
            for (StockerCardCache stockerCardCache : nodeSideCache.stockerCardCaches) {
                if (stockerCardCache.decrementSleep() == 0) {
                    if (countCardsHandled > nodeSideCache.overClocker) return;
                    if (stockItems(stockerCardCache))
                        countCardsHandled++;
                }
            }
        }
    }

    public void tickClient() {
        drawParticlesClient();
        particleRenderData.clear();
    }

    public void tickServer() {
        if (!discoveredNodes) { //On world / chunk reload, lets rediscover the network, including this block's extractor cards.
            discoverAllNodes();
            findMyExtractors();
            findMyStockers();
            updateOverclockers();
            loadRoundRobin();
            discoveredNodes = true;
        }
        extractItems(); //If this node has any extractors, do stuff with them
    }

    public void sortInserters() {
        this.inserterNodes.sort(Comparator.comparingDouble(InserterCardCache::getDistance));
        this.inserterNodes.sort(Comparator.comparingInt(InserterCardCache::getPriority).reversed());
    }

    /** Finds all inserters that can be extracted to **/
    public List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, ItemStack stack) {
        ItemStackKey key = new ItemStackKey(stack, true);
        if (inserterCache.containsKey(extractorCardCache)) { //If this extractor card is already in the cache
            if (inserterCache.get(extractorCardCache).containsKey(key)) //If this extractor card AND itemKey are already in the cache
                return inserterCache.get(extractorCardCache).get(key); //Return the cached results
            else { //Find the list of items that can be extracted by this extractor and cache them
                List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.isStackValidForCard(stack))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                        .toList();
                inserterCache.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else { //Find the list of items that can be extracted by this extractor and cache them along with the extractor card
            List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.isStackValidForCard(stack))
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                    .toList();
            HashMap<ItemStackKey, List<InserterCardCache>> tempMap = new HashMap<>();
            tempMap.put(key, nodes);
            inserterCache.put(extractorCardCache, tempMap);
            return nodes;
        }
    }

    /** Finds all inserters that match the channel (Used for stockers) **/
    public List<InserterCardCache> getChannelMatchInserters(ExtractorCardCache extractorCardCache) {
        if (channelOnlyCache.containsKey(extractorCardCache)) {
            return channelOnlyCache.get(extractorCardCache);
        } else {
            List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                    .toList();
            channelOnlyCache.put(extractorCardCache, nodes);
            return nodes;
        }
    }

    public boolean chunksLoaded(BlockPos nodePos, BlockPos destinationPos) {
        assert level != null;
        if (!level.isLoaded(nodePos)) {
            return false;
        }
        if (!level.isLoaded(destinationPos)) {
            return false;
        }
        return true;
    }

    public LaserNodeHandler getLaserNodeHandler(InserterCardCache inserterCardCache) {
        BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
        if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
        if (be == null) return null;
        IItemHandler handler = be.getAttachedInventory(inserterCardCache.direction, inserterCardCache.sneaky).orElse(EMPTY);
        if (handler.getSlots() == 0) return null;
        return new LaserNodeHandler(be, handler);
    }

    public int getNextRR(ExtractorCardCache extractorCardCache, List<InserterCardCache> inserterCardCaches) {
        if (roundRobinMap.containsKey(extractorCardCache)) {
            int currentRR = roundRobinMap.get(extractorCardCache);
            int nextRR = currentRR + 1 >= inserterCardCaches.size() ? 0 : currentRR + 1;
            return currentRR;
        } else {
            roundRobinMap.put(extractorCardCache, 0);
            return 0;
        }
    }

    public boolean extractForExact(ExtractorCardCache extractorCardCache, IItemHandler fromInventory, ItemStack extractStack) {
        TransferResult extractResults = (ItemHandlerUtil.extractItemWithSlots(this, fromInventory, extractStack, extractStack.getCount(), true, extractorCardCache.isCompareNBT, extractorCardCache)); //Fake Extract
        int amtNeeded = extractStack.getCount();
        if (extractResults.getTotalItemCounts() != amtNeeded) //Return if we didn't get what we needed
            return false;

        TransferResult insertResults = new TransferResult();

        //Begin test inserting into inserters
        int amtStillNeeded = amtNeeded;
        for (InserterCardCache inserterCardCache : getPossibleInserters(extractorCardCache, extractStack)) {
            LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
            if (laserNodeHandler == null) continue;

            TransferResult thisResult = ItemHandlerUtil.insertItemWithSlots(laserNodeHandler.be, laserNodeHandler.handler, extractStack, 0, true, extractorCardCache.isCompareNBT, true, inserterCardCache); //Test!!
            insertResults.addResult(thisResult);

            insertResults.remainingStack = ItemStack.EMPTY; //We don't really care about this
            int amtFit = thisResult.getTotalItemCounts(); //How many items fit (Above)
            amtStillNeeded -= amtFit;
            if (amtStillNeeded == 0)
                break;
            extractStack.setCount(amtStillNeeded); //Modify the stack size rather than .copy
        }

        if (amtStillNeeded != 0) return false;
        //If we get to this point, it means we can insert all the itemstacks we wanted to, so lets do it for realsies
        extractStack.setCount(amtNeeded); //Set back to how many we actually need
        for (TransferResult.Result result : insertResults.results) {
            ItemStack tempStack = extractStack.split(result.itemStack.getCount());
            result.insertHandler.insertItem(result.insertSlot, tempStack, false);
            if (result.inserterCardCache != null)
                drawParticles(tempStack, extractorCardCache.direction, this, result.toBE, result.inserterCardCache.direction, extractorCardCache.cardSlot, result.inserterCardCache.cardSlot);
        }

        for (TransferResult.Result result : extractResults.results) {
            result.extractHandler.extractItem(result.extractSlot, result.itemStack.getCount(), false);
        }

        return true;
    }

    public boolean extractItemStack(ExtractorCardCache extractorCardCache, IItemHandler fromInventory, ItemStack extractStack) {
        TransferResult transferResults = new TransferResult();
        int amtToExtract = extractStack.getCount();
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int nextRR = -1;
        boolean foundAnything = false;

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
            if (laserNodeHandler == null) continue;

            TransferResult insertResults = ItemHandlerUtil.insertItemWithSlots(laserNodeHandler.be, laserNodeHandler.handler, extractStack, 0, true, extractorCardCache.isCompareNBT, true, inserterCardCache);
            if (insertResults.results.isEmpty()) continue; //Next inserter if nothing went in
            foundAnything = true; //We know that we have SOME of this item, and SOME will fit in another chest, so SOMETHING will move!
            int amtFit = insertResults.getTotalItemCounts(); //How many items fit (Above)
            //int amtNoFit = amtToExtract - amtFit;
            extractStack.setCount(amtFit); //Make a stack of how many can fit in here without doing an itemstack.copy()
            ItemStack extractedStack = ItemHandlerUtil.extractItem(fromInventory, extractStack, false, extractorCardCache.isCompareNBT).itemStack();
            boolean chestEmpty = extractedStack.getCount() < extractStack.getCount(); //If we didn't find enough, the extract chest is empty, so don't try again later
            amtToExtract -= extractedStack.getCount(); //Reduce how many we have left to extract by the amount we got here
            extractStack.setCount(amtToExtract); //For use in the next loop -- How many items are still needed
            for (TransferResult.Result result : insertResults.results) {
                int insertAmt = Math.min(result.itemStack.getCount(), extractedStack.getCount());
                ItemStack insertStack = extractedStack.split(insertAmt);
                laserNodeHandler.handler.insertItem(result.insertSlot, insertStack, false);
                drawParticles(insertStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
            }
            //extractStack.setCount(amtNoFit);
            if (chestEmpty || extractStack.isEmpty()) //If the chest is empty, or we have no more items to find, we're done here
                break;
        }

        return foundAnything;
    }

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendItems(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(extractorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        IItemHandler adjacentInventory = getAttachedInventory(extractorCardCache.direction, extractorCardCache.sneaky).orElse(EMPTY);
        for (int slot = 0; slot < adjacentInventory.getSlots(); slot++) {
            ItemStack stackInSlot = adjacentInventory.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || !(extractorCardCache.isStackValidForCard(stackInSlot))) continue;
            ItemStack extractStack = stackInSlot.copy();
            extractStack.setCount(extractorCardCache.extractAmt);
            if (extractorCardCache.exact) {
                if (extractForExact(extractorCardCache, adjacentInventory, extractStack))
                    return true;
            } else {
                if (extractItemStack(extractorCardCache, adjacentInventory, extractStack))
                    return true;
            }
        }
        return false;
    }

    public boolean canAnyFiltersFit(IItemHandler adjacentInventory, StockerCardCache stockerCardCache) {
        for (ItemStack stack : stockerCardCache.getFilteredItems()) {
            int amountFit = testInsertToInventory(adjacentInventory, stack.split(1)); //Try to put one in - if it fits we have room
            if (amountFit > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean regulateStocker(StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        ItemHandlerUtil.InventoryCounts stockerInventoryCount = new ItemHandlerUtil.InventoryCounts(stockerInventory, stockerCardCache.isCompareNBT);
        List<ItemStack> filteredItemsList = stockerCardCache.getFilteredItems();
        for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
            int amtHad = stockerInventoryCount.getCount(itemStack);
            if (amtHad > itemStack.getCount()) { //if we have enough, move onto the next stack after removing this one from the list
                ItemStack extractStack = itemStack.copy();
                extractStack.setCount(Math.min(amtHad - itemStack.getCount(), stockerCardCache.extractAmt));
                if (extractItemStack(stockerCardCache, stockerInventory, extractStack))
                    return true;
            }
        }
        return false;
    }

    /** Stocker Cards call this, and try to find an inserter card to pull their items from **/
    public boolean stockItems(StockerCardCache stockerCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(stockerCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        IItemHandler adjacentInventory = getAttachedInventory(stockerCardCache.direction, stockerCardCache.sneaky).orElse(EMPTY);
        ItemStack filter = stockerCardCache.filterCard;

        if (filter.isEmpty() || !stockerCardCache.isAllowList) { //Needs a filter - at least for now? Also must be in whitelist mode
            return false;
        }
        if (filter.getItem() instanceof FilterBasic || filter.getItem() instanceof FilterCount) {
            if (stockerCardCache.regulate) {
                if (regulateStocker(stockerCardCache, adjacentInventory))
                    return true;
            }
            if (!canAnyFiltersFit(adjacentInventory, stockerCardCache)) {
                return false; //If we can't fit any of our filtered items into this inventory, don't bother scanning for them
            }
            boolean foundItems = findItemStackForStocker(stockerCardCache, adjacentInventory); //Start looking for this item
            if (foundItems)
                return true;

            //If we get to this line of code, it means we found none of the filter
            //stockerCardCache.setRemainingSleep(stockerCardCache.tickSpeed * 5);
        } else if (filter.getItem() instanceof FilterTag) {

        }
        return false;
    }

    public ItemStack getStackAtStockerCachePosition(StockerSource checkSource) {
        LaserNodeHandler laserNodeHandler = getLaserNodeHandler(checkSource.inserterCardCache);
        if (laserNodeHandler == null) return ItemStack.EMPTY;
        return laserNodeHandler.handler.getStackInSlot(checkSource.slot);
    }

    /**
     * Trys to pull from the last place we found this item - checking the same slot first, then the rest of the inventory.
     * Returns the TransferResult (Simulate enabled) that we found.
     */
    public TransferResult tryStockerCacheCount(StockerCardCache stockerCardCache, ItemStack itemStack, IItemHandler stockerInventory) {
        TransferResult extractResult = new TransferResult();
        ItemStackKey itemStackKey = new ItemStackKey(itemStack, stockerCardCache.isCompareNBT);
        StockerRequest stockerRequest = new StockerRequest(stockerCardCache, itemStackKey);
        if (!stockerDestinationCache.containsKey(stockerRequest))
            return extractResult;
        int origItemsWanted = itemStack.getCount();
        int itemsStillNeeded = origItemsWanted;
        StockerSource checkSource = stockerDestinationCache.get(stockerRequest);
        ItemStack stackInSlot = getStackAtStockerCachePosition(checkSource);
        if (stackInSlot == null) //Null means the inventory no longer exists or is unloaded
            return extractResult;

        ItemStack extractedItemStack;
        LaserNodeHandler laserNodeHandler = getLaserNodeHandler(checkSource.inserterCardCache);
        if (laserNodeHandler == null) return extractResult;
        ItemStackKey stackInSlotKey = new ItemStackKey(stackInSlot, stockerCardCache.isCompareNBT);
        if (stackInSlot.isEmpty()) //Null means the inventory no longer exists or is unloaded
            stockerDestinationCache.remove(stockerRequest);
        if (stackInSlotKey.equals(itemStackKey)) {  //If the itemstack in that spot matches the itemstack we are looking for
            int extractAmt = Math.min(itemsStillNeeded, stackInSlot.getCount()); //Find out how many to extract
            extractedItemStack = laserNodeHandler.handler.extractItem(checkSource.slot, extractAmt, true); //Extract Items
            itemsStillNeeded = itemsStillNeeded - extractedItemStack.getCount();
            if (stackInSlot.getCount() - extractedItemStack.getCount() == 0) {
                stockerDestinationCache.remove(stockerRequest);
            }
            if (itemsStillNeeded == 0) {
                extractResult.addResult(new TransferResult.Result(laserNodeHandler.handler, checkSource.slot, checkSource.inserterCardCache, extractedItemStack, laserNodeHandler.be, true));
                extractResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
                return extractResult; //If we got all that we need, return the amount we got
            }
        }
        //If we got here, we have to find more of this item type, so start looking for it - note that we don't use the stack above because we will check the whole inventory and the stack above wasn't pulled out
        extractResult = ItemHandlerUtil.extractItemWithSlots(laserNodeHandler.be, laserNodeHandler.handler, itemStack, origItemsWanted, true, stockerCardCache.isCompareNBT, checkSource.inserterCardCache); //Try to pull out the items we need from this location
        extractResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
        if (!extractResult.results.isEmpty()) { //If we found something, check if the last slot we looked at is empty, and add it to the cache
            int lastSlot = extractResult.results.get(extractResult.results.size() - 1).extractSlot; //The last slot we pulled from in this inventory
            if (laserNodeHandler.handler.getStackInSlot(lastSlot).getCount() - extractResult.results.get(extractResult.results.size() - 1).itemStack.getCount() != 0) { //If its not empty now
                stockerDestinationCache.put(new StockerRequest(stockerCardCache, itemStackKey), new StockerSource(checkSource.inserterCardCache, lastSlot)); //Add to the cache
            }
        }
        return extractResult;
    }

    public boolean findItemStackForStocker(StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        boolean isCount = stockerCardCache.filterCard.getItem() instanceof FilterCount;
        int extractAmt = stockerCardCache.extractAmt;

        List<ItemStack> filteredItemsList = stockerCardCache.getFilteredItems();
        if (isCount) { //If this is a filter count, prune the list of items to search for to just what we need
            ItemHandlerUtil.InventoryCounts stockerInventoryCount = new ItemHandlerUtil.InventoryCounts(stockerInventory, stockerCardCache.isCompareNBT);
            List<ItemStack> tempList = new ArrayList<>(filteredItemsList);
            for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
                int amtHad = stockerInventoryCount.getCount(itemStack);
                if (amtHad >= itemStack.getCount()) { //if we have enough, move onto the next stack after removing this one from the list
                    tempList.remove(itemStack);
                    continue;
                }
                itemStack.setCount(Math.min(itemStack.getCount() - amtHad, extractAmt));
            }
            filteredItemsList = tempList;
        }

        if (filteredItemsList.isEmpty()) //If we have nothing left to look for! Probably only happens when its a count card.
            return false;
        Map<InserterCardCache, ItemHandlerUtil.InventoryCounts> stockerInvCaches = new HashMap<>();
        for (ItemStack itemStack : filteredItemsList) {
            if (!isCount) itemStack.setCount(extractAmt); //If this isn't a counting card, we want the extractAmt value
            int origCountNeeded = itemStack.getCount();
            TransferResult transferResult = tryStockerCacheCount(stockerCardCache, itemStack, stockerInventory);
            if (transferResult.getTotalItemCounts() == origCountNeeded) {//The item stack knows how many we need, so did we get enough?
                transferResult.doIt(); //Move the items for real - we have both extractor/inserter caches from the above method
                return true;
            }
            //If we got here, we still need (more of) this item
            itemStack.setCount(origCountNeeded - transferResult.getTotalItemCounts()); //Shrink the amount of items we want
            for (InserterCardCache inserterCardCache : getChannelMatchInserters(stockerCardCache)) { //Iterate through ALL inserter nodes on this channel only
                if (!inserterCardCache.isStackValidForCard(itemStack))
                    continue;
                if (transferResult.getTotalItemCounts() != 0 && inserterCardCache.equals(transferResult.results.get(0).extractorCardCache))  //If we found something in the cache chest, we have to skip that chest, because of the fake pullout
                    continue;

                LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
                if (laserNodeHandler == null) continue;
                ItemHandlerUtil.InventoryCounts inventoryCounts;
                if (stockerInvCaches.containsKey(inserterCardCache)) { //Count the items in the inventory once -then re-use this for future iterations
                    inventoryCounts = stockerInvCaches.get(inserterCardCache);
                } else {
                    inventoryCounts = new ItemHandlerUtil.InventoryCounts(laserNodeHandler.handler, stockerCardCache.isCompareNBT);
                    stockerInvCaches.put(inserterCardCache, inventoryCounts);
                }
                if (inventoryCounts.getCount(itemStack) == 0)
                    continue; //Move on if this inventory doesn't have any of this item

                transferResult.addResult(ItemHandlerUtil.extractItemWithSlots(laserNodeHandler.be, laserNodeHandler.handler, itemStack, itemStack.getCount(), true, stockerCardCache.isCompareNBT, inserterCardCache));
                transferResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
                if (transferResult.getTotalItemCounts() == origCountNeeded) {
                    transferResult.doIt(); //Move the items for real - we have both extractor/inserter caches from the above method
                    int lastSlot = transferResult.results.get(transferResult.results.size() - 1).extractSlot; //The last slot we pulled from in this inventory
                    if (!laserNodeHandler.handler.getStackInSlot(lastSlot).isEmpty()) //If its not empty now
                        stockerDestinationCache.put(new StockerRequest(stockerCardCache, new ItemStackKey(itemStack, stockerCardCache.isCompareNBT)), new StockerSource(inserterCardCache, lastSlot)); //Add to the cache
                    return true;
                }
                //If we got here, we still need (more of) this item - check the next inventory
                itemStack.setCount(origCountNeeded - transferResult.getTotalItemCounts()); //Shrink the amount of items we want
            }
            //If we got here, we didn't get ALL we needed. If this is exact mode, we try the next item. If its not, and we got more than 0, return it.
            if (!stockerCardCache.exact && transferResult.getTotalItemCounts() > 0) { //If its exact mode and we got here, we clearly didn't get all we wanted....
                transferResult.doIt(); //Move the items for real - we have both extractor/inserter caches from the above method
                return true;
            }
        }
        return false; //If we got NOTHING
    }

    /**
     * Attempts to insert @param stack into @param destitemHandler
     *
     * @return how many items fit
     */
    public int testInsertToInventory(IItemHandler destitemHandler, ItemStack stack) {
        ItemStack tempStack = ItemHandlerHelper.insertItem(destitemHandler, stack, true);
        int remainder = tempStack.getCount();
        return stack.getCount() - remainder;
    }

    /** Determine how many items from @param itemStack can fit into @param destinationInventory based on the filter in @param inserterCardCache **/
    /*public int getTransferAmt(ItemStack itemStack, IItemHandler destinationInventory, InserterCardCache inserterCardCache) {
        ItemStack insertFilter = inserterCardCache.filterCard;
        if (insertFilter.getItem() instanceof FilterBasic || insertFilter.isEmpty() || insertFilter.getItem() instanceof FilterTag) { // Basic cards send as many items as can fit into an inventory
            return testInsertToInventory(destinationInventory, itemStack);
        } else if (insertFilter.getItem() instanceof FilterCount) { //Count cards send up to <X> amount determined by the filter
            ItemHandlerUtil.InventoryCounts invCache = new ItemHandlerUtil.InventoryCounts(destinationInventory, BaseFilter.getCompareNBT(insertFilter)); //Cache the items in the destination
            int countOfItem = invCache.getCount(itemStack); //Find out how many of this itemStack we have in the target inventory
            int desiredAmt = inserterCardCache.getFilterAmt(itemStack); //Find out how many we want from the InserterCardCache

            if (countOfItem >= desiredAmt) { //Compare what we want to the target inventory, if we have enough return
                return 0;
            }

            //Doing this rather than copying.
            int neededAmt = desiredAmt - countOfItem; //How many items we need to fulfill this inventory
            if (itemStack.getCount() > neededAmt) //If we're trying to send more items than needed
                itemStack.setCount(neededAmt); //Set the size of the stack, rather than copying it

            //Test how many of what we weed can actually fit into the destination and return that amount
            return testInsertToInventory(destinationInventory, itemStack);
        }
        return 0;
    }*/

    public void drawParticlesClient() {
        if (particleRenderData.isEmpty()) return;
        ClientLevel clientLevel = (ClientLevel) level;
        //int particlesDrawnThisTick = 0;
        for (ParticleRenderData partData : particleRenderData) {
            //if (particlesDrawnThisTick > 64) return;
            ItemStack itemStack = new ItemStack(Item.byId(partData.item), partData.itemCount);
            BlockPos toPos = partData.toPos;
            BlockPos fromPos = partData.fromPos;
            Direction direction = Direction.values()[partData.direction];

            Vector3f extractOffset = findOffset(direction, partData.position, offsets);
            ItemFlowParticleData data = new ItemFlowParticleData(itemStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
            float randomSpread = 0.01f;
            int min = 1;
            int max = 64;
            int minPart = 32;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (itemStack.getCount() - min)) / (max - min) + minPart;
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
            }
        }
        //System.out.println(particlesDrawnThisTick);
    }

    /** Adds from the PacketNodeParticles a set of particles to draw next client tick **/
    public void addParticleData(ParticleRenderData particleRenderData) {
        this.particleRenderData.add(particleRenderData);
    }

    /** Draw the particles between node and inventory **/
    public void drawParticles(ItemStack itemStack, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
        drawParticles(itemStack, itemStack.getCount(), fromDirection, sourceBE, destinationBE, destinationDirection, extractPosition, insertPosition);
    }

    /** Draw the particles between node and inventory **/
    public void drawParticles(ItemStack itemStack, int amount, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
        ServerTickHandler.addToList(new ParticleData(Item.getId(itemStack.getItem()), (byte) amount, sourceBE.getBlockPos(), (byte) fromDirection.ordinal(), destinationBE.getBlockPos(), (byte) destinationDirection.ordinal(), (byte) extractPosition, (byte) insertPosition), level);

        /*ServerLevel serverWorld = (ServerLevel) level;
        //Extract
        BlockPos fromPos = getBlockPos().relative(direction);
        BlockPos toPos = getBlockPos();
        Vector3f extractOffset = findOffset(direction, position, offsets);
        ItemFlowParticleData data = new ItemFlowParticleData(itemStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
        float randomSpread = 0.01f;
        serverWorld.sendParticles(data, fromPos.getX() + extractOffset.x(), fromPos.getY() + extractOffset.y(), fromPos.getZ() + extractOffset.z(), 8 * itemStack.getCount(), randomSpread, randomSpread, randomSpread, 0);
        //Insert
        fromPos = destinationBE.getBlockPos();
        toPos = destinationBE.getBlockPos().relative(destinationDirection);
        Vector3f insertOffset = findOffset(destinationDirection, insertPosition, offsets);
        data = new ItemFlowParticleData(itemStack, toPos.getX() + insertOffset.x(), toPos.getY() + insertOffset.y(), toPos.getZ() + insertOffset.z(), 10);
        serverWorld.sendParticles(data, fromPos.getX() + insertOffset.x(), fromPos.getY() + insertOffset.y(), fromPos.getZ() + insertOffset.z(), 8 * itemStack.getCount(), randomSpread, randomSpread, randomSpread, 0);
        */
    }

    /** Called when changes happen - such as a card going into a side, or a card being modified via container **/
    public void updateThisNode() {
        setChanged();
        notifyOtherNodesOfChange();
        markDirtyClient();
        findMyExtractors();
        findMyStockers();
        updateOverclockers();
    }

    /** When this node changes, tell other nodes to refresh their cache of it **/
    public void notifyOtherNodesOfChange() {
        for (BlockPos pos : otherNodesInNetwork) {
            LaserNodeBE node = getNodeAt(getWorldPos(pos));
            if (node == null) continue;
            node.checkInvNode(this.getBlockPos(), true);
        }
    }

    /** This method clears the non-persistent inventory node data variables and regenerates them from scratch */
    public void refreshAllInvNodes() {
        inserterNodes.clear();
        inserterCache.clear();
        channelOnlyCache.clear();
        this.stockerDestinationCache.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            checkInvNode(getWorldPos(pos), false);
        }
        sortInserters();
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * Also populates the providerNodes and stockerNodes variables, so we know which inventory nodes provide or keep in stock items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos, boolean sortInserters) {
        LaserNodeBE be = getNodeAt(pos);
        BlockPos relativePos = getRelativePos(pos);
        //Remove this position from all caches, so we can repopulate below
        inserterNodes.removeIf(p -> p.relativePos.equals(relativePos));
        inserterCache.clear(); //TODO maybe just remove destinations that match this blockPos
        channelOnlyCache.clear();
        this.stockerDestinationCache.clear();
        if (be == null) return; //If the block position given doesn't contain a LaserNodeBE stop
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = be.nodeSideCaches[direction.ordinal()];
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        //sendItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        //getItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.INSERT)) {
                        inserterNodes.add(new InserterCardCache(relativePos, direction, card, be, slot));
                    }
                }
            }
        }
        if (sortInserters) sortInserters();
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IItemHandler> getAttachedInventory(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        LazyOptional<IItemHandler> testHandler = (facingHandler.get(sideConnection));
        if (testHandler != null && testHandler.isPresent()) {
            return testHandler;
        }

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IItemHandler> handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventorySide);
            if (handler.isPresent()) {
                // add the invalidator
                handler.addListener(getInvalidator(sideConnection));
                // cache and return
                facingHandler.put(sideConnection, handler);
                return handler;
            }
        }
        // no item handler, cache empty
        facingHandler.remove(sideConnection);
        return LazyOptional.empty();
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IItemHandler> getAttachedInventoryNoCache(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IItemHandler> handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventorySide);
            if (handler.isPresent()) {
                return handler;
            }
        }
        return LazyOptional.empty();
    }

    private NonNullConsumer<LazyOptional<IItemHandler>> getInvalidator(SideConnection sideConnection) {
        return connectionInvalidator.computeIfAbsent(sideConnection, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
            if (te.facingHandler.get(sideConnection) == handler) {
                te.clearCachedInventories(sideConnection);
            }
        }));
    }


    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories(SideConnection sideConnection) {
        stockerDestinationCache.clear();
        this.facingHandler.remove(sideConnection);
    }

    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories() {
        stockerDestinationCache.clear();
        this.facingHandler.clear();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null) {
            return nodeSideCaches[side.ordinal()].handlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    public void saveRoundRobin() {
        for (Map.Entry<ExtractorCardCache, Integer> entry : roundRobinMap.entrySet()) {
            BaseCard.setRoundRobinPosition(entry.getKey().cardItem, entry.getValue());
        }
    }

    public void loadRoundRobin() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                int lastRR = BaseCard.getRoundRobinPosition(extractorCardCache.cardItem);
                if (lastRR != -1)
                    roundRobinMap.put(extractorCardCache, lastRR);
            }
            for (StockerCardCache stockerCardCache : nodeSideCache.stockerCardCaches) {
                int lastRR = BaseCard.getRoundRobinPosition(stockerCardCache.cardItem);
                if (lastRR != -1)
                    roundRobinMap.put(stockerCardCache, lastRR);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        for (int i = 0; i < Direction.values().length; i++) {
            NodeSideCache nodeSideCache = nodeSideCaches[i];
            if (tag.contains("Inventory" + i)) {
                nodeSideCache.itemHandler.deserializeNBT(tag.getCompound("Inventory" + i));
                if (nodeSideCache.itemHandler.getSlots() < LaserNodeContainer.SLOTS) {
                    nodeSideCache.itemHandler.reSize(LaserNodeContainer.SLOTS);
                }
            }
        }
        super.load(tag);

    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < Direction.values().length; i++) {
            NodeSideCache nodeSideCache = nodeSideCaches[i];
            tag.put("Inventory" + i, nodeSideCache.itemHandler.serializeNBT());
        }
        saveRoundRobin();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Arrays.stream(nodeSideCaches).forEach(e -> e.handlerLazyOptional.invalidate());
    }
}
