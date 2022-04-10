package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.NodeItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.ExtractorCard;
import com.direwolf20.laserio.util.InserterCard;
import com.direwolf20.laserio.util.WeakConsumerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
import java.util.stream.Collectors;

public class LaserNodeBE extends BaseLaserBE {
    /** This blocks Item Handlers **/
    private final NodeItemHandler[] itemHandler = new NodeItemHandler[6]; //The item stacks in each side of the node, for local use only
    private final LazyOptional<NodeItemHandler>[] handler = new LazyOptional[6]; //The capability thingy gives this one out for others to access?
    private final IItemHandler EMPTY = new ItemStackHandler(0);

    /** Adjacent Inventory Handlers **/
    @Nullable
    private LazyOptional<IItemHandler>[] facingHandler = new LazyOptional[6];
    private final List<NonNullConsumer<LazyOptional<IItemHandler>>> facingInvalidator = new ArrayList<>(); //Lambda to call when a lazy optional is invalidated. Final variable to reduce memory usage

    /** Variables for tracking and sending items/filters/etc **/
    private Set<BlockPos> otherNodesInNetwork = new HashSet<>();
    private final List<InserterCard> inserterNodes = new ArrayList<>(); //All Inventory nodes that contain an inserter card
    private final List<ExtractorCard> extractorCards = new ArrayList<>();

    /** Misc Variables **/
    private boolean discoveredNodes = false;

    public LaserNodeBE(BlockPos pos, BlockState state) {
        super(Registration.LaserNode_BE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            final int j = direction.ordinal();
            itemHandler[j] = new NodeItemHandler(9, this);
            handler[j] = LazyOptional.of(() -> itemHandler[j]);
            facingInvalidator.add(new WeakConsumerWrapper<>(this, (te, handler) -> {
                if (te.facingHandler[j] == handler) {
                    te.clearCachedInventories(j);
                }
            }));
        }
    }

