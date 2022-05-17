package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.*;
import com.mojang.math.Vector3f;
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

    private final Map<StockerCardCache, StockerSource> stockerDestinationCache = new HashMap<>();
    private final Map<StockerRequest, StockerSource> stockerCountDestinationCache = new HashMap<>();

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
            this.stockerCountDestinationCache.clear();
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        nodeSideCache.stockerCardCaches.add(new StockerCardCache(direction, card, slot));
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
                        nodeSideCache.extractorCardCaches.add(new ExtractorCardCache(direction, card, slot));
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

    public boolean extractForExact(ExtractorCardCache extractorCardCache, IItemHandler fromInventory, ItemStack extractStack) {
        TransferResult extractResults = (ItemHandlerUtil.extractItemWithSlots(fromInventory, extractStack, extractStack.getCount(), true, extractorCardCache.isCompareNBT)); //Fake Extract
        int amtNeeded = extractStack.getCount();
        if (extractResults.getTotalItemCounts() != amtNeeded) //Return if we didn't get what we needed
            return false;

        TransferResult insertResults = new TransferResult();

        //Begin test inserting into inserters
        int amtStillNeeded = amtNeeded;
        for (InserterCardCache inserterCardCache : getPossibleInserters(extractorCardCache, extractStack)) {
            LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
            if (laserNodeHandler == null) continue;

            TransferResult thisResult = ItemHandlerUtil.insertItemWithSlots(laserNodeHandler.handler, extractStack, 0, true, extractorCardCache.isCompareNBT, true, inserterCardCache); //Test!!
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
            ItemStack tempStack = extractStack.split(result.count());
            result.handler().insertItem(result.slot(), tempStack, false);
            if (result.inserterCardCache() != null)
                drawParticles(tempStack, extractorCardCache.direction, this, getNodeAt(getWorldPos(result.inserterCardCache().relativePos)), result.inserterCardCache().direction, extractorCardCache.cardSlot, result.inserterCardCache().cardSlot);
        }

        for (TransferResult.Result result : extractResults.results) {
            result.handler().extractItem(result.slot(), result.count(), false);
        }

        return true;
    }

    public boolean extractItemStack(ExtractorCardCache extractorCardCache, IItemHandler fromInventory, ItemStack extractStack) {
        TransferResult transferResults = new TransferResult();
        if (extractorCardCache instanceof StockerCardCache)
            transferResults = (ItemHandlerUtil.extractItemWithSlotsBackwards(fromInventory, extractStack, extractStack.getCount(), true, extractorCardCache.isCompareNBT)); //Fake Extract
        else
            transferResults = (ItemHandlerUtil.extractItemWithSlots(fromInventory, extractStack, extractStack.getCount(), true, extractorCardCache.isCompareNBT)); //Fake Extract

        if (transferResults.results.isEmpty()) //If we didn't get any items out.
            return false;

        int amtExtractedRemaining = transferResults.getTotalItemCounts();

        extractStack.setCount(amtExtractedRemaining); //Set the extract stack - used to insert with - to how many we got

        for (InserterCardCache inserterCardCache : getPossibleInserters(extractorCardCache, extractStack)) {
            LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
            if (laserNodeHandler == null) continue;

            TransferResult insertResults = ItemHandlerUtil.insertItemWithSlots(laserNodeHandler.handler, extractStack, 0, false, extractorCardCache.isCompareNBT, true, inserterCardCache);
            int amtFit = insertResults.getTotalItemCounts(); //How many items fit (Above)
            int amtNoFit = amtExtractedRemaining - amtFit; //How many items didn't fit in the inv (above)
            extractStack.setCount(amtNoFit);
            for (TransferResult.Result result : transferResults.results) {
                if (insertResults.results.isEmpty()) { //if we inserted nothing, this inv is full
                    break;
                }
                transferResults.results.remove(result); //If we inserted something, Remove this (extract) result from the list
                int amtToRemove = Math.min(result.count(), amtFit); //Extract either the amount we removed or the size of this slot, whichever is smaller
                amtFit -= amtToRemove;
                if (amtToRemove != result.count())
                    transferResults.results.add(new TransferResult.Result(result.handler(), result.count() - amtToRemove, result.slot(), null)); //Re Add to the list if this is a partial insert of the extracted items
                ItemStack extractedStack = fromInventory.extractItem(result.slot(), amtToRemove, false); //remove the items we got
                drawParticles(extractedStack, extractorCardCache.direction, this, laserNodeHandler.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
                amtExtractedRemaining -= amtToRemove; //Decrement the amount we have remaining by the amt that was extracted from this pass
                if (amtExtractedRemaining == 0) //If we fit everything into the inventory and extracted everything, return true.
                    return true;
                if (amtNoFit == amtExtractedRemaining) //If we reached the total amount that fit in this inventory, stop extracting
                    break;
            }
        }
        return transferResults.getTotalItemCounts() != amtExtractedRemaining; //If the amount of items remaining is different from our cached amount, we moved SOMETHING
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

    public boolean canAnyFiltersFit(IItemHandler adjacentInventory, List<ItemStack> filteredItems, boolean exact) {
        if (exact) {
            for (ItemStack stack : filteredItems) {
                int amountFit = testInsertToInventory(adjacentInventory, stack); //How many will fit in our inventory
                if (amountFit == stack.getCount()) {
                    return true;
                }
            }
        } else {
            for (ItemStack stack : filteredItems) {
                int amountFit = testInsertToInventory(adjacentInventory, stack); //How many will fit in our inventory
                if (amountFit > 0) {
                    return true;
                }
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
            if (!canAnyFiltersFit(adjacentInventory, stockerCardCache.getFilteredItems(), stockerCardCache.exact)) {
                return false; //If we can't fit any of our filtered items into this inventory, don't bother scanning for them
            }
            boolean foundItems = findItemStackForStocker(stockerCardCache, adjacentInventory); //Start looking for this item
            if (foundItems)
                return true;

            //If we get to this line of code, it means we found none of the filter
            stockerCardCache.setRemainingSleep(stockerCardCache.tickSpeed * 5);
        } else if (filter.getItem() instanceof FilterTag) {

        }
        return false;
    }

    public ItemStack getStackAtStockerCachePosition(StockerSource checkSource) {
        //System.out.println("Cache found, checking for item at: " + getWorldPos(checkSource.inserterCardCache.relativePos) + " slot " + checkSource.slot);
        LaserNodeHandler laserNodeHandler = getLaserNodeHandler(checkSource.inserterCardCache);
        if (laserNodeHandler == null) return ItemStack.EMPTY;
        return laserNodeHandler.handler.getStackInSlot(checkSource.slot);
    }

    /**
     * Trys to pull from the last place we found this item - checking the same slot first, then the rest of the inventory.
     * Returns the itemstack we found.
     */
    public ItemStack tryStockerCache(StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        if (!stockerDestinationCache.containsKey(stockerCardCache))
            return ItemStack.EMPTY;
        int origItemsWanted = stockerCardCache.extractAmt;
        int itemsStillNeeded = origItemsWanted;
        StockerSource checkSource = stockerDestinationCache.get(stockerCardCache);
        ItemStack stackInSlot = getStackAtStockerCachePosition(checkSource);
        if (stackInSlot == null) //Null means the inventory no longer exists or is unloaded
            return ItemStack.EMPTY;

        ItemStack extractedItemStack = ItemStack.EMPTY;
        LaserNodeHandler laserNodeHandler = getLaserNodeHandler(checkSource.inserterCardCache);
        if (laserNodeHandler == null) return ItemStack.EMPTY;
        if (!stackInSlot.isEmpty()) {
            if (stockerCardCache.isStackValidForCard(stackInSlot)) {  //If the itemstack in that spot meets ANY filter for this card (Even if its different from the last pull)
                int extractAmt = Math.min(itemsStillNeeded, stackInSlot.getCount()); //Find out how many to extract
                extractedItemStack = laserNodeHandler.handler.extractItem(checkSource.slot, extractAmt, false); //Extract Items
                drawParticles(extractedItemStack, checkSource.inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                ItemHandlerHelper.insertItem(stockerInventory, extractedItemStack, false); //Actually insert into the destination
                //System.out.println("Got " + extractedItemStack.getCount() + " items from cache slot " + checkSource.slot + " at " + getWorldPos(checkSource.inserterCardCache.relativePos));
                itemsStillNeeded = itemsStillNeeded - extractedItemStack.getCount();
                if (stackInSlot.isEmpty()) {
                    //System.out.println("Emptied existing cache slot  at " + checkSource.slot + " at " + getWorldPos(checkSource.inserterCardCache.relativePos));
                    stockerDestinationCache.remove(stockerCardCache);
                }
                if (itemsStillNeeded == 0) {
                    return extractedItemStack; //If we got all that we need, return the amount we got
                }
            }
        }
        //If we got here, we have to find more of this item type, so start looking for it
        //System.out.println("Still need " + itemsStillNeeded + " items - checking chest at " + getWorldPos(checkSource.inserterCardCache.relativePos));
        ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(laserNodeHandler.handler, extractedItemStack, itemsStillNeeded, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
        if (extractResult.itemStack().isEmpty()) {
            //System.out.println("This chest no longer has any of the item, removing from cache");
            stockerDestinationCache.remove(stockerCardCache);
            return extractedItemStack;
        }

        //System.out.println("Got " + extractResult.itemStack().getCount() + " items from slot " + extractResult.slot() + " in the chest");
        drawParticles(extractResult.itemStack(), checkSource.inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
        ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination

        if (extractResult.slot() != -1 && !laserNodeHandler.handler.getStackInSlot(extractResult.slot()).isEmpty()) {//If we got it from another slot in the chest, update the cache
            //System.out.println("Updating cache at: " + getWorldPos(checkSource.inserterCardCache.relativePos) + " to slot " + extractResult.slot());
            stockerDestinationCache.put(stockerCardCache, new StockerSource(checkSource.inserterCardCache, extractResult.slot()));
        }
        extractedItemStack.grow(extractResult.itemStack().getCount());
        return extractedItemStack;
    }

    /**
     * Trys to pull from the last place we found this item - checking the same slot first, then the rest of the inventory.
     * Returns the itemstack we found.
     */
    public ItemStack tryStockerCacheCount(StockerCardCache stockerCardCache, IItemHandler stockerInventory, ItemStack itemStack) {
        StockerRequest stockerRequest = new StockerRequest(stockerCardCache, new ItemStackKey(itemStack, stockerCardCache.isCompareNBT));
        if (!stockerCountDestinationCache.containsKey(stockerRequest))
            return ItemStack.EMPTY;
        int origItemsWanted = itemStack.getCount();
        int itemsStillNeeded = origItemsWanted;
        StockerSource checkSource = stockerCountDestinationCache.get(stockerRequest);
        ItemStack stackInSlot = getStackAtStockerCachePosition(checkSource);
        if (stackInSlot == null) //Null means the inventory no longer exists or is unloaded
            return ItemStack.EMPTY;

        ItemStack extractedItemStack = ItemStack.EMPTY;
        LaserNodeHandler laserNodeHandler = getLaserNodeHandler(checkSource.inserterCardCache);
        if (laserNodeHandler == null) return ItemStack.EMPTY;
        if (!stackInSlot.isEmpty()) {
            if (stockerCardCache.isStackValidForCard(stackInSlot)) {  //If the itemstack in that spot meets ANY filter for this card (Even if its different from the last pull)
                int extractAmt = Math.min(itemsStillNeeded, stackInSlot.getCount()); //Find out how many to extract
                extractedItemStack = laserNodeHandler.handler.extractItem(checkSource.slot, extractAmt, false); //Extract Items
                drawParticles(extractedItemStack, checkSource.inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                ItemHandlerHelper.insertItem(stockerInventory, extractedItemStack, false); //Actually insert into the destination
                //System.out.println("Got " + extractedItemStack.getCount() + " items from cache slot " + checkSource.slot + " at " + getWorldPos(checkSource.inserterCardCache.relativePos));
                itemsStillNeeded = itemsStillNeeded - extractedItemStack.getCount();
                if (stackInSlot.isEmpty()) {
                    //System.out.println("Emptied existing cache slot  at " + checkSource.slot + " at " + getWorldPos(checkSource.inserterCardCache.relativePos));
                    stockerCountDestinationCache.remove(stockerRequest);
                }
                if (itemsStillNeeded == 0) {
                    return extractedItemStack; //If we got all that we need, return the amount we got
                }
            }
        }
        //If we got here, we have to find more of this item type, so start looking for it
        //System.out.println("Still need " + itemsStillNeeded + " items - checking chest at " + getWorldPos(checkSource.inserterCardCache.relativePos));
        ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(laserNodeHandler.handler, extractedItemStack, itemsStillNeeded, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
        if (extractResult.itemStack().isEmpty()) {
            //System.out.println("This chest no longer has any of the item, removing from cache");
            stockerCountDestinationCache.remove(stockerRequest);
            return extractedItemStack;
        }

        //System.out.println("Got " + extractResult.itemStack().getCount() + " items from slot " + extractResult.slot() + " in the chest");
        drawParticles(extractResult.itemStack(), checkSource.inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
        ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination

        if (extractResult.slot() != -1 && !laserNodeHandler.handler.getStackInSlot(extractResult.slot()).isEmpty()) {//If we got it from another slot in the chest, update the cache
            //System.out.println("Updating cache at: " + getWorldPos(checkSource.inserterCardCache.relativePos) + " to slot " + extractResult.slot());
            stockerCountDestinationCache.put(stockerRequest, new StockerSource(checkSource.inserterCardCache, extractResult.slot()));
        }
        extractedItemStack.grow(extractResult.itemStack().getCount());
        return extractedItemStack;
    }

    public boolean findItemStackForStocker(StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        boolean isCount = stockerCardCache.filterCard.getItem() instanceof FilterCount;
        if (!isCount) {
            if (!tryStockerCache(stockerCardCache, stockerInventory).equals(ItemStack.EMPTY))
                return true;
        }
        //System.out.println("Scanning all inventories for items in cache");
        int origItemsWanted = stockerCardCache.extractAmt;
        int itemsStillNeeded = origItemsWanted;
        ItemStack firstStackFound = ItemStack.EMPTY;

        List<ItemStack> filteredItemsList = stockerCardCache.getFilteredItems();
        //System.out.println("Filtered Items List: " + filteredItemsList);
        if (isCount) { //If this is a filter count, prune the list of items to search for to just what we need
            ItemHandlerUtil.InventoryCounts stockerInventoryCount = new ItemHandlerUtil.InventoryCounts(stockerInventory, stockerCardCache.isCompareNBT);
            List<ItemStack> tempList = new ArrayList<>(filteredItemsList);
            for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
                int amtHad = stockerInventoryCount.getCount(itemStack);
                if (amtHad >= itemStack.getCount()) { //if we have enough, move onto the next stack after removing this one from the list
                    tempList.remove(itemStack);
                    continue;
                }
                itemStack.setCount(Math.min(itemStack.getCount() - amtHad, itemsStillNeeded));
                if (!tryStockerCacheCount(stockerCardCache, stockerInventory, itemStack).equals(ItemStack.EMPTY))
                    return true;
            }
            //System.out.println("Narrowed Down List: " + tempList);
            filteredItemsList = tempList;
        }


        for (InserterCardCache inserterCardCache : getChannelMatchInserters(stockerCardCache)) { //Iterate through ALL inserter nodes on this channel only
            LaserNodeHandler laserNodeHandler = getLaserNodeHandler(inserterCardCache);
            if (laserNodeHandler == null) continue;

            if (firstStackFound.equals(ItemStack.EMPTY)) { //If we haven't found any items yet, start looking for anything that matches our filter
                //System.out.println("firstStackFound is empty == Looking for anything at :" + be.getBlockPos());
                ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(laserNodeHandler.handler, stockerCardCache.isCompareNBT);
                for (ItemStack itemStack : filteredItemsList) {
                    if (!inserterCardCache.isStackValidForCard(itemStack)) //If this item stack can't come from this inserter, check next stack
                        continue;
                    if (inventoryCounts.getCount(itemStack) == 0)
                        continue; //Next item if the chest has none of this item

                    if (isCount)  //If this is a filter count, adjust the amount of this item we are looking for
                        itemsStillNeeded = Math.min(itemStack.getCount(), itemsStillNeeded);

                    itemStack.setCount(itemsStillNeeded);
                    int amountFit = testInsertToInventory(stockerInventory, itemStack);
                    if (amountFit == 0) continue; //If none of this item fit into the destination, go to the next item
                    //System.out.println("Extracting From: " + be.getBlockPos());
                    ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(laserNodeHandler.handler, itemStack, amountFit, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
                    if (extractResult.itemStack().isEmpty())
                        continue; //If we didn't find anything in this inventory, move onto the next -- Should never happen?
                    drawParticles(extractResult.itemStack(), inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                    ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination
                    itemsStillNeeded = itemsStillNeeded - extractResult.itemStack().getCount();
                    //itemStack.setCount(itemsStillNeeded); //Adjust the stack size to how many more items we need, maybe zero
                    //System.out.println("Found " + extractResult.itemStack().getCount() + " " + extractResult.itemStack().getItem() + ". Need " + itemsStillNeeded + " more.");
                    if (extractResult.slot() != -1 && itemsStillNeeded == 0) {
                        //System.out.println("Adding to cache at: " + getWorldPos(inserterCardCache.relativePos) + " to slot " + extractResult.slot());
                        if (isCount)
                            stockerCountDestinationCache.put(new StockerRequest(stockerCardCache, new ItemStackKey(extractResult.itemStack(), stockerCardCache.isCompareNBT)), new StockerSource(inserterCardCache, extractResult.slot()));
                        else
                            stockerDestinationCache.put(stockerCardCache, new StockerSource(inserterCardCache, extractResult.slot()));
                    }
                    if (itemsStillNeeded == 0) return true; //If we got all we need, return true
                    firstStackFound = extractResult.itemStack().copy(); //If we found 3 cobble, but wanted 5, now we start looking for the rest of the cobble
                    break;
                }
            } else { //If we found a partial stack, start looking for the rest of it.
                if (!inserterCardCache.isStackValidForCard(firstStackFound)) //If this item stack can't come from this inserter, check next stack
                    continue;
                //System.out.println("firstStackFound contains " + firstStackFound.getItem() + " looking for " + itemsStillNeeded + " more of them");
                //System.out.println("Extracting From: " + be.getBlockPos());
                ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(laserNodeHandler.handler, firstStackFound, itemsStillNeeded, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
                if (extractResult.itemStack().isEmpty())
                    continue; //If we didn't find anything in this inventory, move onto the next
                drawParticles(extractResult.itemStack(), inserterCardCache.direction, laserNodeHandler.be, this, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination
                itemsStillNeeded = itemsStillNeeded - extractResult.itemStack().getCount();
                firstStackFound.grow(extractResult.itemStack().getCount()); //Adjust the stack size to how many more items we need, maybe zero
                //System.out.println("Found " + extractResult.itemStack().getCount() + " " + extractResult.itemStack().getItem() + ". Need " + itemsStillNeeded + " more.");
                if (extractResult.slot() != -1 && itemsStillNeeded == 0) {
                    //System.out.println("Adding to cache at: " + getWorldPos(inserterCardCache.relativePos) + " to slot " + extractResult.slot());
                    if (isCount)
                        stockerCountDestinationCache.put(new StockerRequest(stockerCardCache, new ItemStackKey(extractResult.itemStack(), stockerCardCache.isCompareNBT)), new StockerSource(inserterCardCache, extractResult.slot()));
                    else
                        stockerDestinationCache.put(stockerCardCache, new StockerSource(inserterCardCache, extractResult.slot()));
                }
                if (itemsStillNeeded == 0) return true; //If zero, return true meaning we found it all
            }
        }
        if (origItemsWanted != itemsStillNeeded)
            return true; //If we got SOMETHING
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
    public int getTransferAmt(ItemStack itemStack, IItemHandler destinationInventory, InserterCardCache inserterCardCache) {
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
    }

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
        BlockPos fromPos = getBlockPos().relative(fromDirection);
        BlockPos toPos = getBlockPos();
        Vector3f extractOffset = findOffset(fromDirection, extractPosition, offsets);
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
        this.stockerDestinationCache.clear(); //Clear these 2 because if inserters changed, we don't want to reference an existing inserter
        this.stockerCountDestinationCache.clear();
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
        this.stockerDestinationCache.clear(); //Clear these 2 because if inserters changed, we don't want to reference an existing inserter
        this.stockerCountDestinationCache.clear();
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
                        inserterNodes.add(new InserterCardCache(relativePos, direction, card, slot));
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
        stockerCountDestinationCache.clear();
        this.facingHandler.remove(sideConnection);
    }

    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories() {
        stockerDestinationCache.clear();
        stockerCountDestinationCache.clear();
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
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Arrays.stream(nodeSideCaches).forEach(e -> e.handlerLazyOptional.invalidate());
    }
}
