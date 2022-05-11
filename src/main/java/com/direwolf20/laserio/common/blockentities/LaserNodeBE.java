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

    private Map<SideConnection, LazyOptional<IItemHandler>> facingHandler = new HashMap<>();
    private final Map<SideConnection, NonNullConsumer<LazyOptional<IItemHandler>>> connectionInvalidator = new HashMap<>();

    /** Variables for tracking and sending items/filters/etc **/
    private Set<BlockPos> otherNodesInNetwork = new HashSet<>();
    private final List<InserterCardCache> inserterNodes = new CopyOnWriteArrayList<>(); //All Inventory nodes that contain an inserter card
    private final HashMap<ExtractorCardCache, HashMap<ItemStackKey, List<InserterCardCache>>> inserterCache = new HashMap<>();
    private List<ParticleRenderData> particleRenderData = new ArrayList<>();
    private Random random = new Random();

    private record StockerRequest(StockerCardCache stockerCardCache, ItemStackKey itemStackKey) {

    }

    private record StockerSource(InserterCardCache inserterCardCache, int slot) {

    }

    private final Map<StockerRequest, StockerSource> stockerDestinationCache = new HashMap<>();
    private final Map<StockerCardCache, Integer> stockerSleepers = new Object2IntOpenHashMap<>();

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

    /** Loop through all the extractorCards and run the extractions **/
    public void extractItems() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            int countCardsHandled = 0;
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                if (countCardsHandled > nodeSideCache.overClocker) return;
                if (sendItems(extractorCardCache))
                    countCardsHandled++;
            }
            for (StockerCardCache stockerCardCache : nodeSideCache.stockerCardCaches) {
                if (stockerSleepers.containsKey(stockerCardCache)) {
                    int sleepRemaining = stockerSleepers.get(stockerCardCache);
                    //System.out.println("Stock card at " + this.getBlockPos() + " in slot " + stockerCardCache.cardSlot + " has " + sleepRemaining + " sleep time remaining, decrementing by 1 - worldtime: " + level.getGameTime());
                    sleepRemaining--;
                    if (sleepRemaining == 1)
                        stockerSleepers.remove(stockerCardCache);
                    else
                        stockerSleepers.put(stockerCardCache, sleepRemaining);
                    continue;
                }
                if (countCardsHandled > nodeSideCache.overClocker) return;
                if (stockItems(stockerCardCache))
                    countCardsHandled++;
            }
        }
    }

    public void tickClient() {
        drawParticlesClient();
        particleRenderData.clear();
    }

    public void tickServer() {
        /*if (level.getGameTime() % 40 == 0) {
            System.out.println("My chunk is loaded == " + level.isLoaded(getBlockPos()));
            System.out.println("I am alive at: " + getBlockPos());
            System.out.println("Inserter cache: " + inserterNodes.size());
        }*/
        if (!discoveredNodes) { //On world / chunk reload, lets rediscover the network, including this block's extractor cards.
            discoverAllNodes();
            findMyExtractors();
            findMyStockers();
            updateOverclockers();
            discoveredNodes = true;
        }
        extractItems(); //If this node has any extractors, do stuff with them
        /*if (!extractorCardCaches.isEmpty()) {
            drawParticles(new ItemStack(Blocks.GOLD_BLOCK), extractorCardCaches.get(0).direction, this, Direction.UP, 0, 0);
            drawParticles(new ItemStack(Blocks.DIAMOND_BLOCK), extractorCardCaches.get(0).direction, this, Direction.UP, 1, 1);
            drawParticles(new ItemStack(Blocks.REDSTONE_BLOCK), extractorCardCaches.get(0).direction, this, Direction.UP, 2, 2);
            drawParticles(new ItemStack(Blocks.IRON_BLOCK), extractorCardCaches.get(0).direction, this, Direction.UP, 4, 4);
            drawParticles(new ItemStack(Blocks.LAPIS_BLOCK), extractorCardCaches.get(0).direction, this, Direction.UP, 8, 8);
        }*/
    }

    public void sortInserters() {
        this.inserterNodes.sort(Comparator.comparingDouble(InserterCardCache::getDistance));
        this.inserterNodes.sort(Comparator.comparingInt(InserterCardCache::getPriority).reversed());
    }

    /** Multipurpose: finds all inserters that can be extracted to OR stocked from **/
    public List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, ItemStack stack) {
        ItemStackKey key = new ItemStackKey(stack, true);
        if (inserterCache.containsKey(extractorCardCache)) {
            if (inserterCache.get(extractorCardCache).containsKey(key))
                return inserterCache.get(extractorCardCache).get(key);
            else {
                List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.isStackValidForCard(stack))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                        .toList();
                inserterCache.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else {
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

    public boolean chunksLoaded(BlockPos nodePos, BlockPos destinationPos) {
        if (!level.isLoaded(nodePos)) {
            //System.out.println(nodePos + " node is unloaded, returning false");
            return false;
        }
        if (!level.isLoaded(destinationPos)) {
            //System.out.println(destinationPos + " chest is unloaded, returning false");
            return false;
        }
        return true;
    }

    //TODO Efficiency

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendItems(ExtractorCardCache extractorCardCache) {
        IItemHandler adjacentInventory = getAttachedInventory(extractorCardCache.direction, extractorCardCache.sneaky).orElse(EMPTY);
        for (int slot = 0; slot < adjacentInventory.getSlots(); slot++) {
            ItemStack stackInSlot = adjacentInventory.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || !(extractorCardCache.isStackValidForCard(stackInSlot))) continue;
            for (InserterCardCache inserterCardCache : getPossibleInserters(extractorCardCache, stackInSlot)) {
                BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
                if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction)))
                    continue; //Skip this if the node is unloaded

                LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
                if (be == null) continue;
                IItemHandler possibleDestination = be.getAttachedInventory(inserterCardCache.direction, inserterCardCache.sneaky).orElse(EMPTY);
                if (possibleDestination.getSlots() == 0) continue;
                ItemStack itemStack = adjacentInventory.extractItem(slot, extractorCardCache.extractAmt, true); //Pretend to pull the item out
                int transferAmt = getTransferAmt(itemStack, possibleDestination, inserterCardCache);
                if (transferAmt == 0) continue;  //If nothing fits in this destination, move onto the next
                itemStack.setCount(transferAmt);
                adjacentInventory.extractItem(slot, transferAmt, false); //Actually extract the number of items that fit
                drawParticles(itemStack, extractorCardCache.direction, this, be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
                ItemHandlerHelper.insertItem(possibleDestination, itemStack, false); //Actually insert into the destination
                return true;
            }
        }
        return false;
    }

    /** Stocker Cards call this, and try to find an inserter card to pull their items from **/
    public boolean stockItems(StockerCardCache stockerCardCache) {
        IItemHandler adjacentInventory = getAttachedInventory(stockerCardCache.direction, stockerCardCache.sneaky).orElse(EMPTY);
        ItemStack filter = stockerCardCache.filterCard;
        if (filter.isEmpty() || !stockerCardCache.isAllowList) { //Needs a filter - at least for now? Also must be in whitelist mode
            return false;
        }
        if (filter.getItem() instanceof FilterBasic) {
            List<ItemStack> filteredItems = stockerCardCache.getFilteredItems();
            for (ItemStack stack : filteredItems) {
                int originalCount = stack.getCount(); //store the original stack size
                stack.setCount(stockerCardCache.extractAmt); //Adjust the stack size to how much we want
                int amountFit = testInsertToInventory(adjacentInventory, stack); //How many will fit in our inventory
                if (amountFit == 0) {
                    stack.setCount(originalCount);
                    continue; //Check the next item in the list if we can't fit any more of this in here
                }
                if (amountFit < stack.getCount()) //Todo exact Transfer Mode Maybe?
                    stack.setCount(amountFit); //If less than what we want fits, adjust it
                int itemsStillNeeded = stack.getCount() - tryStockerCache(stack, stockerCardCache, adjacentInventory);
                if (itemsStillNeeded == 0) {
                    stack.setCount(originalCount);
                    return true;
                }
                stack.setCount(itemsStillNeeded);
                //System.out.println("Didn't find enough in the cache, still need " + itemsStillNeeded + " items");
                boolean foundItems = findItemStackForStocker(stack, stockerCardCache, adjacentInventory); //Start looking for this item
                if (foundItems) {
                    stack.setCount(originalCount);
                    return true;
                }
            }
            //If we get to this line of code, it means we found none of the filer
            System.out.println("Stock card at " + this.getBlockPos() + " in slot " + stockerCardCache.cardSlot + " has found nothing. Adding to sleeper list with 40 ticks at gametime: " + level.getGameTime());
            stockerSleepers.put(stockerCardCache, 40);
        } else if (filter.getItem() instanceof FilterCount) {
            ItemHandlerUtil.InventoryCounts invCache = new ItemHandlerUtil.InventoryCounts(adjacentInventory, BaseFilter.getCompareNBT(filter));

        } else if (filter.getItem() instanceof FilterTag) {

        }
        return false;
    }

    /**
     * Trys to pull from the last place we found this item - checking the same slot first, then the rest of the inventory.
     * Returns the amount of items extracted.
     */
    public int tryStockerCache(ItemStack itemStack, StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        ItemStackKey key = new ItemStackKey(itemStack, stockerCardCache.isCompareNBT);
        StockerRequest stockRequest = new StockerRequest(stockerCardCache, key);
        if (stockerDestinationCache.containsKey(stockRequest)) {
            int origItemsWanted = itemStack.getCount();
            int itemsStillNeeded = origItemsWanted;
            StockerSource checkSource = stockerDestinationCache.get(stockRequest);
            //System.out.println("Cache found, trying to pull from: " + getWorldPos(checkSource.inserterCardCache.relativePos));
            BlockPos nodeWorldPos = getWorldPos(checkSource.inserterCardCache.relativePos);
            if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(checkSource.inserterCardCache.direction)))
                return 0; //Skip this if the node is unloaded

            LaserNodeBE be = getNodeAt(getWorldPos(checkSource.inserterCardCache.relativePos));
            if (be == null) return 0; //If for some reason this Block Entity no longer exists - shouldn't ever happen

            IItemHandler possibleSource = be.getAttachedInventory(checkSource.inserterCardCache.direction, checkSource.inserterCardCache.sneaky).orElse(EMPTY);
            if (possibleSource.getSlots() == 0)
                return 0; //If the attached inventory no longer exists - shouldn't ever happen

            ItemStack stackInSlot = possibleSource.getStackInSlot(checkSource.slot);
            if (ItemHandlerUtil.doItemsMatch(stackInSlot, key.getStack(), stockerCardCache.isCompareNBT)) {
                int extractAmt = Math.min(itemsStillNeeded, stackInSlot.getCount()); //Find out how many to extract
                ItemStack extractedItemStack = possibleSource.extractItem(checkSource.slot, extractAmt, false); //Extract Items
                drawParticles(extractedItemStack, checkSource.inserterCardCache.direction, be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                ItemHandlerHelper.insertItem(stockerInventory, extractedItemStack, false); //Actually insert into the destination
                //System.out.println("Got "+extractedItemStack.getCount()+" items from cache slot " + checkSource.slot);
                itemsStillNeeded = itemsStillNeeded - extractedItemStack.getCount();
                if (itemsStillNeeded == 0)
                    return extractedItemStack.getCount(); //If we got all that we need, return the amount we got
            }
            //System.out.println("Still need " + itemsStillNeeded + " items - checking chest at " + getWorldPos(checkSource.inserterCardCache.relativePos));
            itemStack.setCount(itemsStillNeeded); //We only reach this point if we still have items to extract
            ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(possibleSource, itemStack, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
            if (extractResult.itemStack().isEmpty()) {
                //System.out.println("This chest no longer has any of the item, removing from cache");
                stockerDestinationCache.remove(stockRequest);
                return origItemsWanted - itemsStillNeeded; //Return how many items we actually got
            }

            //System.out.println("Got " + extractResult.itemStack().getCount() + " items from slot " + extractResult.slot() + " in the chest");
            drawParticles(extractResult.itemStack(), checkSource.inserterCardCache.direction, be, this, stockerCardCache.direction, checkSource.inserterCardCache.cardSlot, stockerCardCache.cardSlot);
            ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination
            itemsStillNeeded = itemsStillNeeded - extractResult.itemStack().getCount();
            itemStack.setCount(itemsStillNeeded); //Adjust the stack size to how many more items we need, maybe zero
            if (extractResult.slot() != -1) {//If we got it from another slot in the chest, update the cache
                //System.out.println("Updating cache at: " + getWorldPos(checkSource.inserterCardCache.relativePos) + " to slot " + extractResult.slot());
                stockerDestinationCache.put(new StockerRequest(stockerCardCache, key), new StockerSource(checkSource.inserterCardCache, extractResult.slot()));
            }
            return origItemsWanted - itemsStillNeeded; //Return how many items we actually got
        }
        return 0;
    }

    public boolean findItemStackForStocker(ItemStack itemStack, StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        System.out.println("Scanning all inventories for item");
        int origItemsWanted = itemStack.getCount();
        int itemsStillNeeded = origItemsWanted;
        for (InserterCardCache inserterCardCache : getPossibleInserters(stockerCardCache, itemStack)) {
            BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
            if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction)))
                continue; //Skip this if the node is unloaded

            LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
            if (be == null) continue;
            IItemHandler possibleSource = be.getAttachedInventory(inserterCardCache.direction, inserterCardCache.sneaky).orElse(EMPTY);
            if (possibleSource.getSlots() == 0) continue;
            ItemHandlerUtil.ExtractResult extractResult = ItemHandlerUtil.extractItem(possibleSource, itemStack, false, stockerCardCache.isCompareNBT); //Try to pull out the items we need from this location
            if (extractResult.itemStack().isEmpty())
                continue; //If we didn't find anything in this inventory, move onto the next
            drawParticles(extractResult.itemStack(), inserterCardCache.direction, be, this, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
            ItemHandlerHelper.insertItem(stockerInventory, extractResult.itemStack(), false); //Actually insert into the destination
            itemsStillNeeded = itemsStillNeeded - extractResult.itemStack().getCount();
            itemStack.setCount(itemsStillNeeded); //Adjust the stack size to how many more items we need, maybe zero
            if (extractResult.slot() != -1) {
                //System.out.println("Adding to cache at: " + getWorldPos(inserterCardCache.relativePos) + " to slot " + extractResult.slot());
                stockerDestinationCache.put(new StockerRequest(stockerCardCache, new ItemStackKey(extractResult.itemStack(), stockerCardCache.isCompareNBT)), new StockerSource(inserterCardCache, extractResult.slot()));
            }
            if (itemStack.getCount() == 0) return true; //If zero, return true
        }
        if (origItemsWanted != itemStack.getCount())
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
            int minPart = 8;
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
        ServerTickHandler.addToList(new ParticleData(Item.getId(itemStack.getItem()), (byte) itemStack.getCount(), sourceBE.getBlockPos(), (byte) fromDirection.ordinal(), destinationBE.getBlockPos(), (byte) destinationDirection.ordinal(), (byte) extractPosition, (byte) insertPosition), level);

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

    /** TODO For the stocker mode **/
    public void getItems(ItemStack card, Direction direction) {
        /*IItemHandler adjacentInventory = getAttachedInventory(direction).orElse(EMPTY);
        if (adjacentInventory.getSlots() != 0) {
            //System.out.println("Getting for: " + getBlockPos().relative(direction));
        }*/
    }

    /** Called when changes happen - such as a card going into a side, or a card being modified via container **/
    public void updateThisNode() {
        //System.out.println("Updating node at: " + getBlockPos());
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

    private NonNullConsumer<LazyOptional<IItemHandler>> getInvalidator(SideConnection sideConnection) {
        return connectionInvalidator.computeIfAbsent(sideConnection, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
            if (te.facingHandler.get(sideConnection) == handler) {
                te.clearCachedInventories(sideConnection);
            }
        }));
    }


    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories(SideConnection sideConnection) {
        //System.out.println("Clearing: " + sideConnection.nodeSide.getName() + " - " + sideConnection.sneakySide.getName());
        this.facingHandler.remove(sideConnection);
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