    public void setOtherNodesInNetwork(Set<BlockPos> otherNodesInNetwork) {
        this.otherNodesInNetwork.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            this.otherNodesInNetwork.add(getRelativePos(pos));
        }
        refreshAllInvNodes(); //Seeing as the otherNodes list just got updated, we should refresh the InventoryNode content caches
    }

    public void findMyExtractors() {
        this.extractorCards.clear();
        for (Direction direction : Direction.values()) {
            for (int slot = 0; slot < LaserNodeContainer.SLOTS; slot++) {
                ItemStack card = itemHandler[direction.ordinal()].getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        extractorCards.add(new ExtractorCard(BaseCard.getItemExtractAmt(card), direction, BaseCard.getChannel(card)));
                    }
                }
            }
        }
    }

    public void extractItems() {
        for (ExtractorCard extractorCard : extractorCards) {
            sendItems(extractorCard);
        }
    }

    public void tickServer() {
        if (level.isClientSide) return;
        if (!discoveredNodes) { //On world / chunk reload, lets rediscover the network, including this block's extractor cards.
            discoverAllNodes();
            findMyExtractors();
            discoveredNodes = true;
        }
        extractItems();
    }

    //TODO Efficiency
    public void sendItems(ExtractorCard extractorCard) {
        IItemHandler adjacentInventory = getAttachedInventory(extractorCard.direction).orElse(EMPTY);
        for (int slot = 0; slot < adjacentInventory.getSlots(); slot++) {
            ItemStack stackInSlot = adjacentInventory.getStackInSlot(slot);
            if (stackInSlot.isEmpty()) continue;
            for (InserterCard inserterCard : inserterNodes.stream().filter(p -> p.channel == extractorCard.channel).collect(Collectors.toList())) {
                LaserNodeBE be = getNodeAt(getWorldPos(inserterCard.relativePos));
                if (be == null) continue;
                IItemHandler possibleDestination = be.getAttachedInventory(inserterCard.direction).orElse(EMPTY);
                if (possibleDestination.getSlots() == 0) continue;
                ItemStack itemStack = adjacentInventory.extractItem(slot, extractorCard.extractAmt, true); //Pretend to pull the item out
                ItemStack postInsertStack = ItemHandlerHelper.insertItem(possibleDestination, itemStack, false); //Attempt to insert the item
                if (!postInsertStack.equals(itemStack, false)) { //If something changed
                    int countExtracted = postInsertStack.isEmpty() ? itemStack.getCount() : itemStack.getCount() - postInsertStack.getCount();
                    adjacentInventory.extractItem(slot, countExtracted, false); //Actually remove the number of items
                    drawParticles(itemStack, extractorCard.direction, be, inserterCard.direction);
                    return;
                }
            }
        }
    }

    public void drawParticles(ItemStack itemStack, Direction fromdirection, LaserNodeBE destinationBE, Direction destinationDirection) {
        ServerLevel serverWorld = (ServerLevel) level;
        //Extract
        BlockPos fromPos = getBlockPos().relative(fromdirection);
        BlockPos toPos = getBlockPos();
        ItemFlowParticleData data = new ItemFlowParticleData(itemStack, toPos.getX() + 0.5, toPos.getY() + 0.5, toPos.getZ() + 0.5, 10);
        serverWorld.sendParticles(data, fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5, 8 * itemStack.getCount(), 0.1f, 0.1f, 0.1f, 0);

        //Insert
        fromPos = destinationBE.getBlockPos();
        toPos = destinationBE.getBlockPos().relative(destinationDirection);
        data = new ItemFlowParticleData(itemStack, toPos.getX() + 0.5, toPos.getY() + 0.5, toPos.getZ() + 0.5, 10);
        serverWorld.sendParticles(data, fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5, 8 * itemStack.getCount(), 0.1f, 0.1f, 0.1f, 0);

    }

    public void getItems(ItemStack card, Direction direction) {
        IItemHandler adjacentInventory = getAttachedInventory(direction).orElse(EMPTY);
        if (adjacentInventory.getSlots() != 0) {
            //System.out.println("Getting for: " + getBlockPos().relative(direction));
        }
    }

    /** Called when changes happen - such as a card going into a side, or a card being modified via container **/
    public void updateThisNode() {
        setChanged();
        notifyOtherNodesOfChange();
        markDirtyClient();
        findMyExtractors();
    }

    public void notifyOtherNodesOfChange() {
        for (BlockPos pos : otherNodesInNetwork) {
            LaserNodeBE node = getNodeAt(getWorldPos(pos));
            if (node == null) continue;
            //System.out.println("Telling " + node.getBlockPos() + " to update inventory of " + this.getBlockPos());
            node.checkInvNode(this.getBlockPos());
        }
    }

    /**
     * This method clears the non-persistent inventory node data variables and regenerates them from scratch
     */
    public void refreshAllInvNodes() {
        //System.out.println("Scanning all inventory nodes at: " + getBlockPos());
        inserterNodes.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            checkInvNode(getWorldPos(pos));
        }
        //System.out.println(inserterNodes);
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * Also populates the providerNodes and stockerNodes variables, so we know which inventory nodes provide or keep in stock items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos) {
        //System.out.println("Updating cache at: " + pos);
        LaserNodeBE be = getNodeAt(pos);
        BlockPos relativePos = getRelativePos(pos);
        //Remove this position from all caches, so we can repopulate below
        inserterNodes.removeIf(p -> p.relativePos.equals(relativePos));
        if (be == null) return; //If the block position given doesn't contain a LaserNodeBE stop
        for (Direction direction : Direction.values()) {
            for (int slot = 0; slot < LaserNodeContainer.SLOTS; slot++) {
                ItemStack card = be.itemHandler[direction.ordinal()].getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    //System.out.println("Found card at " + pos + ": " + BaseCard.getTransferMode(card));
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        //sendItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        //getItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.INSERT)) {
                        //getItems(card, direction);
                        inserterNodes.add(new InserterCard(relativePos, direction, BaseCard.getChannel(card)));
                    }
                }
            }
        }
        //Loop through all cards and update the cache'd data
        /*for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            addToFilterCache(pos, stack);
            if (stack.getItem() instanceof CardExtractor)
                extractorNodes.add(pos);
            if (stack.getItem() instanceof CardInserter) {
                inserterNodes.add(pos);
            }
            if (stack.getItem() instanceof CardProvider) {
                providerNodes.add(pos);
            }
            if (stack.getItem() instanceof CardStocker)
                stockerNodes.add(pos);
        }*/
    }

    public LazyOptional<IItemHandler> getAttachedInventory(Direction direction) {
        if (facingHandler[direction.ordinal()] != null) {
            return facingHandler[direction.ordinal()];
        }

        // if no inventory cached yet, find a new one
        assert level != null;
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        // if we have a TE and its an item handler, try extracting from that
        if (be != null) {
            LazyOptional<IItemHandler> handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler.isPresent()) {
                // add the invalidator
                handler.addListener(facingInvalidator.get(direction.ordinal()));
                // cache and return
                return facingHandler[direction.ordinal()] = handler;
            }
        }
        // no item handler, cache empty //TODO This was commented out in Logistics Lasers - why?
        facingHandler[direction.ordinal()] = null;
        return LazyOptional.empty();
    }

    /**
     * Called when a neighbor updates to invalidate the inventory cache
     */
    public void clearCachedInventories(int j) {
        this.facingHandler[j] = null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null) {
            return handler[side.ordinal()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        for (int i = 0; i < Direction.values().length; i++) {
            if (tag.contains("Inventory" + i)) {
                itemHandler[i].deserializeNBT(tag.getCompound("Inventory" + i));
            }
        }
        super.load(tag);

    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < Direction.values().length; i++)
            tag.put("Inventory" + i, itemHandler[i].serializeNBT());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Arrays.stream(handler).forEach(e -> e.invalidate());
    }
}
