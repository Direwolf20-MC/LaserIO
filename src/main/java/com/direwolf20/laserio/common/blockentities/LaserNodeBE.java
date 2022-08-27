package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.client.particles.fluidparticle.FluidFlowParticleData;
import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.events.ServerTickHandler;
import com.direwolf20.laserio.common.items.cards.*;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterMod;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.*;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.bytes.Byte2BooleanMap;
import it.unimi.dsi.fastutil.bytes.Byte2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.direwolf20.laserio.util.MiscTools.findOffset;
import static net.minecraft.world.level.block.Block.UPDATE_ALL;

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
    public final NodeSideCache[] nodeSideCaches = new NodeSideCache[6];
    private final IItemHandler EMPTY = new ItemStackHandler(0);

    /** Adjacent Inventory Handlers **/
    private record SideConnection(Direction nodeSide, Direction sneakySide) {
    }

    /** BE and ItemHandler used for checking if a note/container is valid **/
    private record LaserNodeItemHandler(LaserNodeBE be, IItemHandler handler) {

    }

    private record LaserNodeFluidHandler(LaserNodeBE be, IFluidHandler handler) {

    }

    private record LaserNodeEnergyHandler(LaserNodeBE be, IEnergyStorage handler) {

    }

    public Map<ExtractorCardCache, Integer> roundRobinMap = new Object2IntOpenHashMap<>();

    private final Map<SideConnection, LazyOptional<IItemHandler>> facingHandlerItem = new HashMap<>();
    private final Map<SideConnection, NonNullConsumer<LazyOptional<IItemHandler>>> connectionInvalidatorItem = new HashMap<>();
    private final Map<SideConnection, LazyOptional<IFluidHandler>> facingHandlerFluid = new HashMap<>();
    private final Map<SideConnection, NonNullConsumer<LazyOptional<IFluidHandler>>> connectionInvalidatorFluid = new HashMap<>();
    private final Map<SideConnection, LazyOptional<IEnergyStorage>> facingHandlerEnergy = new HashMap<>();
    private final Map<SideConnection, NonNullConsumer<LazyOptional<IEnergyStorage>>> connectionInvalidatorEnergy = new HashMap<>();

    /** Variables for tracking and sending items/filters/etc **/
    private final Set<BlockPos> otherNodesInNetwork = new HashSet<>();
    private final List<InserterCardCache> inserterNodes = new CopyOnWriteArrayList<>(); //All Inventory nodes that contain an inserter card
    private final HashMap<ExtractorCardCache, HashMap<ItemStackKey, List<InserterCardCache>>> inserterCache = new HashMap<>();
    private final HashMap<ExtractorCardCache, HashMap<FluidStackKey, List<InserterCardCache>>> inserterCacheFluid = new HashMap<>();
    private final HashMap<ExtractorCardCache, List<InserterCardCache>> channelOnlyCache = new HashMap<>();
    private final List<ParticleRenderData> particleRenderData = new ArrayList<>();
    private final List<ParticleRenderDataFluid> particleRenderDataFluids = new ArrayList<>();
    private final Random random = new Random();

    private record StockerRequest(StockerCardCache stockerCardCache, ItemStackKey itemStackKey) {
    }

    private record StockerSource(InserterCardCache inserterCardCache, int slot) {
    }

    private final Map<StockerRequest, StockerSource> stockerDestinationCache = new HashMap<>();

    public boolean rendersChecked = false;
    public List<CardRender> cardRenders = new ArrayList<>();

    /** Redstone Variables **/
    public Byte2ByteMap redstoneNetwork = new Byte2ByteOpenHashMap(); //Channel,Strength
    public Byte2ByteMap myRedstoneIn = new Byte2ByteOpenHashMap();  //Channel,Strength

    public Byte2ByteMap myRedstoneOut = new Byte2ByteOpenHashMap();  //Side,Strength
    public Byte2BooleanMap redstoneCardSides = new Byte2BooleanOpenHashMap(); //Side and whether it has a redstone card, for client
    public boolean redstoneChecked = false;
    public boolean redstoneRefreshed = false;
    public boolean firstTimeNodeLoaded = true; //Redstone needs to be refreshed first time node loads into the world


    /** Misc Variables **/
    private boolean discoveredNodes = false; //The first time this block entity loads, it'll run discovery to refresh itself

    public LaserNodeBE(BlockPos pos, BlockState state) {
        super(Registration.LaserNode_BE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            final int j = direction.ordinal();
            com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler tempHandler = new com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler(LaserNodeContainer.SLOTS, this);
            nodeSideCaches[j] = new NodeSideCache(tempHandler, LazyOptional.of(() -> tempHandler), 0, new LaserEnergyStorage(direction));
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

    /** Build a list of extractor cards AND Stocker cards this node has in it, for looping through **/
    public void findMyExtractors() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            nodeSideCache.extractorCardCaches.clear();
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard && !(card.getItem() instanceof CardRedstone)) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        nodeSideCache.extractorCardCaches.add(new ExtractorCardCache(direction, card, slot, this));
                    }
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        nodeSideCache.extractorCardCaches.add(new StockerCardCache(direction, card, slot, this));
                    }
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.SENSOR)) {
                        nodeSideCache.extractorCardCaches.add(new SensorCardCache(direction, card, slot, this));
                    }
                }
            }
        }
    }

    /** Loop through all the extractorCards/stockerCards and run the extractions **/
    public void extract() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            int countCardsHandled = 0;
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                if (extractorCardCache instanceof SensorCardCache) continue; //Don't try to operate on SensorCards
                if (extractorCardCache.decrementSleep() == 0) {
                    if (!extractorCardCache.enabled) continue;
                    if (countCardsHandled > nodeSideCache.overClocker) continue;
                    if (extractorCardCache instanceof StockerCardCache stockerCardCache) {
                        if (extractorCardCache.cardType.equals(BaseCard.CardType.ITEM)) {
                            if (stockItems(stockerCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.FLUID)) {
                            if (stockFluids(stockerCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.ENERGY)) {
                            if (stockEnergy(stockerCardCache))
                                countCardsHandled++;
                        }
                    } else {
                        if (extractorCardCache.cardType.equals(BaseCard.CardType.ITEM)) {
                            if (sendItems(extractorCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.FLUID)) {
                            if (sendFluids(extractorCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.ENERGY)) {
                            if (sendEnergy(extractorCardCache))
                                countCardsHandled++;
                        }
                    }
                    if (extractorCardCache.remainingSleep <= 0) {
                        extractorCardCache.remainingSleep = extractorCardCache.tickSpeed;
                    }
                }
            }
        }
    }

    /** Loop through all the sensorCards and run the sensing **/
    public void sense() {
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            int countCardsHandled = 0;
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                if (!(extractorCardCache instanceof SensorCardCache))
                    continue; //Don't even try to operate on non-sensor cards
                if (extractorCardCache.decrementSleep() == 0) {
                    if (!extractorCardCache.enabled) continue;
                    if (countCardsHandled > nodeSideCache.overClocker) continue;
                    if (extractorCardCache instanceof SensorCardCache sensorCardCache) {
                        if (extractorCardCache.cardType.equals(BaseCard.CardType.ITEM)) {
                            if (senseItems(sensorCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.FLUID)) {
                            if (senseFluids(sensorCardCache))
                                countCardsHandled++;
                        } else if (extractorCardCache.cardType.equals(BaseCard.CardType.ENERGY)) {
                            if (senseEnergy(sensorCardCache))
                                countCardsHandled++;
                        }
                    }
                    if (extractorCardCache.remainingSleep <= 0) {
                        extractorCardCache.remainingSleep = extractorCardCache.tickSpeed;
                    }
                }
            }
        }
    }

    public void tickClient() {
        drawParticlesClient();
        particleRenderData.clear();
        particleRenderDataFluids.clear();
    }

    public void tickServer() {
        if (!discoveredNodes) { //On world / chunk reload, lets rediscover the network, including this block's extractor cards.
            discoverAllNodes();
            findMyExtractors();
            updateOverclockers();
            discoveredNodes = true;
        }
        sense();
        if (!redstoneChecked) {
            populateThisRedstoneNetwork(true);
            redstoneChecked = true;
        }
        if (!redstoneRefreshed) {
            refreshRedstoneNetwork();
            redstoneRefreshed = true;
        }
        extract(); //If this node has any extractors, do stuff with them
    }

    public void populateThisRedstoneNetwork(boolean notifyOthers) {
        //System.out.println("Checking redstone at: " + getBlockPos() + ", Gametime: " + level.getGameTime());
        //int myRedstoneCount = myRedstoneIn.size();
        //myRedstoneIn.clear();
        Byte2ByteMap myRedstoneInTemp = new Byte2ByteOpenHashMap();
        boolean updated = false;
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof CardRedstone && BaseCard.getTransferMode(card) == 0) { //Redstone mode and input mode
                    int redstoneStrength = level.getSignal(getBlockPos().relative(direction), direction);
                    //System.out.println("Input: " + getBlockPos() + ":" + direction + ":" + redstoneStrength);
                    if (redstoneStrength > 0) {
                        byte redstoneChannel = BaseCard.getRedstoneChannel(card);
                        //if (updateMyRedstoneIn(redstoneChannel, (byte) redstoneStrength))
                        //    updated = true;
                        if (myRedstoneInTemp.containsKey(redstoneChannel)) {
                            byte existingRedstoneStrength = myRedstoneInTemp.get(redstoneChannel);
                            if (redstoneStrength > existingRedstoneStrength) { //Only update the network if the new strength is bigger.
                                myRedstoneInTemp.put(redstoneChannel, (byte) redstoneStrength);
                            }
                        } else {
                            myRedstoneInTemp.put(redstoneChannel, (byte) redstoneStrength);
                        }
                    }
                }
            }
            for (Map.Entry<Byte, Byte> entry : nodeSideCache.myRedstoneFromSensors.byte2ByteEntrySet()) { //Update the temp variable with data from any sensors
                myRedstoneInTemp.put(entry.getKey(), entry.getValue());
            }
        }

        if (!myRedstoneInTemp.equals(myRedstoneIn)) {
            //System.out.println("Redstone input changed - updating network");
            updated = true;
            myRedstoneIn = new Byte2ByteOpenHashMap(myRedstoneInTemp);
        }
        if (updated && notifyOthers)
            notifyOtherNodesOfChange();
    }

    /** Visits all the notes in the network, and refreshes this redstone network cache from theirs **/
    public void refreshRedstoneNetwork() {
        //System.out.println("Updating Redstone Network at: " + getBlockPos() + ", Gametime: " + level.getGameTime());
        redstoneNetwork.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            LaserNodeBE laserNodeBE = getNodeAt(getWorldPos(pos));
            if (laserNodeBE == null) continue;
            for (Map.Entry<Byte, Byte> entry : laserNodeBE.myRedstoneIn.byte2ByteEntrySet()) {
                updateRedstoneNetwork(entry.getKey(), entry.getValue());
            }
        }
        updateRedstoneOutputs(); //Now that we know what the network should look like - update the outputs
        refreshCardsRedstone();
    }

    /** Goes through all the cards in this node, and updates their redstone state **/
    public void refreshCardsRedstone() {
        boolean inserterUpdated = false;
        boolean extractorUpdated = false;
        for (InserterCardCache inserterCardCache : inserterNodes) {
            if (inserterCardCache.be.getBlockPos().equals(getBlockPos())) {
                boolean tempEnabled = inserterCardCache.enabled;
                inserterCardCache.setEnabled();
                if (tempEnabled != inserterCardCache.enabled)
                    inserterUpdated = true;
            }
        }
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            nodeSideCache.invalidateEnergy();
            for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
                boolean tempEnabled = extractorCardCache.enabled;
                extractorCardCache.setEnabled();
                if (tempEnabled != extractorCardCache.enabled)
                    extractorUpdated = true;
            }
        }
        //if (inserterUpdated || extractorUpdated)
        markDirtyClient();
        if (inserterUpdated) {
            for (BlockPos pos : otherNodesInNetwork) {
                LaserNodeBE node = getNodeAt(getWorldPos(pos));
                if (node == null) continue;
                node.checkInvNode(this.getBlockPos(), true);
            }
        }
    }

    public byte getRedstoneChannelStrength(byte channel) {
        if (redstoneNetwork.containsKey(channel))
            return redstoneNetwork.get(channel);
        return 0;
    }

    public void updateRedstoneNetwork(byte redstoneChannel, byte redstoneStrength) {
        if (redstoneNetwork.containsKey(redstoneChannel)) {
            byte existingRedstoneStrength = redstoneNetwork.get(redstoneChannel);
            if (redstoneStrength > existingRedstoneStrength) //Only update the network if the new strength is bigger.
                this.redstoneNetwork.put(redstoneChannel, redstoneStrength);
        } else {
            this.redstoneNetwork.put(redstoneChannel, redstoneStrength);
        }
    }

    public boolean getRedstoneSideStrong(Direction direction) {
        byte side = (byte) direction.ordinal();
        if (!myRedstoneOut.containsKey(side)) return false;
        byte redstoneOut = myRedstoneOut.get(side);
        return redstoneOut > 15; //>15 means strong signal
    }

    public int getRedstoneSide(Direction direction) {
        byte side = (byte) direction.ordinal();
        if (!myRedstoneOut.containsKey(side)) return 0;
        byte redstoneOut = myRedstoneOut.get(side);
        return redstoneOut > 15 ? redstoneOut - 15 : redstoneOut; //>15 means strong signal
    }

    public void updateRedstoneOutputs() {
        //System.out.println("Checking Redstone Outputs at: " + getBlockPos());
        //myRedstoneOut.clear();
        Byte2ByteMap myRedstoneOutTemp = new Byte2ByteOpenHashMap();  //Side,Strength
        redstoneCardSides.clear();
        for (Direction direction : Direction.values()) {
            byte side = (byte) direction.ordinal();
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof CardRedstone && BaseCard.getTransferMode(card) == 1) { //Redstone mode and Output mode
                    redstoneCardSides.put((byte) direction.ordinal(), true);
                    byte cardChannel = BaseCard.getRedstoneChannel(card);
                    if (redstoneNetwork.containsKey(cardChannel)) { //Not in the list, so move on
                        byte redstoneStrength = redstoneNetwork.get(cardChannel);
                        //System.out.println("Output: " + getBlockPos() + ":" + direction + ":" + redstoneStrength);
                        if (redstoneStrength > 0) {
                            if (CardRedstone.getStrong(card))
                                redstoneStrength += 15;

                            if (myRedstoneOutTemp.containsKey(side)) {
                                byte existingRedstoneStrength = myRedstoneOutTemp.get(side);
                                if (redstoneStrength > existingRedstoneStrength) { //Only update the network if the new strength is bigger.
                                    myRedstoneOutTemp.put(side, redstoneStrength);
                                }
                            } else {
                                myRedstoneOutTemp.put(side, redstoneStrength);
                            }
                            //if (updateMyRedstoneOut((byte) direction.ordinal(), redstoneStrength)) ;
                        }
                    }
                } else if (card.getItem() instanceof CardRedstone && BaseCard.getTransferMode(card) == 0) { //Redstone mode and Output mode
                    redstoneCardSides.put((byte) direction.ordinal(), true);
                }
            }
            if (firstTimeNodeLoaded || !Objects.equals(myRedstoneOutTemp.get(side), myRedstoneOut.get(side))) {
                if (myRedstoneOutTemp.containsKey(side))
                    myRedstoneOut.put(side, myRedstoneOutTemp.get(side));
                else
                    myRedstoneOut.remove(side);
                level.neighborChanged(getBlockPos().relative(direction), this.getBlockState().getBlock(), getBlockPos());
                level.updateNeighborsAtExceptFromFacing(getBlockPos().relative(direction), this.getBlockState().getBlock(), direction.getOpposite());

            }
        }
        BlockState state = this.getBlockState();
        state.updateNeighbourShapes(level, getBlockPos(), UPDATE_ALL);
        if (firstTimeNodeLoaded)
            firstTimeNodeLoaded = false;
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
                                && (p.enabled)
                                && (p.isStackValidForCard(stack))
                                && (p.cardType.equals(extractorCardCache.cardType))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction) && p.sneaky == extractorCardCache.sneaky)))
                        .toList();
                inserterCache.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else { //Find the list of items that can be extracted by this extractor and cache them along with the extractor card
            List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.enabled)
                            && (p.isStackValidForCard(stack))
                            && (p.cardType.equals(extractorCardCache.cardType))
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction) && p.sneaky == extractorCardCache.sneaky)))
                    .toList();
            HashMap<ItemStackKey, List<InserterCardCache>> tempMap = new HashMap<>();
            tempMap.put(key, nodes);
            inserterCache.put(extractorCardCache, tempMap);
            return nodes;
        }
    }

    /** Finds all inserters that can be extracted to **/
    public List<InserterCardCache> getPossibleInserters(ExtractorCardCache extractorCardCache, FluidStack stack) {
        FluidStackKey key = new FluidStackKey(stack, true);
        if (inserterCacheFluid.containsKey(extractorCardCache)) { //If this extractor card is already in the cache
            if (inserterCacheFluid.get(extractorCardCache).containsKey(key)) //If this extractor card AND itemKey are already in the cache
                return inserterCacheFluid.get(extractorCardCache).get(key); //Return the cached results
            else { //Find the list of items that can be extracted by this extractor and cache them
                List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                                && (p.enabled)
                                && (p.isStackValidForCard(stack))
                                && (p.cardType.equals(extractorCardCache.cardType))
                                && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                        .toList();
                inserterCacheFluid.get(extractorCardCache).put(key, nodes);
                return nodes;
            }
        } else { //Find the list of items that can be extracted by this extractor and cache them along with the extractor card
            List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.enabled)
                            && (p.isStackValidForCard(stack))
                            && (p.cardType.equals(extractorCardCache.cardType))
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction))))
                    .toList();
            HashMap<FluidStackKey, List<InserterCardCache>> tempMap = new HashMap<>();
            tempMap.put(key, nodes);
            inserterCacheFluid.put(extractorCardCache, tempMap);
            return nodes;
        }
    }

    /** Finds all inserters that match the channel (Used for stockers) **/
    public List<InserterCardCache> getChannelMatchInserters(ExtractorCardCache extractorCardCache) {
        if (channelOnlyCache.containsKey(extractorCardCache)) {
            return channelOnlyCache.get(extractorCardCache);
        } else {
            List<InserterCardCache> nodes = inserterNodes.stream().filter(p -> (p.channel == extractorCardCache.channel)
                            && (p.enabled)
                            && (!(p.relativePos.equals(BlockPos.ZERO) && p.direction.equals(extractorCardCache.direction) && (p.cardType.equals(extractorCardCache.cardType)))))
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

    public int getNextRR(ExtractorCardCache extractorCardCache, List<InserterCardCache> inserterCardCaches) {
        int nextRR;
        if (roundRobinMap.containsKey(extractorCardCache)) {
            int currentRR = roundRobinMap.get(extractorCardCache);
            nextRR = currentRR + 1 >= inserterCardCaches.size() ? 0 : currentRR + 1;
        } else {
            nextRR = 0;
        }
        roundRobinMap.put(extractorCardCache, nextRR);
        return nextRR;
    }

    public int getRR(ExtractorCardCache extractorCardCache) {
        if (roundRobinMap.containsKey(extractorCardCache)) {
            return roundRobinMap.get(extractorCardCache);
        } else {
            roundRobinMap.put(extractorCardCache, 0);
            return 0;
        }
    }

    public List<InserterCardCache> applyRR(ExtractorCardCache extractorCardCache, List<InserterCardCache> inserterCardCaches, int nextRR) {
        List<List<InserterCardCache>> lists = new ArrayList<>(
                inserterCardCaches.stream()
                        .collect(Collectors.partitioningBy(
                                s -> inserterCardCaches.indexOf(s) >= nextRR))
                        .values());
        lists.get(1).addAll(lists.get(0));
        return lists.get(1);
    }

    public boolean extractItem(ExtractorCardCache extractorCardCache, IItemHandler fromInventory, ItemStack extractStack) {
        TransferResult extractResults = (ItemHandlerUtil.extractItemWithSlots(this, fromInventory, extractStack, extractStack.getCount(), true, true, extractorCardCache)); //Fake Extract
        int amtNeeded = extractResults.getTotalItemCounts();
        boolean exactMode = extractorCardCache.exact;
        if (amtNeeded != extractorCardCache.extractAmt && exactMode) //Return if we didn't get what we needed and we are in exact mode
            return false;
        extractStack.setCount(amtNeeded);
        TransferResult insertResults = new TransferResult();

        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int roundRobin = -1;

        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        //Begin test inserting into inserters
        int amtStillNeeded = amtNeeded;
        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeItemHandler laserNodeItemHandler = getLaserNodeHandlerItem(inserterCardCache);
            if (laserNodeItemHandler == null) continue;

            TransferResult thisResult = ItemHandlerUtil.insertItemWithSlots(laserNodeItemHandler.be, laserNodeItemHandler.handler, extractStack, 0, true, extractorCardCache.isCompareNBT, true, inserterCardCache); //Test!!
            if (extractorCardCache.roundRobin == 2 && thisResult.getTotalItemCounts() < amtStillNeeded) {
                return false;
            }
            if (thisResult.results.isEmpty()) { //Next inserter if nothing went in -- return false if enforcing round robin
                getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            insertResults.addResult(thisResult);

            insertResults.remainingStack = ItemStack.EMPTY; //We don't really care about this
            int amtFit = thisResult.getTotalItemCounts(); //How many items fit (Above)
            amtStillNeeded -= amtFit;
            if (amtStillNeeded == 0)
                break;
            extractStack.setCount(amtStillNeeded); //Modify the stack size rather than .copy
        }

        if (amtStillNeeded == amtNeeded || (amtStillNeeded != 0 && exactMode))
            return false; //If we are in exact mode, make sure we fit exactly what we need, otherwise see if we fit anything
        //If we get to this point, it means we can insert all the itemstacks we wanted to, so lets do it for realsies
        extractStack.setCount(amtNeeded - amtStillNeeded); //Set back to how many we actually need
        for (TransferResult.Result result : insertResults.results) {
            ItemStack tempStack = extractStack.split(result.itemStack.getCount());
            ItemStack returnedStack = result.insertHandler.insertItem(result.insertSlot, tempStack, true);
            if (!returnedStack.isEmpty())
                break; //We tested all these, so this should be empty, unless something weird happened
            int amtToExtract = tempStack.getCount();
            ItemStack extractedStack;
            for (TransferResult.Result extractResult : extractResults.results) {
                int amtToExtractThis = Math.min(amtToExtract, extractResult.itemStack.getCount());
                extractedStack = extractResult.extractHandler.extractItem(extractResult.extractSlot, amtToExtractThis, false);
                if (extractResult.itemStack.getCount() == extractedStack.getCount())
                    extractResults.results.remove(extractResult); //If the extract result is now empty, remove it
                else
                    extractResult.itemStack.split(extractedStack.getCount()); //Otherwise, remove the amount we got from the stack
                amtToExtract -= extractedStack.getCount();
                if (amtToExtract == 0) break;
            }
            result.insertHandler.insertItem(result.insertSlot, tempStack, false);
            if (result.inserterCardCache != null)
                drawParticles(tempStack, extractorCardCache.direction, this, result.toBE, result.inserterCardCache.direction, extractorCardCache.cardSlot, result.inserterCardCache.cardSlot);
        }

        if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);

        return true;
    }

    public boolean updateRedstoneFromSensor(boolean filterMatched, byte redstoneChannel, NodeSideCache nodeSideCache) {
        byte currentRedstoneFromNetwork = nodeSideCache.myRedstoneFromSensors.get(redstoneChannel);
        byte newRedstoneStrength = filterMatched ? (byte) 15 : (byte) 0;
        if (newRedstoneStrength == 0) {
            nodeSideCache.myRedstoneFromSensors.remove(redstoneChannel);
        } else {
            nodeSideCache.myRedstoneFromSensors.put(redstoneChannel, newRedstoneStrength);
        }
        if (currentRedstoneFromNetwork != newRedstoneStrength) {
            return true;
        }
        return false; //No changes were needed
    }

    public boolean senseItems(SensorCardCache sensorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(sensorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        ItemStack filter = sensorCardCache.filterCard;
        boolean andMode = BaseCard.getAnd(sensorCardCache.cardItem);
        boolean filterMatched = false;
        NodeSideCache nodeSideCache = nodeSideCaches[sensorCardCache.direction.ordinal()];
        if (filter.isEmpty()) { //Needs a filter
            if (updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                rendersChecked = false;
                clearCachedInventories();
                redstoneChecked = false;
            }
            return false;
        }

        IItemHandler adjacentInventory = getAttachedInventory(sensorCardCache.direction, sensorCardCache.sneaky).orElse(EMPTY);
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(adjacentInventory, sensorCardCache.isCompareNBT);

        if (filter.getItem() instanceof FilterMod) {
            List<ItemStack> filteredItemsListOriginal = sensorCardCache.filteredItems;
            List<ItemStack> filteredItemsList = new ArrayList<>(filteredItemsListOriginal);
            List<ItemStack> itemStacksInChest = inventoryCounts.getItemCounts().values().stream().toList();
            outloop:
            for (ItemStack stack : itemStacksInChest) {
                for (ItemStack testStack : filteredItemsListOriginal) {
                    if (stack.getItem().getCreatorModId(stack).equals(testStack.getItem().getCreatorModId(testStack))) {
                        filteredItemsList.remove(testStack);
                        if (!andMode) {
                            break outloop;
                        }
                    }
                }
            }
            //In and mode, the list of tags needs to be empty, in or mode it just has to be 1 smaller.
            if (andMode)
                filterMatched = filteredItemsList.size() == 0;
            else
                filterMatched = filteredItemsList.size() < filteredItemsListOriginal.size();
        } else if (filter.getItem() instanceof FilterBasic) {
            List<ItemStack> filteredItemsList = sensorCardCache.filteredItems;
            boolean allMatched = true;
            for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
                int amtHad = inventoryCounts.getCount(itemStack);
                if (amtHad > 0) {
                    if (!andMode) {
                        filterMatched = true;
                        break;
                    }
                } else {
                    if (andMode) {
                        allMatched = false;
                        break;
                    }
                }
            }
            if (andMode && !filteredItemsList.isEmpty()) {
                filterMatched = allMatched;
            }
        } else if (filter.getItem() instanceof FilterCount) {
            List<ItemStack> filteredItemsList = sensorCardCache.filteredItems;
            boolean allMatched = true;
            for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
                int amtHad = inventoryCounts.getCount(itemStack);
                if (amtHad < itemStack.getCount() || (sensorCardCache.exact && amtHad > itemStack.getCount())) {
                    if (andMode) {
                        allMatched = false;
                        break;
                    }

                } else {
                    if (!andMode) {
                        filterMatched = true;
                        break;
                    }
                }
            }
            if (andMode && !filteredItemsList.isEmpty()) {
                filterMatched = allMatched;
            }
        } else if (filter.getItem() instanceof FilterTag) {
            List<String> tags = sensorCardCache.filterTags;
            int tagsToMatch = tags.size();
            List<ItemStack> itemStacksInChest = inventoryCounts.getItemCounts().values().stream().toList();
            outloop:
            for (ItemStack itemStack : itemStacksInChest) {
                for (TagKey tagKey : itemStack.getItem().builtInRegistryHolder().tags().toList()) {
                    String itemTag = tagKey.location().toString().toLowerCase(Locale.ROOT);
                    if (tags.contains(itemTag)) {
                        tags.remove(itemTag);
                        if (!andMode) {
                            break outloop;
                        }
                    }
                }
            }
            //In and mode, the list of tags needs to be empty, in or mode it just has to be 1 smaller.
            if (andMode)
                filterMatched = tags.size() == 0;
            else
                filterMatched = tags.size() < tagsToMatch;
        }

        if (updateRedstoneFromSensor(filterMatched, sensorCardCache.redstoneChannel, nodeSideCache)) {
            //System.out.println("Redstone network change detected");
            rendersChecked = false;
            clearCachedInventories();
            redstoneChecked = false;
        }
        return true;
    }

    public boolean senseFluids(SensorCardCache sensorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(sensorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        NodeSideCache nodeSideCache = nodeSideCaches[sensorCardCache.direction.ordinal()];
        Optional<IFluidHandler> adjacentTankOptional = getAttachedFluidTank(sensorCardCache.direction, sensorCardCache.sneaky).resolve();
        if (adjacentTankOptional.isEmpty()) { //Needs a filter
            if (updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                rendersChecked = false;
                clearCachedInventories();
                redstoneChecked = false;
            }
            return false;
        }
        IFluidHandler adacentTank = adjacentTankOptional.get();

        ItemStack filter = sensorCardCache.filterCard;
        boolean andMode = BaseCard.getAnd(sensorCardCache.cardItem);
        boolean filterMatched = false;

        if (filter.isEmpty()) { //Needs a filter
            if (updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                rendersChecked = false;
                clearCachedInventories();
                redstoneChecked = false;
            }
            return false;
        }
        if (filter.getItem() instanceof FilterBasic) {
            List<FluidStack> filteredFluids = sensorCardCache.getFilteredFluids();
            List<FluidStack> filteredFluidsOriginal = new ArrayList<>(filteredFluids);

            outloop:
            for (FluidStack fluidStack : filteredFluidsOriginal) {
                for (int tank = 0; tank < adacentTank.getTanks(); tank++) { //Loop through all the tanks
                    FluidStack stackInTank = adacentTank.getFluidInTank(tank);
                    if (stackInTank.isFluidEqual(fluidStack)) {
                        filteredFluids.remove(fluidStack);
                        if (!andMode) {
                            break outloop;
                        }
                    }
                }
            }
            if (andMode)
                filterMatched = filteredFluids.size() == 0;
            else
                filterMatched = filteredFluids.size() < filteredFluidsOriginal.size();
        } else if (filter.getItem() instanceof FilterCount) {
            List<FluidStack> filteredFluids = sensorCardCache.getFilteredFluids();
            List<FluidStack> filteredFluidsOriginal = new ArrayList<>(filteredFluids);

            outloop:
            for (FluidStack fluidStack : filteredFluidsOriginal) {
                int desiredAmt = sensorCardCache.getFilterAmt(fluidStack);
                for (int tank = 0; tank < adacentTank.getTanks(); tank++) { //Loop through all the tanks
                    FluidStack stackInTank = adacentTank.getFluidInTank(tank);
                    if (stackInTank.isFluidEqual(fluidStack)) {
                        int amtHad = stackInTank.getAmount();
                        if (amtHad < desiredAmt || (sensorCardCache.exact && amtHad > desiredAmt)) {
                            //noOp
                        } else {
                            filteredFluids.remove(fluidStack);
                            if (!andMode) {
                                break outloop;
                            }
                        }
                    }
                }
            }
            if (andMode)
                filterMatched = filteredFluids.size() == 0;
            else
                filterMatched = filteredFluids.size() < filteredFluidsOriginal.size();
        } else if (filter.getItem() instanceof FilterTag) {
            List<String> tags = sensorCardCache.getFilterTags();
            int tagsToMatch = tags.size();

            outloop:
            for (int tank = 0; tank < adacentTank.getTanks(); tank++) { //Loop through all the tanks
                FluidStack stackInTank = adacentTank.getFluidInTank(tank);
                for (TagKey tagKey : stackInTank.getFluid().builtInRegistryHolder().tags().toList()) {
                    String fluidTag = tagKey.location().toString().toLowerCase(Locale.ROOT);
                    if (tags.contains(fluidTag)) {
                        tags.remove(fluidTag);
                        if (!andMode) {
                            break outloop;
                        }
                    }
                }
            }
            //In and mode, the list of tags needs to be empty, in or mode it just has to be 1 smaller.
            if (andMode)
                filterMatched = tags.size() == 0;
            else
                filterMatched = tags.size() < tagsToMatch;
        }
        if (updateRedstoneFromSensor(filterMatched, sensorCardCache.redstoneChannel, nodeSideCache)) {
            //System.out.println("Redstone network change detected");
            rendersChecked = false;
            clearCachedInventories();
            redstoneChecked = false;
        }
        return true;
    }

    public boolean senseEnergy(SensorCardCache sensorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(sensorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Optional<IEnergyStorage> adjacentEnergyOptional = getAttachedEnergyTank(sensorCardCache.direction, sensorCardCache.sneaky).resolve();
        NodeSideCache nodeSideCache = nodeSideCaches[sensorCardCache.direction.ordinal()];
        if (adjacentEnergyOptional.isEmpty()) { //Needs a filter
            if (updateRedstoneFromSensor(false, sensorCardCache.redstoneChannel, nodeSideCache)) {
                rendersChecked = false;
                clearCachedInventories();
                redstoneChecked = false;
            }
            return false;
        }
        IEnergyStorage adjacentEnergy = adjacentEnergyOptional.get();
        boolean filterMatched = false;
        int desired = (int) (adjacentEnergy.getMaxEnergyStored() * ((float) sensorCardCache.insertLimit / 100));
        int amtHad = adjacentEnergy.getEnergyStored();
        if (amtHad < desired || (sensorCardCache.exact && amtHad > desired)) {
            filterMatched = false;
        } else {
            filterMatched = true;
        }
        if (updateRedstoneFromSensor(filterMatched, sensorCardCache.redstoneChannel, nodeSideCache)) {
            //System.out.println("Redstone network change detected");
            rendersChecked = false;
            clearCachedInventories();
            redstoneChecked = false;
        }
        return true;
    }

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendItems(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(extractorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        IItemHandler adjacentInventory = getAttachedInventory(extractorCardCache.direction, extractorCardCache.sneaky).orElse(EMPTY);
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts();
        if (extractorCardCache.filterCard.getItem() instanceof FilterCount) {
            inventoryCounts = new ItemHandlerUtil.InventoryCounts(adjacentInventory, extractorCardCache.isCompareNBT);
        }
        for (int slot = 0; slot < adjacentInventory.getSlots(); slot++) {
            ItemStack stackInSlot = adjacentInventory.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || !(extractorCardCache.isStackValidForCard(stackInSlot))) continue;
            ItemStack extractStack = stackInSlot.copy();
            extractStack.setCount(extractorCardCache.extractAmt);

            if (extractorCardCache.filterCard.getItem() instanceof FilterCount) { //If this is a count filter, only try to extract up to the amount in the filter
                int filterCount = extractorCardCache.getFilterAmt(extractStack);
                if (filterCount <= 0) continue; //This should never happen in theory...
                int amtInInv = inventoryCounts.getCount(extractStack);
                int amtAllowedToRemove = amtInInv - filterCount;
                if (amtAllowedToRemove <= 0) continue;
                int amtRemaining = Math.min(extractStack.getCount(), amtAllowedToRemove);
                extractStack.setCount(amtRemaining);
            }
            if (extractItem(extractorCardCache, adjacentInventory, extractStack))
                return true;
        }
        return false;
    }

    public boolean extractFluidStack(ExtractorCardCache extractorCardCache, IFluidHandler fromInventory, FluidStack extractStack) {
        int totalAmtNeeded = extractStack.getAmount();
        int amtToExtract = extractStack.getAmount();
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int roundRobin = -1;
        boolean foundAnything = false;
        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeFluidHandler laserNodeFluidHandler = getLaserNodeHandlerFluid(inserterCardCache);
            if (laserNodeFluidHandler == null) continue;
            IFluidHandler handler = laserNodeFluidHandler.handler;
            //for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
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
            }
            if (amtToExtract == 0) {
                amtToExtract = totalAmtNeeded;
                continue;
            }
            extractStack.setAmount(amtToExtract);
            int amtFit = handler.fill(extractStack, IFluidHandler.FluidAction.SIMULATE);
            if (amtFit == 0) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            extractStack.setAmount(amtFit);
            FluidStack drainedStack = fromInventory.drain(extractStack, IFluidHandler.FluidAction.EXECUTE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            foundAnything = true;
            handler.fill(drainedStack, IFluidHandler.FluidAction.EXECUTE);
            drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
            totalAmtNeeded -= drainedStack.getAmount();
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return true;
        }

        return foundAnything;
    }

    public boolean extractFluidStackExact(ExtractorCardCache extractorCardCache, IFluidHandler fromInventory, FluidStack extractStack) {
        int totalAmtNeeded = extractStack.getAmount();
        int amtToExtract = extractStack.getAmount();
        List<InserterCardCache> inserterCardCaches = getPossibleInserters(extractorCardCache, extractStack);
        int roundRobin = -1;

        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        Map<InserterCardCache, Integer> insertHandlers = new Object2IntOpenHashMap<>();

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeFluidHandler laserNodeFluidHandler = getLaserNodeHandlerFluid(inserterCardCache);
            if (laserNodeFluidHandler == null) continue;
            IFluidHandler handler = laserNodeFluidHandler.handler;
            if (inserterCardCache.filterCard.getItem() instanceof FilterCount) {
                int filterCount = inserterCardCache.getFilterAmt(extractStack);
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    FluidStack fluidStack = handler.getFluidInTank(tank);
                    if (fluidStack.isEmpty() || fluidStack.isFluidEqual(extractStack)) {
                        int currentAmt = fluidStack.getAmount();
                        int neededAmt = filterCount - currentAmt;
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
            int amtFit = handler.fill(extractStack, IFluidHandler.FluidAction.SIMULATE);
            if (amtFit == 0) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            extractStack.setAmount(amtFit);
            FluidStack drainedStack = fromInventory.drain(extractStack, IFluidHandler.FluidAction.SIMULATE);
            if (drainedStack.isEmpty()) continue; //If we didn't get anything for whatever reason
            insertHandlers.put(inserterCardCache, drainedStack.getAmount()); //Add the handler to the list of handlers we found fluid in
            totalAmtNeeded -= drainedStack.getAmount(); //Keep track of how much we have left to insert
            amtToExtract = totalAmtNeeded;
            if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) break;
        }

        if (totalAmtNeeded > 0) return false;

        for (Map.Entry<InserterCardCache, Integer> entry : insertHandlers.entrySet()) {
            InserterCardCache inserterCardCache = entry.getKey();
            LaserNodeFluidHandler laserNodeFluidHandler = getLaserNodeHandlerFluid(inserterCardCache);
            IFluidHandler handler = laserNodeFluidHandler.handler;
            extractStack.setAmount(entry.getValue());
            FluidStack drainedStack = fromInventory.drain(extractStack, IFluidHandler.FluidAction.EXECUTE);
            handler.fill(drainedStack, IFluidHandler.FluidAction.EXECUTE);
            drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        return true;
    }

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendFluids(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(extractorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        LazyOptional<IFluidHandler> adjacentTankOptional = getAttachedFluidTank(extractorCardCache.direction, extractorCardCache.sneaky);
        if (!adjacentTankOptional.isPresent()) return false;
        IFluidHandler adjacentTank = adjacentTankOptional.resolve().get();
        for (int tank = 0; tank < adjacentTank.getTanks(); tank++) {
            FluidStack fluidStack = adjacentTank.getFluidInTank(tank);
            if (fluidStack.isEmpty() || !extractorCardCache.isStackValidForCard(fluidStack)) continue;
            FluidStack extractStack = fluidStack.copy();
            extractStack.setAmount(extractorCardCache.extractAmt);

            if (extractorCardCache.filterCard.getItem() instanceof FilterCount) { //If this is a count filter, only try to extract up to the amount in the filter
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
            }


        }
        return false;
    }

    public int receiveEnergy(Direction direction, int receiveAmt, boolean simulate) {
        int totalAmtNeeded = receiveAmt;
        int totalAmtSent = 0;

        NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
        int countCardsHandled = 0;
        for (ExtractorCardCache extractorCardCache : nodeSideCache.extractorCardCaches) {
            if (!extractorCardCache.enabled) continue;
            if (countCardsHandled > nodeSideCache.overClocker) return totalAmtSent;
            if (extractorCardCache instanceof StockerCardCache) {
                //No-Op
            } else {
                if (extractorCardCache.cardType.equals(BaseCard.CardType.ENERGY)) {
                    int amtSent = sendReceivedEnergy(extractorCardCache, totalAmtNeeded, simulate);
                    if (amtSent > 0)
                        countCardsHandled++;
                    totalAmtNeeded -= amtSent;
                    totalAmtSent += amtSent;
                    if (totalAmtNeeded <= 0) break;
                }
            }
        }
        return totalAmtSent;
    }

    public int sendReceivedEnergy(ExtractorCardCache extractorCardCache, int receiveAmt, boolean simulate) {
        int totalAmtNeeded = Math.min(extractorCardCache.extractAmt, receiveAmt);
        int totalFit = 0;
        List<InserterCardCache> inserterCardCaches = getChannelMatchInserters(extractorCardCache);
        int roundRobin = -1;
        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            if (laserNodeEnergyHandler == null) continue;
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;
            int desired;
            if (inserterCardCache.insertLimit != 100)
                desired = (int) (energyStorage.getMaxEnergyStored() * ((float) inserterCardCache.insertLimit / 100)) - energyStorage.getEnergyStored();
            else
                desired = receiveAmt;
            if (desired <= 0) continue;
            int amtToTry = Math.min(desired, totalAmtNeeded);
            int amtFit = energyStorage.receiveEnergy(amtToTry, true); //Simulate Insert Energy
            if (amtFit == 0) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return totalFit;
                }
                if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            //int amtDrained = fromEnergyTank.extractEnergy(amtFit, false); //Remove some energy from the extract tank
            //if (amtDrained == 0) continue; //If we didn't get anything, like the energy storage is empty
            //foundAnything = true;
            totalAmtNeeded -= amtFit; //If we removed 100 and wanted to remove 1000, keep looking for other nodes to insert into
            totalFit += amtFit;
            if (!simulate)
                energyStorage.receiveEnergy(amtFit, false); //Insert the amount we removed from the source
            //drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);

            if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return totalFit;
        }
        return totalFit;
    }

    public boolean extractEnergy(ExtractorCardCache extractorCardCache, IEnergyStorage fromEnergyTank, int extractAmt) {
        int totalAmtNeeded = extractAmt;
        List<InserterCardCache> inserterCardCaches = getChannelMatchInserters(extractorCardCache);
        int roundRobin = -1;
        boolean foundAnything = false;
        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            if (laserNodeEnergyHandler == null) continue;
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;
            int desired;
            if (inserterCardCache.insertLimit != 100)
                desired = (int) (energyStorage.getMaxEnergyStored() * ((float) inserterCardCache.insertLimit / 100)) - energyStorage.getEnergyStored();
            else
                desired = extractAmt;
            if (desired <= 0) continue;
            int amtToTry = Math.min(desired, totalAmtNeeded);
            int amtFit = energyStorage.receiveEnergy(amtToTry, true); //Simulate Insert Energy
            if (amtFit == 0) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            int amtDrained = fromEnergyTank.extractEnergy(amtFit, false); //Remove some energy from the extract tank
            if (amtDrained == 0) continue; //If we didn't get anything, like the energy storage is empty
            foundAnything = true;
            energyStorage.receiveEnergy(amtDrained, false); //Insert the amount we removed from the source
            //drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
            totalAmtNeeded -= amtDrained; //If we removed 100 and wanted to remove 1000, keep looking for other nodes to insert into
            if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) return true;
        }
        return foundAnything;
    }

    public boolean extractEnergyExact(ExtractorCardCache extractorCardCache, IEnergyStorage fromEnergyTank, int extractAmt) {
        int totalAmtNeeded = extractAmt;
        List<InserterCardCache> inserterCardCaches = getChannelMatchInserters(extractorCardCache);
        int roundRobin = -1;

        if (extractorCardCache.roundRobin != 0) {
            roundRobin = getRR(extractorCardCache);
            inserterCardCaches = applyRR(extractorCardCache, inserterCardCaches, roundRobin);
        }

        Map<InserterCardCache, Integer> insertHandlers = new Object2IntOpenHashMap<>();

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            if (laserNodeEnergyHandler == null) continue;
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;
            int desired;
            if (inserterCardCache.insertLimit != 100)
                desired = (int) (energyStorage.getMaxEnergyStored() * ((float) inserterCardCache.insertLimit / 100)) - energyStorage.getEnergyStored();
            else
                desired = extractAmt;
            if (desired <= 0) continue;
            int amtToTry = Math.min(desired, totalAmtNeeded);
            int amtFit = energyStorage.receiveEnergy(amtToTry, true); //Simulate Insert
            if (amtFit == 0) { //Next inserter if nothing went in -- return false if enforcing round robin
                if (extractorCardCache.roundRobin == 2) {
                    return false;
                }
                if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
                continue;
            }
            int amtDrained = fromEnergyTank.extractEnergy(amtFit, true); //Simulate Remove some energy
            if (amtDrained == 0) continue; //If we didn't get anything
            insertHandlers.put(inserterCardCache, amtDrained); //Add the handler to the list of handlers we found fluid in
            totalAmtNeeded -= amtDrained; //Keep track of how much we have left to insert
            if (extractorCardCache.roundRobin != 0) getNextRR(extractorCardCache, inserterCardCaches);
            if (totalAmtNeeded == 0) break;
        }

        if (totalAmtNeeded > 0) return false;

        for (Map.Entry<InserterCardCache, Integer> entry : insertHandlers.entrySet()) {
            InserterCardCache inserterCardCache = entry.getKey();
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;
            int actualRemoved = fromEnergyTank.extractEnergy(entry.getValue(), false);
            energyStorage.receiveEnergy(actualRemoved, false);
            //drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        return true;
    }

    /** Extractor Cards call this, and try to find an inserter card to send their items to **/
    public boolean sendEnergy(ExtractorCardCache extractorCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(extractorCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Optional<IEnergyStorage> adjacentEnergyOptional = getAttachedEnergyTank(extractorCardCache.direction, extractorCardCache.sneaky).resolve();
        if (adjacentEnergyOptional.isEmpty()) return false;
        IEnergyStorage adjacentEnergy = adjacentEnergyOptional.get();
        int desired = (int) (adjacentEnergy.getMaxEnergyStored() * ((float) extractorCardCache.extractLimit / 100));
        int extractAmt = Math.min(extractorCardCache.extractAmt, adjacentEnergy.getEnergyStored() - desired);
        if (extractAmt <= 0) return false;
        if (extractorCardCache.exact) {
            return extractEnergyExact(extractorCardCache, adjacentEnergy, extractAmt);
        } else {
            return extractEnergy(extractorCardCache, adjacentEnergy, extractAmt);
        }
    }

    public boolean canAnyItemFiltersFit(IItemHandler adjacentInventory, StockerCardCache stockerCardCache) {
        for (ItemStack stack : stockerCardCache.getFilteredItems()) {
            int amountFit = testInsertToInventory(adjacentInventory, stack.split(1)); //Try to put one in - if it fits we have room
            if (amountFit > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean canAnyFluidFiltersFit(IFluidHandler adjacentTank, StockerCardCache stockerCardCache) {
        for (FluidStack fluidStack : stockerCardCache.getFilteredFluids()) {
            int amtFit = adjacentTank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
            if (amtFit > 0)
                return true;
        }
        return false;
    }

    public boolean canFluidFitInTank(IFluidHandler handler, FluidStack fluidStack) {
        return (handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) > 0);
    }

    public boolean regulateItemStocker(StockerCardCache stockerCardCache, IItemHandler stockerInventory) {
        ItemHandlerUtil.InventoryCounts stockerInventoryCount = new ItemHandlerUtil.InventoryCounts(stockerInventory, stockerCardCache.isCompareNBT);
        List<ItemStack> filteredItemsList = stockerCardCache.getFilteredItems();
        for (ItemStack itemStack : filteredItemsList) { //Remove all the items from the list that we already have enough of
            int amtHad = stockerInventoryCount.getCount(itemStack);
            if (amtHad > itemStack.getCount()) { //if we have enough, move onto the next stack after removing this one from the list
                ItemStack extractStack = itemStack.copy();
                extractStack.setCount(Math.min(amtHad - itemStack.getCount(), stockerCardCache.extractAmt));
                if (extractItem(stockerCardCache, stockerInventory, extractStack))
                    return true;
            }
        }
        return false;
    }

    public boolean regulateFluidStocker(StockerCardCache stockerCardCache, IFluidHandler stockerTank) {
        List<FluidStack> filteredFluidsList = stockerCardCache.getFilteredFluids();
        for (FluidStack fluidStack : filteredFluidsList) { //Iterate the list of filtered items for extracting purposes
            int desiredAmt = stockerCardCache.getFilterAmt(fluidStack);
            int amtHad = 0;
            for (int tank = 0; tank < stockerTank.getTanks(); tank++) { //Loop through all the tanks
                FluidStack stackInTank = stockerTank.getFluidInTank(tank);
                if (stackInTank.isFluidEqual(fluidStack))
                    amtHad += stackInTank.getAmount();
            }
            if (amtHad > desiredAmt) { //If we have too much of this fluid, remove the difference.
                fluidStack.setAmount(Math.min(amtHad - desiredAmt, stockerCardCache.extractAmt));
                if (extractFluidStack(stockerCardCache, stockerTank, fluidStack))
                    return true;
            }
        }
        return false;
    }

    public boolean regulateEnergyStocker(StockerCardCache stockerCardCache, IEnergyStorage stockerTank) {
        int desired = (int) (stockerTank.getMaxEnergyStored() * ((float) stockerCardCache.insertLimit / 100));
        if (desired >= stockerTank.getEnergyStored()) return false;
        int overFlow = Math.min(stockerCardCache.extractAmt, stockerTank.getEnergyStored() - desired);
        return extractEnergy(stockerCardCache, stockerTank, overFlow);
    }

    /** Stocker Cards call this, and try to find an inserter card to pull their fluids from **/
    public boolean stockEnergy(StockerCardCache stockerCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(stockerCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Optional<IEnergyStorage> adjacentEnergyOptional = getAttachedEnergyTank(stockerCardCache.direction, stockerCardCache.sneaky).resolve();
        if (adjacentEnergyOptional.isEmpty()) return false;
        IEnergyStorage adjacentEnergy = adjacentEnergyOptional.get();

        if (stockerCardCache.regulate) {
            if (regulateEnergyStocker(stockerCardCache, adjacentEnergy))
                return true;
        }
        int desired = (int) (adjacentEnergy.getMaxEnergyStored() * ((float) stockerCardCache.insertLimit / 100));
        if (adjacentEnergy.getEnergyStored() >= desired) {
            return false; //If we can't fit any more energy into here
        }
        return findEnergyForStocker(stockerCardCache, adjacentEnergy);
    }

    /** Stocker Cards call this, and try to find an inserter card to pull their fluids from **/
    public boolean stockFluids(StockerCardCache stockerCardCache) {
        BlockPos adjacentPos = getBlockPos().relative(stockerCardCache.direction);
        assert level != null;
        if (!level.isLoaded(adjacentPos)) return false;
        Optional<IFluidHandler> adjacentTankOptional = getAttachedFluidTank(stockerCardCache.direction, stockerCardCache.sneaky).resolve();
        if (adjacentTankOptional.isEmpty()) return false;
        IFluidHandler adacentTank = adjacentTankOptional.get();

        ItemStack filter = stockerCardCache.filterCard;
        if (filter.isEmpty() || !stockerCardCache.isAllowList) { //Needs a filter - at least for now? Also must be in whitelist mode
            return false;
        }
        if (filter.getItem() instanceof FilterBasic || filter.getItem() instanceof FilterCount) {
            if (stockerCardCache.regulate && filter.getItem() instanceof FilterCount) {
                if (regulateFluidStocker(stockerCardCache, adacentTank))
                    return true;
            }
            if (!canAnyFluidFiltersFit(adacentTank, stockerCardCache)) {
                return false; //If we can't fit any of our filtered items into this inventory, don't bother scanning for them
            }
            boolean foundItems = findFluidStackForStocker(stockerCardCache, adacentTank); //Start looking for this item
            if (foundItems)
                return true;

            //If we get to this line of code, it means we found none of the filter
            //stockerCardCache.setRemainingSleep(stockerCardCache.tickSpeed * 5);
        } else if (filter.getItem() instanceof FilterTag) {

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
            if (stockerCardCache.regulate && filter.getItem() instanceof FilterCount) {
                if (regulateItemStocker(stockerCardCache, adjacentInventory))
                    return true;
            }
            if (!canAnyItemFiltersFit(adjacentInventory, stockerCardCache)) {
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
        LaserNodeItemHandler laserNodeItemHandler = getLaserNodeHandlerItem(checkSource.inserterCardCache);
        if (laserNodeItemHandler == null) return ItemStack.EMPTY;
        return laserNodeItemHandler.handler.getStackInSlot(checkSource.slot);
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
        LaserNodeItemHandler laserNodeItemHandler = getLaserNodeHandlerItem(checkSource.inserterCardCache);
        if (laserNodeItemHandler == null) return extractResult;
        ItemStackKey stackInSlotKey = new ItemStackKey(stackInSlot, stockerCardCache.isCompareNBT);
        if (stackInSlot.isEmpty()) //Null means the inventory no longer exists or is unloaded
            stockerDestinationCache.remove(stockerRequest);
        if (stackInSlotKey.equals(itemStackKey)) {  //If the itemstack in that spot matches the itemstack we are looking for
            int extractAmt = Math.min(itemsStillNeeded, stackInSlot.getCount()); //Find out how many to extract
            extractedItemStack = laserNodeItemHandler.handler.extractItem(checkSource.slot, extractAmt, true); //Extract Items
            itemsStillNeeded = itemsStillNeeded - extractedItemStack.getCount();
            if (stackInSlot.getCount() - extractedItemStack.getCount() == 0) {
                stockerDestinationCache.remove(stockerRequest);
            }
            if (itemsStillNeeded == 0) {
                extractResult.addResult(new TransferResult.Result(laserNodeItemHandler.handler, checkSource.slot, checkSource.inserterCardCache, extractedItemStack, laserNodeItemHandler.be, true));
                extractResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
                return extractResult; //If we got all that we need, return the amount we got
            }
        }
        //If we got here, we have to find more of this item type, so start looking for it - note that we don't use the stack above because we will check the whole inventory and the stack above wasn't pulled out
        extractResult = ItemHandlerUtil.extractItemWithSlots(laserNodeItemHandler.be, laserNodeItemHandler.handler, itemStack, origItemsWanted, true, stockerCardCache.isCompareNBT, checkSource.inserterCardCache); //Try to pull out the items we need from this location
        extractResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
        if (!extractResult.results.isEmpty()) { //If we found something, check if the last slot we looked at is empty, and add it to the cache
            int lastSlot = extractResult.results.get(extractResult.results.size() - 1).extractSlot; //The last slot we pulled from in this inventory
            if (laserNodeItemHandler.handler.getStackInSlot(lastSlot).getCount() - extractResult.results.get(extractResult.results.size() - 1).itemStack.getCount() != 0) { //If its not empty now
                stockerDestinationCache.put(new StockerRequest(stockerCardCache, itemStackKey), new StockerSource(checkSource.inserterCardCache, lastSlot)); //Add to the cache
            }
        }
        return extractResult;
    }

    public boolean findEnergyForStocker(StockerCardCache stockerCardCache, IEnergyStorage toEnergyTank) {
        int desired = (int) (toEnergyTank.getMaxEnergyStored() * ((float) stockerCardCache.insertLimit / 100));
        int extractAmt = Math.min(stockerCardCache.extractAmt, desired - toEnergyTank.getEnergyStored());
        List<InserterCardCache> inserterCardCaches = getChannelMatchInserters(stockerCardCache);
        Map<InserterCardCache, Integer> insertHandlers = new Object2IntOpenHashMap<>();

        for (InserterCardCache inserterCardCache : inserterCardCaches) {
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            if (laserNodeEnergyHandler == null) continue;
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;

            int amtRemoved = energyStorage.extractEnergy(extractAmt, true); //Simulate Extract
            if (amtRemoved == 0) { //Next inserter if nothing removed
                continue;
            }
            int amtInserted = toEnergyTank.receiveEnergy(amtRemoved, true); //Simulate Inserting some energy
            if (amtInserted == 0) return false; //This really shouldn't happen - it means the tank is already full?
            insertHandlers.put(inserterCardCache, amtInserted); //Add the handler
            extractAmt -= amtInserted; //Keep track of how much we have left to insert
            if (extractAmt == 0) break;
        }

        if ((stockerCardCache.exact && extractAmt > 0) || insertHandlers.isEmpty()) return false;

        for (Map.Entry<InserterCardCache, Integer> entry : insertHandlers.entrySet()) {
            InserterCardCache inserterCardCache = entry.getKey();
            LaserNodeEnergyHandler laserNodeEnergyHandler = getLaserNodeHandlerEnergy(inserterCardCache);
            IEnergyStorage energyStorage = laserNodeEnergyHandler.handler;
            int actualRemoved = energyStorage.extractEnergy(entry.getValue(), false);
            toEnergyTank.receiveEnergy(actualRemoved, false);
            //drawParticlesFluid(drainedStack, extractorCardCache.direction, extractorCardCache.be, inserterCardCache.be, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        return false; //If we got NOTHING
    }

    public boolean findFluidStackForStocker(StockerCardCache stockerCardCache, IFluidHandler stockerTank) {
        boolean isCount = stockerCardCache.filterCard.getItem() instanceof FilterCount;
        int extractAmt = stockerCardCache.extractAmt;

        List<FluidStack> filteredFluidsList = new CopyOnWriteArrayList<>(stockerCardCache.getFilteredFluids());
        filteredFluidsList.removeIf(fluidStack -> !canFluidFitInTank(stockerTank, fluidStack));//If this fluid can't fit in this tank at all, skip the fluid
        if (filteredFluidsList.isEmpty()) //If nothing in the filter can fit, return false
            return false;

        if (isCount) { //If this is a filter count, prune the list of items to search for to just what we need
            for (FluidStack fluidStack : filteredFluidsList) { //Remove all the items from the list that we already have enough of
                for (int tank = 0; tank < stockerTank.getTanks(); tank++) {
                    FluidStack tankStack = stockerTank.getFluidInTank(tank);
                    if (tankStack.isEmpty() || tankStack.isFluidEqual(fluidStack)) {
                        int filterAmt = stockerCardCache.getFilterAmt(fluidStack);
                        int amtHad = tankStack.getAmount();
                        int amtNeeded = filterAmt - amtHad;
                        if (amtNeeded <= 0) {//if we have enough, move onto the next stack after removing this one from the list
                            filteredFluidsList.remove(fluidStack);
                            continue;
                        }
                        fluidStack.setAmount(Math.min(amtNeeded, extractAmt)); //Adjust the amount we need
                    }
                }
            }
        }

        if (filteredFluidsList.isEmpty()) //If we have nothing left to look for! Probably only happens when its a count card.
            return false;


        //At this point we should have a list of fluids that we need to satisfy the stock request
        for (FluidStack fluidStack : filteredFluidsList) {
            Map<InserterCardCache, FluidStack> insertHandlers = new HashMap<>();
            if (!isCount)
                fluidStack.setAmount(extractAmt); //If this isn't a counting card, we want the extractAmt value
            int amtNeeded = fluidStack.getAmount();

            for (InserterCardCache inserterCardCache : getChannelMatchInserters(stockerCardCache)) { //Iterate through ALL inserter nodes on this channel only
                if (!inserterCardCache.isStackValidForCard(fluidStack))
                    continue;
                LaserNodeFluidHandler laserNodeFluidHandler = getLaserNodeHandlerFluid(inserterCardCache);
                if (laserNodeFluidHandler == null) continue;
                fluidStack.setAmount(amtNeeded);
                IFluidHandler handler = laserNodeFluidHandler.handler();
                FluidStack extractStack = handler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (extractStack.isEmpty()) continue;
                insertHandlers.put(inserterCardCache, extractStack);
                amtNeeded -= extractStack.getAmount();
                if (amtNeeded == 0) break;
            }
            if (!insertHandlers.isEmpty()) {
                if (!stockerCardCache.exact || amtNeeded == 0) { //If its not exact mode, or it is exact mode and we found all we need to satisfy this
                    for (Map.Entry<InserterCardCache, FluidStack> entry : insertHandlers.entrySet()) { //Do all the extracts/inserts
                        InserterCardCache inserterCardCache = entry.getKey();
                        FluidStack insertStack = entry.getValue();
                        LaserNodeFluidHandler laserNodeFluidHandler = getLaserNodeHandlerFluid(inserterCardCache);
                        IFluidHandler handler = laserNodeFluidHandler.handler;
                        int amtFit = stockerTank.fill(insertStack, IFluidHandler.FluidAction.SIMULATE); //Test inserting into the target
                        insertStack.setAmount(amtFit); //Change the stack to size to how much can fit
                        FluidStack drainedStack = handler.drain(insertStack, IFluidHandler.FluidAction.EXECUTE);
                        stockerTank.fill(drainedStack, IFluidHandler.FluidAction.EXECUTE);
                        drawParticlesFluid(drainedStack, inserterCardCache.direction, inserterCardCache.be, stockerCardCache.be, stockerCardCache.direction, inserterCardCache.cardSlot, stockerCardCache.cardSlot);
                    }
                    return true;
                }
            }
        }
        return false; //If we got NOTHING
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
                itemStack.setCount(transferResult.getTotalItemCounts()); //Set the itemStack to how many items we got
                ItemStack insertedStack = ItemHandlerHelper.insertItem(stockerInventory, itemStack, true);
                int totalInserted = transferResult.getTotalItemCounts() - insertedStack.getCount();
                if (totalInserted < transferResult.getTotalItemCounts()) { //We can insert less than we expected, lets fix this...
                    if (totalInserted == 0 || (stockerCardCache.exact))
                        break; //If we can't fit any of the items into this inventory, or failed to meet exact mode's needs, try the next filtered stack
                    for (TransferResult.Result result : transferResult.results) { //Iterate the results and prune them to match what we can insert
                        if (result.itemStack.getCount() > totalInserted) { //In this result is too big
                            if (totalInserted <= 0)
                                transferResult.results.remove(result); //If we can't fit this result at all, remove it
                            else
                                result.itemStack.setCount(totalInserted); //Set the result to match how many more can fit
                        }
                        totalInserted -= result.itemStack.getCount(); //Set the remaining amount to fit less the amount of this result
                    }
                }
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

                LaserNodeItemHandler laserNodeItemHandler = getLaserNodeHandlerItem(inserterCardCache);
                if (laserNodeItemHandler == null) continue;
                ItemHandlerUtil.InventoryCounts inventoryCounts;
                if (stockerInvCaches.containsKey(inserterCardCache)) { //Count the items in the inventory once -then re-use this for future iterations
                    inventoryCounts = stockerInvCaches.get(inserterCardCache);
                } else {
                    inventoryCounts = new ItemHandlerUtil.InventoryCounts(laserNodeItemHandler.handler, stockerCardCache.isCompareNBT);
                    stockerInvCaches.put(inserterCardCache, inventoryCounts);
                }
                if (inventoryCounts.getCount(itemStack) == 0)
                    continue; //Move on if this inventory doesn't have any of this item

                transferResult.addResult(ItemHandlerUtil.extractItemWithSlots(laserNodeItemHandler.be, laserNodeItemHandler.handler, itemStack, itemStack.getCount(), true, stockerCardCache.isCompareNBT, inserterCardCache));
                transferResult.addOtherCard(stockerInventory, -1, stockerCardCache, stockerCardCache.be);
                if (transferResult.getTotalItemCounts() == origCountNeeded) {
                    itemStack.setCount(transferResult.getTotalItemCounts()); //Set the itemStack to how many items we got
                    ItemStack insertedStack = ItemHandlerHelper.insertItem(stockerInventory, itemStack, true);
                    int totalInserted = transferResult.getTotalItemCounts() - insertedStack.getCount();
                    if (totalInserted < transferResult.getTotalItemCounts()) { //We can insert less than we expected, lets fix this...
                        if (totalInserted == 0 || (stockerCardCache.exact))
                            break; //If we can't fit any of the items into this inventory, or failed to meet exact mode's needs, try the next filtered stack
                        for (TransferResult.Result result : transferResult.results) { //Iterate the results and prune them to match what we can insert
                            if (result.itemStack.getCount() > totalInserted) { //In this result is too big
                                if (totalInserted <= 0)
                                    transferResult.results.remove(result); //If we can't fit this result at all, remove it
                                else
                                    result.itemStack.setCount(totalInserted); //Set the result to match how many more can fit
                            }
                            totalInserted -= result.itemStack.getCount(); //Set the remaining amount to fit less the amount of this result
                        }
                    }
                    transferResult.doIt(); //Move the items for real - we have both extractor/inserter caches from the above method
                    int lastSlot = transferResult.results.get(transferResult.results.size() - 1).extractSlot; //The last slot we pulled from in this inventory
                    if (!laserNodeItemHandler.handler.getStackInSlot(lastSlot).isEmpty()) //If its not empty now
                        stockerDestinationCache.put(new StockerRequest(stockerCardCache, new ItemStackKey(itemStack, stockerCardCache.isCompareNBT)), new StockerSource(inserterCardCache, lastSlot)); //Add to the cache
                    return true;
                }
                //If we got here, we still need (more of) this item - check the next inventory
                itemStack.setCount(origCountNeeded - transferResult.getTotalItemCounts()); //Shrink the amount of items we want
            }
            //If we got here, we didn't get ALL we needed. If this is exact mode, we try the next item. If its not, and we got more than 0, return it.
            if (!stockerCardCache.exact && transferResult.getTotalItemCounts() > 0) { //If its exact mode and we got here, we clearly didn't get all we wanted....
                itemStack.setCount(transferResult.getTotalItemCounts()); //Set the itemStack to how many items we got
                ItemStack insertedStack = ItemHandlerHelper.insertItem(stockerInventory, itemStack, true);
                int totalInserted = transferResult.getTotalItemCounts() - insertedStack.getCount();
                if (totalInserted < transferResult.getTotalItemCounts()) { //We can insert less than we expected, lets fix this...
                    if (totalInserted == 0)
                        break; //If we can't fit any of the items into this inventory, try the next filtered stack
                    for (TransferResult.Result result : transferResult.results) { //Iterate the results and prune them to match what we can insert
                        if (result.itemStack.getCount() > totalInserted) { //In this result is too big
                            if (totalInserted <= 0)
                                transferResult.results.remove(result); //If we can't fit this result at all, remove it
                            else
                                result.itemStack.setCount(totalInserted); //Set the result to match how many more can fit
                        }
                        totalInserted -= result.itemStack.getCount(); //Set the remaining amount to fit less the amount of this result
                    }
                }
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

    public void drawParticlesClient() {

        /*if (true) {
            ClientLevel clientLevel = (ClientLevel) level;
            FluidStack fluidStack = new FluidStack(Fluids.LAVA, 1);
            BlockPos toPos = getBlockPos().relative(Direction.UP);
            BlockPos fromPos = getBlockPos();
            Direction direction = Direction.values()[1];

            Vector3f extractOffset = findOffset(direction, 0, offsets);
            FluidFlowParticleData data = new FluidFlowParticleData(fluidStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
            float randomSpread = 0.01f;
            int min = 1;
            int max = 64;
            int minPart = 32;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (fluidStack.getAmount() - min)) / (max - min) + minPart;
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
            }
        }

        if (true) {
            ClientLevel clientLevel = (ClientLevel) level;
            FluidStack fluidStack = new FluidStack(Fluids.WATER, 1);
            BlockPos toPos = getBlockPos().relative(Direction.UP);
            BlockPos fromPos = getBlockPos();
            Direction direction = Direction.values()[1];

            Vector3f extractOffset = findOffset(direction, 1, offsets);
            FluidFlowParticleData data = new FluidFlowParticleData(fluidStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
            float randomSpread = 0.01f;
            int min = 1;
            int max = 64;
            int minPart = 32;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (fluidStack.getAmount() - min)) / (max - min) + minPart;
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
            }
        }

        if (true) {
            ClientLevel clientLevel = (ClientLevel) level;
            FluidStack fluidStack = new FluidStack(Fluids.WATER, 1);
            BlockPos toPos = getBlockPos().relative(Direction.UP);
            BlockPos fromPos = getBlockPos();
            Direction direction = Direction.values()[1];

            Vector3f extractOffset = findOffset(direction, 1, offsets);
            FluidFlowParticleData data = new FluidFlowParticleData(fluidStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
            float randomSpread = 0.01f;
            int min = 1;
            int max = 64;
            int minPart = 32;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (fluidStack.getAmount() - min)) / (max - min) + minPart;
            for (int i = 0; i < count; ++i) {
                //particlesDrawnThisTick++;
                double d1 = this.random.nextGaussian() * (double) randomSpread;
                double d3 = this.random.nextGaussian() * (double) randomSpread;
                double d5 = this.random.nextGaussian() * (double) randomSpread;
                clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
            }
        }*/
        if (particleRenderData.isEmpty() && particleRenderDataFluids.isEmpty()) return;
        ClientLevel clientLevel = (ClientLevel) level;
        //int particlesDrawnThisTick = 0;
        for (ParticleRenderData partData : particleRenderData) {
            //if (particlesDrawnThisTick > 64) return;
            ItemStack itemStack = new ItemStack(Item.byId(partData.item), partData.itemCount);
            BlockPos toPos = partData.toPos;
            BlockPos fromPos = partData.fromPos;
            Direction direction = Direction.values()[partData.direction];
            BlockState targetState = level.getBlockState(toPos);
            float randomSpread = 0.01f;
            int min = 1;
            int max = 64;
            int minPart = 32;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (itemStack.getCount() - min)) / (max - min) + minPart;

            if (targetState.getBlock() instanceof LaserNode) {
                targetState = level.getBlockState(fromPos);
                VoxelShape voxelShape = targetState.getShape(level, fromPos);
                Vector3f extractOffset = findOffset(direction, partData.position, offsets);
                Vector3f insertOffset = CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState);
                ItemFlowParticleData data = new ItemFlowParticleData(itemStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
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
                ItemFlowParticleData data = new ItemFlowParticleData(itemStack, fromPos.getX() + insertOffset.x(), fromPos.getY() + insertOffset.y(), fromPos.getZ() + insertOffset.z(), 10);
                for (int i = 0; i < count; ++i) {
                    //particlesDrawnThisTick++;
                    double d1 = this.random.nextGaussian() * (double) randomSpread;
                    double d3 = this.random.nextGaussian() * (double) randomSpread;
                    double d5 = this.random.nextGaussian() * (double) randomSpread;
                    clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
                }
            }
        }

        for (ParticleRenderDataFluid partData : particleRenderDataFluids) {
            //if (particlesDrawnThisTick > 64) return;
            FluidStack fluidStack = partData.fluidStack;
            BlockPos toPos = partData.toPos;
            BlockPos fromPos = partData.fromPos;
            Direction direction = Direction.values()[partData.direction];
            BlockState targetState = level.getBlockState(toPos);
            float randomSpread = 0.01f;
            int min = 100;
            int max = 8000;
            int minPart = 8;
            int maxPart = 64;
            int count = ((maxPart - minPart) * (fluidStack.getAmount() - min)) / (max - min) + minPart;

            if (targetState.getBlock() instanceof LaserNode) {
                targetState = level.getBlockState(fromPos);
                VoxelShape voxelShape = targetState.getShape(level, toPos);
                Vector3f extractOffset = findOffset(direction, partData.position, offsets);
                Vector3f insertOffset = CardRender.shapeOffset(extractOffset, voxelShape, fromPos, toPos, direction, level, targetState);
                FluidFlowParticleData data = new FluidFlowParticleData(fluidStack, toPos.getX() + extractOffset.x(), toPos.getY() + extractOffset.y(), toPos.getZ() + extractOffset.z(), 10);
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
                FluidFlowParticleData data = new FluidFlowParticleData(fluidStack, fromPos.getX() + insertOffset.x(), fromPos.getY() + insertOffset.y(), fromPos.getZ() + insertOffset.z(), 10);
                for (int i = 0; i < count; ++i) {
                    //particlesDrawnThisTick++;
                    double d1 = this.random.nextGaussian() * (double) randomSpread;
                    double d3 = this.random.nextGaussian() * (double) randomSpread;
                    double d5 = this.random.nextGaussian() * (double) randomSpread;
                    clientLevel.addParticle(data, fromPos.getX() + extractOffset.x() + d1, fromPos.getY() + extractOffset.y() + d3, fromPos.getZ() + extractOffset.z() + d5, 0, 0, 0);
                }
            }
        }
        //System.out.println(particlesDrawnThisTick);
    }

    /** Adds from the PacketNodeParticles a set of particles to draw next client tick **/
    public void addParticleData(ParticleRenderData particleRenderData) {
        this.particleRenderData.add(particleRenderData);
    }

    public void addParticleDataFluid(ParticleRenderDataFluid particleRenderData) {
        this.particleRenderDataFluids.add(particleRenderData);
    }

    /** Draw the particles between node and inventory **/
    public void drawParticles(ItemStack itemStack, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
        drawParticles(itemStack, itemStack.getCount(), fromDirection, sourceBE, destinationBE, destinationDirection, extractPosition, insertPosition);
    }

    /** Draw the particles between node and inventory **/
    public void drawParticlesFluid(FluidStack fluidStack, Direction fromDirection, LaserNodeBE sourceBE, LaserNodeBE destinationBE, Direction destinationDirection, int extractPosition, int insertPosition) {
        ServerTickHandler.addToListFluid(new ParticleDataFluid(fluidStack, sourceBE.getBlockPos(), (byte) fromDirection.ordinal(), destinationBE.getBlockPos(), (byte) destinationDirection.ordinal(), (byte) extractPosition, (byte) insertPosition), level);
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
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = nodeSideCaches[direction.ordinal()];
            nodeSideCache.myRedstoneFromSensors.clear();
        }
        redstoneChecked = false;
        //populateThisRedstoneNetwork(false);
        notifyOtherNodesOfChange();
        markDirtyClient();
        findMyExtractors();
        updateOverclockers();
        Arrays.stream(nodeSideCaches).forEach(NodeSideCache::invalidateEnergy);
        //updateRedstoneOutputs();
    }

    /** When this node changes, tell other nodes to refresh their cache of it **/
    public void notifyOtherNodesOfChange() {
        for (BlockPos pos : otherNodesInNetwork) {
            LaserNodeBE node = getNodeAt(getWorldPos(pos));
            if (node == null) continue;
            node.checkInvNode(this.getBlockPos(), true);
            //node.refreshRedstoneNetwork();
            node.redstoneRefreshed = false;
        }
    }

    /** This method clears the non-persistent inventory node data variables and regenerates them from scratch */
    public void refreshAllInvNodes() {
        inserterNodes.clear();
        inserterCache.clear();
        inserterCacheFluid.clear();
        channelOnlyCache.clear();
        this.stockerDestinationCache.clear();
        this.redstoneNetwork.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            checkInvNode(getWorldPos(pos), false);
        }
        //refreshRedstoneNetwork();
        redstoneRefreshed = false;
        sortInserters();
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * Also populates the providerNodes and stockerNodes variables, so we know which inventory nodes provide or keep in stock items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos, boolean sortInserters) {
        //System.out.println("Check inv node at: " + getBlockPos());
        LaserNodeBE be = getNodeAt(pos);
        BlockPos relativePos = getRelativePos(pos);
        //Remove this position from all caches, so we can repopulate below
        inserterNodes.removeIf(p -> p.relativePos.equals(relativePos));
        inserterCache.clear(); //TODO maybe just remove destinations that match this blockPos
        inserterCacheFluid.clear();
        channelOnlyCache.clear();
        this.stockerDestinationCache.clear();
        if (be == null) return; //If the block position given doesn't contain a LaserNodeBE stop
        /*for (Map.Entry<Byte, Byte> beRedstone: be.myRedstoneIn.entrySet()) {
            updateRedstoneNetwork(beRedstone.getKey(), beRedstone.getValue());
        }*/
        for (Direction direction : Direction.values()) {
            NodeSideCache nodeSideCache = be.nodeSideCaches[direction.ordinal()];
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard && !(card.getItem() instanceof CardRedstone)) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.INSERT)) {
                        inserterNodes.add(new InserterCardCache(relativePos, direction, card, be, slot));
                    }
                }
            }
        }
        if (sortInserters) sortInserters();
    }

    public LaserNodeItemHandler getLaserNodeHandlerItem(InserterCardCache inserterCardCache) {
        if (!inserterCardCache.cardType.equals(BaseCard.CardType.ITEM)) return null;
        BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
        if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
        if (be == null) return null;
        IItemHandler handler = be.getAttachedInventory(inserterCardCache.direction, inserterCardCache.sneaky).orElse(EMPTY);
        if (handler.getSlots() == 0) return null;
        return new LaserNodeItemHandler(be, handler);
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IItemHandler> getAttachedInventory(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        LazyOptional<IItemHandler> testHandler = (facingHandlerItem.get(sideConnection));
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
                handler.addListener(getInvalidatorItem(sideConnection));
                // cache and return
                facingHandlerItem.put(sideConnection, handler);
                return handler;
            }
        }
        // no item handler, cache empty
        facingHandlerItem.remove(sideConnection);
        return LazyOptional.empty();
    }

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

    private NonNullConsumer<LazyOptional<IItemHandler>> getInvalidatorItem(SideConnection sideConnection) {
        return connectionInvalidatorItem.computeIfAbsent(sideConnection, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
            if (te.facingHandlerItem.get(sideConnection) == handler) {
                te.clearCachedInventories(sideConnection);
            }
        }));
    }

    public LaserNodeFluidHandler getLaserNodeHandlerFluid(InserterCardCache inserterCardCache) {
        if (!inserterCardCache.cardType.equals(BaseCard.CardType.FLUID)) return null;
        BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
        if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
        if (be == null) return null;
        LazyOptional<IFluidHandler> fluidhandler = be.getAttachedFluidTank(inserterCardCache.direction, inserterCardCache.sneaky);
        if (!fluidhandler.isPresent()) return null;
        IFluidHandler handler = fluidhandler.resolve().get();
        if (handler.getTanks() == 0) return null;
        return new LaserNodeFluidHandler(be, handler);
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IFluidHandler> getAttachedFluidTank(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        LazyOptional<IFluidHandler> testHandler = (facingHandlerFluid.get(sideConnection));
        if (testHandler != null && testHandler.isPresent()) {
            return testHandler;
        }

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IFluidHandler> handler = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inventorySide);
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
    }

    public LazyOptional<IFluidHandler> getAttachedFluidTankNoCache(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IFluidHandler> handler = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inventorySide);
            if (handler.isPresent()) {
                return handler;
            }
        }
        return LazyOptional.empty();
    }

    public LaserNodeEnergyHandler getLaserNodeHandlerEnergy(InserterCardCache inserterCardCache) {
        if (!inserterCardCache.cardType.equals(BaseCard.CardType.ENERGY)) return null;
        BlockPos nodeWorldPos = getWorldPos(inserterCardCache.relativePos);
        if (!chunksLoaded(nodeWorldPos, nodeWorldPos.relative(inserterCardCache.direction))) return null;
        LaserNodeBE be = getNodeAt(getWorldPos(inserterCardCache.relativePos));
        if (be == null) return null;
        Optional<IEnergyStorage> energyhandler = be.getAttachedEnergyTank(inserterCardCache.direction, inserterCardCache.sneaky).resolve();
        if (energyhandler.isEmpty()) return null;
        IEnergyStorage energyTank = energyhandler.get();
        return new LaserNodeEnergyHandler(be, energyTank);
    }

    /** Somehow this makes it so if you break an adjacent chest it immediately invalidates the cache of it **/
    public LazyOptional<IEnergyStorage> getAttachedEnergyTank(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];
        SideConnection sideConnection = new SideConnection(direction, inventorySide);
        LazyOptional<IEnergyStorage> testHandler = (facingHandlerEnergy.get(sideConnection));
        if (testHandler != null && testHandler.isPresent()) {
            return testHandler;
        }

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IEnergyStorage> handler = be.getCapability(CapabilityEnergy.ENERGY, inventorySide);
            if (handler.isPresent()) {
                // add the invalidator
                handler.addListener(getInvalidatorEnergy(sideConnection));
                // cache and return
                facingHandlerEnergy.put(sideConnection, handler);
                return handler;
            }
        }
        // no item handler, cache empty
        facingHandlerFluid.remove(sideConnection);
        return LazyOptional.empty();
    }

    public LazyOptional<IEnergyStorage> getAttachedEnergyTankNoCache(Direction direction, Byte sneakySide) {
        Direction inventorySide = direction.getOpposite();
        if (sneakySide != -1)
            inventorySide = Direction.values()[sneakySide];

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IEnergyStorage> handler = be.getCapability(CapabilityEnergy.ENERGY, inventorySide);
            if (handler.isPresent()) {
                return handler;
            }
        }
        return LazyOptional.empty();
    }

    private NonNullConsumer<LazyOptional<IFluidHandler>> getInvalidatorFluid(SideConnection sideConnection) {
        return connectionInvalidatorFluid.computeIfAbsent(sideConnection, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
            if (te.facingHandlerFluid.get(sideConnection) == handler) {
                te.clearCachedInventories(sideConnection);
            }
        }));
    }

    private NonNullConsumer<LazyOptional<IEnergyStorage>> getInvalidatorEnergy(SideConnection sideConnection) {
        return connectionInvalidatorEnergy.computeIfAbsent(sideConnection, c -> new WeakConsumerWrapper<>(this, (te, handler) -> {
            if (te.facingHandlerEnergy.get(sideConnection) == handler) {
                te.clearCachedInventories(sideConnection);
            }
        }));
    }


    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories(SideConnection sideConnection) {
        stockerDestinationCache.clear();
        this.facingHandlerItem.remove(sideConnection);
        this.facingHandlerFluid.remove(sideConnection);
        this.facingHandlerEnergy.remove(sideConnection);
    }

    /** Called when a neighbor updates to invalidate the inventory cache */
    public void clearCachedInventories() {
        stockerDestinationCache.clear();
        this.facingHandlerItem.clear();
        this.facingHandlerFluid.clear();
        this.facingHandlerEnergy.clear();
        markDirtyClient();
    }

    public void populateRenderList() {
        //System.out.println("Refreshing Renders at: " + getBlockPos());
        if (level == null || !level.isClientSide) return;
        this.cardRenders.clear();
        redstoneCardSides.clear();
        for (Direction direction : Direction.values()) {
            IItemHandler h = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(new ItemStackHandler(0));
            for (int slot = 0; slot < h.getSlots(); slot++) {
                ItemStack card = h.getStackInSlot(slot);
                if (!(card.getItem() instanceof BaseCard)) continue;
                byte redstoneMode = BaseCard.getRedstoneMode(card);
                if (card.getItem() instanceof CardRedstone) redstoneMode = 2;
                byte redstoneChannel = BaseCard.getRedstoneChannel(card);
                boolean enabled;
                if (redstoneMode == 0 || BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.SENSOR)) { //Sensors are always enabled
                    enabled = true;
                } else {
                    byte strength = getRedstoneChannelStrength(redstoneChannel);
                    if (strength > 0 && redstoneMode == 1) {
                        enabled = false;
                    } else if (strength == 0 && redstoneMode == 2) {
                        enabled = false;
                    } else {
                        enabled = true;
                    }
                }

                if (card.getItem() instanceof CardItem) {
                    if (getAttachedInventoryNoCache(direction, BaseCard.getSneaky(card)).equals(LazyOptional.empty()))
                        continue;

                    cardRenders.add(new CardRender(direction, slot, card, getBlockPos(), level, enabled));
                } else if (card.getItem() instanceof CardFluid) {
                    if (getAttachedFluidTankNoCache(direction, BaseCard.getSneaky(card)).equals(LazyOptional.empty()))
                        continue;

                    cardRenders.add(new CardRender(direction, slot, card, getBlockPos(), level, enabled));
                } else if (card.getItem() instanceof CardEnergy) {
                    Optional<IEnergyStorage> lazyEnergyStorage = getAttachedEnergyTankNoCache(direction, BaseCard.getSneaky(card)).resolve();
                    if (lazyEnergyStorage.isEmpty())
                        continue;
                    //IEnergyStorage energyStorage = lazyEnergyStorage.get();
                    /*if (BaseCard.getTransferMode(card) == 1) { //Extract
                        if (energyStorage.canExtract())
                            cardRenders.add(new CardRender(direction, slot, card, getBlockPos()));
                    } else { //Insert/Stock
                        if (energyStorage.canReceive())*/
                    cardRenders.add(new CardRender(direction, slot, card, getBlockPos(), level, enabled));
                    //}
                } else if (card.getItem() instanceof CardRedstone) {
                    redstoneCardSides.put((byte) direction.ordinal(), true);
                    cardRenders.add(new CardRender(direction, slot, card, getBlockPos(), level, enabled));
                }
            }
        }
        BlockState state = this.getBlockState();
        level.updateNeighborsAt(getBlockPos(), this.getBlockState().getBlock());
        state.updateNeighbourShapes(level, getBlockPos(), UPDATE_ALL);
        rendersChecked = true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null) {
            return nodeSideCaches[side.ordinal()].handlerLazyOptional.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            if (side == null) {
                return LazyOptional.empty();
            } else {
                NodeSideCache nodeSideCache = nodeSideCaches[side.ordinal()];
                for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                    ItemStack card = nodeSideCache.itemHandler.getStackInSlot(slot);
                    if (card.getItem() instanceof CardEnergy) {
                        BaseCardCache baseCardCache = new BaseCardCache(side, card, slot, this);
                        if (baseCardCache.enabled)
                            return nodeSideCaches[side.ordinal()].laserEnergyStorage.cast();
                    }
                }
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        ListTag redstoneNetworkTag = new ListTag();
        for (Map.Entry<Byte, Byte> entry : redstoneNetwork.byte2ByteEntrySet()) {
            CompoundTag comp = new CompoundTag();
            comp.putByte("channel", entry.getKey());
            comp.putByte("strength", entry.getValue());
            redstoneNetworkTag.add(comp);
        }
        tag.put("redstoneNetworkTag", redstoneNetworkTag);
        //System.out.println(redstoneNetworkTag + " at " + getBlockPos());
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        this.load(tag);
        redstoneNetwork.clear();
        ListTag redstoneNetworkTag = tag.getList("redstoneNetworkTag", Tag.TAG_COMPOUND);
        for (int i = 0; i < redstoneNetworkTag.size(); i++) {
            byte channel = redstoneNetworkTag.getCompound(i).getByte("channel");
            byte strength = redstoneNetworkTag.getCompound(i).getByte("strength");
            redstoneNetwork.put(channel, strength);
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
        rendersChecked = false;
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
        Arrays.stream(nodeSideCaches).forEach(e -> e.laserEnergyStorage.invalidate());
    }

    public class LaserEnergyStorage implements IEnergyStorage {
        private final Direction facing;

        public LaserEnergyStorage(Direction facing) {
            this.facing = facing;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return LaserNodeBE.this.receiveEnergy(facing, maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

}
