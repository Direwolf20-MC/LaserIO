package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.CustomItemHandlers.NodeItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class LaserNodeBE extends BaseLaserBE {
    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    private final ItemStackHandler[] itemHandler = new ItemStackHandler[6];
    private final LazyOptional<IItemHandler>[] handler = new LazyOptional[6];
    private final IItemHandler EMPTY = new ItemStackHandler(0);

    private final Set<BlockPos> otherNodesInNetwork = new HashSet<>();
    private final HashMap<BlockPos, Direction> inserterNodes = new HashMap<>(); //All Inventory nodes that contain an inserter card

    public LaserNodeBE(BlockPos pos, BlockState state) {
        super(Registration.LaserNode_BE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            final int j = direction.ordinal();
            itemHandler[j] = new NodeItemHandler(9, this);
            handler[j] = LazyOptional.of(() -> itemHandler[j]);
        }
    }

    public void tickServer() {
        if (level.isClientSide) return;
        for (Direction direction : Direction.values()) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack card = itemHandler[direction.ordinal()].getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        sendItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        getItems(card, direction);
                    }
                }
            }
        }
    }

    //TODO Efficiency
    public void sendItems(ItemStack card, Direction direction) {
        IItemHandler adjacentInventory = getAttachedInventory(direction);
        if (adjacentInventory != null) {
            for (int slot = 0; slot < adjacentInventory.getSlots(); slot++) {
                ItemStack stackInSlot = adjacentInventory.getStackInSlot(slot);
                if (!stackInSlot.isEmpty()) {
                    for (Map.Entry<BlockPos, Direction> entry : inserterNodes.entrySet()) {
                        BlockEntity be = level.getBlockEntity(getWorldPos(entry.getKey()));
                        if (be instanceof LaserNodeBE) {
                            IItemHandler possibleDestination = ((LaserNodeBE) be).getAttachedInventory(entry.getValue());
                            if (possibleDestination == null) continue;
                            ItemStack itemStack = adjacentInventory.extractItem(slot, 1, false);
                            ItemStack postInsertStack = ItemHandlerHelper.insertItem(possibleDestination, itemStack, false);
                            if (postInsertStack.isEmpty()) {
                                ServerLevel serverWorld = (ServerLevel) level;
                                //Extract
                                BlockPos fromPos = getBlockPos().relative(direction);
                                BlockPos toPos = getBlockPos();
                                ItemFlowParticleData data = new ItemFlowParticleData(itemStack, toPos.getX() + 0.5, toPos.getY() + 0.5, toPos.getZ() + 0.5, 10);
                                serverWorld.sendParticles(data, fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5, 8 * itemStack.getCount(), 0.1f, 0.1f, 0.1f, 0);

                                //Insert
                                fromPos = be.getBlockPos();
                                toPos = be.getBlockPos().relative(entry.getValue());
                                data = new ItemFlowParticleData(itemStack, toPos.getX() + 0.5, toPos.getY() + 0.5, toPos.getZ() + 0.5, 10);
                                serverWorld.sendParticles(data, fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5, 8 * itemStack.getCount(), 0.1f, 0.1f, 0.1f, 0);

                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void getItems(ItemStack card, Direction direction) {
        IItemHandler adjacentInventory = getAttachedInventory(direction);
        if (adjacentInventory != null) {
            //System.out.println("Getting for: " + getBlockPos().relative(direction));
        }
    }

    /**
     * Resets all the cached node data and rediscovers the network by depth first searching (I think).
     */
    public void discoverAllNodes() {
        System.out.println("Discovering All Nodes!");
        otherNodesInNetwork.clear(); //Clear the existing list of nodes

        Queue<BlockPos> nodesToCheck = new LinkedList<>();
        Set<BlockPos> checkedNodes = new HashSet<>();
        nodesToCheck.add(getBlockPos()); //We should add this block to itself, so it can transfer between 2 adjacent inventories
        //nodesToCheck.addAll(getWorldConnections()); //Add all the nodes connected to this controller to the list of nodes to check out


        while (nodesToCheck.size() > 0) {
            BlockPos posToCheck = nodesToCheck.remove(); //Pop the stack
            if (!checkedNodes.add(posToCheck))
                continue; //Don't check nodes we've checked before
            BlockEntity be = level.getBlockEntity(posToCheck);
            if (be instanceof BaseLaserBE) {
                //addToAllNodes(posToCheck); //Add this node to the all nodes list
                Set<BlockPos> connectedNodes = ((BaseLaserBE) be).getWorldConnections(); //Get all the nodes this node is connected to
                nodesToCheck.addAll(connectedNodes); //Add them to the list to check
                //((BaseLaserBE) be).setControllerPos(this.pos); //Set this node's controller to this position
                //oldNodes.remove(posToCheck);
                if (be instanceof LaserNodeBE)
                    otherNodesInNetwork.add(getRelativePos(posToCheck));
            }
        }
        System.out.println("Other Nodes: " + otherNodesInNetwork);
        for (BlockPos pos : otherNodesInNetwork) {
            System.out.println(getWorldPos(pos));
        }
        //updateLaserConnections();
        refreshAllInvNodes();
    }

    /**
     * This method clears the non-persistent inventory node data variables and regenerates them from scratch
     */
    public void refreshAllInvNodes() {
        System.out.println("Scanning all inventory nodes");
        inserterNodes.clear();
        for (BlockPos pos : otherNodesInNetwork) {
            checkInvNode(getWorldPos(pos));
        }
        System.out.println(inserterNodes);
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * Also populates the providerNodes and stockerNodes variables, so we know which inventory nodes provide or keep in stock items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos) {
        System.out.println("Updating cache at: " + pos);
        LaserNodeBE be = (LaserNodeBE) level.getBlockEntity(pos);
        //Remove this position from all caches, so we can repopulate below
        inserterNodes.remove(pos);

        for (Direction direction : Direction.values()) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack card = be.itemHandler[direction.ordinal()].getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    System.out.println("Found card at " + pos + ": " + BaseCard.getTransferMode(card));
                    if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        //sendItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.STOCK)) {
                        //getItems(card, direction);
                    } else if (BaseCard.getNamedTransferMode(card).equals(BaseCard.TransferMode.INSERT)) {
                        //getItems(card, direction);
                        inserterNodes.put(getRelativePos(pos), direction);
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

    public IItemHandler getAttachedInventory(Direction direction) {
        BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
        if (be == null)
            return null;
        if (!(be instanceof LaserNodeBE)) {
            IItemHandler adjacentHandler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(EMPTY);
            if (adjacentHandler.getSlots() != 0)
                return adjacentHandler;
        }

        return null;
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
}
