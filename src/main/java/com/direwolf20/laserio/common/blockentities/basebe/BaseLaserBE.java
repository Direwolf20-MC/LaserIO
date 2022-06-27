package com.direwolf20.laserio.common.blockentities.basebe;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.direwolf20.laserio.common.items.LaserWrench.maxDistance;

public class BaseLaserBE extends BlockEntity {
    protected final Set<BlockPos> connections = new CopyOnWriteArraySet<>();
    protected final Set<BlockPos> renderedConnections = new CopyOnWriteArraySet<>();

    public BaseLaserBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Gets the node at a specific world position, returning null if not a node */
    public LaserNodeBE getNodeAt(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LaserNodeBE) return (LaserNodeBE) be;
        return null;
    }

    /**
     * Resets all the cached node data and rediscovers the network by depth first searching (I think).
     * Share what we've learned with all the InventoryNodes we found
     */
    public void discoverAllNodes() {
        //System.out.println("Discovering all nodes at: " + getBlockPos());
        Set<BlockPos> otherNodesInNetwork = new HashSet<>(); //Fresh list of nodes

        Queue<BlockPos> nodesToCheck = new LinkedList<>();
        Set<BlockPos> checkedNodes = new HashSet<>();
        nodesToCheck.add(getBlockPos()); //We should add this block to itself, as a starting point -- also if its a node it'll add itself

        while (nodesToCheck.size() > 0) {
            BlockPos posToCheck = nodesToCheck.remove(); //Pop the stack
            if (!checkedNodes.add(posToCheck))
                continue; //Don't check nodes we've checked before
            BlockEntity be = level.getBlockEntity(posToCheck);
            if (be instanceof BaseLaserBE) {
                Set<BlockPos> connectedNodes = ((BaseLaserBE) be).getWorldConnections(); //Get all the nodes this node is connected to
                nodesToCheck.addAll(connectedNodes); //Add them to the list to check
                if (be instanceof LaserNodeBE)
                    otherNodesInNetwork.add(posToCheck);
            }
        }
        for (BlockPos pos : otherNodesInNetwork) { //Go through all the inventory nodes we've found and tell them about all the inventory nodes...
            LaserNodeBE nodeBE = getNodeAt(pos);
            if (nodeBE == null) continue;
            nodeBE.setOtherNodesInNetwork(otherNodesInNetwork);
        }
    }

    /** Add another node to this ones connected list */
    public boolean addNode(BlockPos pos) {
        return connections.add(getRelativePos(pos));
    }

    /** Only one of the nodes should render the laser connection - doesn't really matter which one */
    public boolean addRenderNode(BlockPos pos) {
        boolean success = renderedConnections.add(getRelativePos(pos));
        if (success) {
            markDirtyClient();
        } else {
            setChanged();
        }
        return success;
    }

    /** Remove another nodes location from the list of connected nodes */
    public boolean removeNode(BlockPos pos) {
        BlockPos relativePos = getRelativePos(pos);
        connections.remove(relativePos);
        boolean success = renderedConnections.remove(relativePos); //Remove it from the rendered list as well, whether its there or not
        if (success) {
            markDirtyClient();
        } else {
            setChanged();
        }
        return success;
    }

    /** Check to see if a worldPos is connected to this block **/
    public boolean isNodeConnected(BlockPos pos) {
        return connections.contains(getRelativePos(pos));
    }

    /** Helpers to translate between relative/world pos */
    public BlockPos getWorldPos(BlockPos relativePos) {
        return getBlockPos().offset(relativePos);
    }

    public BlockPos getRelativePos(BlockPos worldPos) {
        return worldPos.subtract(getBlockPos());
    }

    public void handleConnection(BaseLaserBE be) {
        BlockPos connectingPos = be.getBlockPos();
        if (isNodeConnected(connectingPos)) { //If these nodes are already connected, disconnect them
            removeConnection(connectingPos, be);
        } else {
            addConnection(connectingPos, be);
        }
    }

    /**
     * @param connectingPos The Position in world you're connecting this TE to.
     * @param be            The block entity being connected to this one (And vice versa)
     *                      Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */

    public void addConnection(BlockPos connectingPos, BaseLaserBE be) {
        addNode(connectingPos); // Add that node to this one
        be.addNode(getBlockPos()); // Add this node to that one
        addRenderNode(connectingPos); // Add the render on this node only
        discoverAllNodes(); //Re discover this new network
    }

    /**
     * @param connectingPos The block position in world you're disconnection this TE from.
     * @param be            The block entity being disconnected from this one (And vice versa)
     *                      Disconnects This Pos from Target Pos, and disconnects Target Pos from This pos
     */

    public void removeConnection(BlockPos connectingPos, BaseLaserBE be) {
        removeNode(connectingPos); // Remove that node from this one
        be.removeNode(getBlockPos()); // Remove this node from that one
        discoverAllNodes(); //Re discover on both nodes in case we have separated 2 networks
        be.discoverAllNodes();
    }

    /** Get the connections relative coordinates */
    public Set<BlockPos> getConnections() {
        return connections;
    }

    /** Get the connections world coordinates */
    public Set<BlockPos> getWorldConnections() {
        Set<BlockPos> worldConnections = new HashSet<>();
        for (BlockPos relativePos : connections)
            worldConnections.add(getWorldPos(relativePos));
        return worldConnections;
    }

    public Set<BlockPos> getRenderedConnections() {
        return renderedConnections;
    }

    /** Disconnect ALL connected nodes - called when the block is broken for example */
    public void disconnectAllNodes() {
        Set<BaseLaserBE> connectionsToUpdate = new HashSet<>(); //We're going to want to rediscover the network on each disconnected node, but not until all disconnections are done
        for (BlockPos pos : connections) {
            BlockPos connectingPos = getWorldPos(pos);
            BlockEntity be = level.getBlockEntity(connectingPos);

            if (be instanceof BaseLaserBE) {
                ((BaseLaserBE) be).removeNode(getBlockPos()); // Remove this node from that one
                connectionsToUpdate.add((BaseLaserBE) be);
            }
        }

        connections.clear();
        for (BaseLaserBE be : connectionsToUpdate)
            be.discoverAllNodes(); //Tell the other node to re-discover their new (possibly disconnected) network(s)

        discoverAllNodes(); //Typically this isn't really needed, but in case its used in some future point i guess it can't hurt
    }

    /** Validates the connections are still valid -- for use if a block is moved **/
    public void validateConnections(BlockPos originalPos) {
        if (level == null || level.isClientSide) {
            return;
        }
        Set<BaseLaserBE> connectionsToUpdate = new HashSet<>(); //We're going to want to rediscover the network on each disconnected node, but not until all disconnections are done
        BlockPos movedPos = getBlockPos().subtract(originalPos);
        for (BlockPos pos : connections) {
            BlockPos oldPos = pos.subtract(movedPos);
            BlockPos oldWorldPos = getWorldPos(oldPos);
            BlockEntity oldBe = level.getBlockEntity(oldWorldPos);
            if (oldBe instanceof BaseLaserBE baseLaserBE) {
                boolean wasRender = renderedConnections.contains(pos);
                baseLaserBE.removeNode(originalPos); // Remove this node from that one
                removeNode(baseLaserBE.getBlockPos().offset(movedPos)); //Remove that node from this one
                connectionsToUpdate.add(baseLaserBE); //Prepare to update that node's connections
                if (oldWorldPos.closerThan(getBlockPos(), maxDistance)) {
                    addNode(baseLaserBE.getBlockPos()); // Add that node to this one
                    baseLaserBE.addNode(getBlockPos()); // Add this node to that one
                    if (wasRender) //IF this was responsible for rendering, hook me up, otherwise get the other node to render
                        addRenderNode(baseLaserBE.getBlockPos());
                    else
                        baseLaserBE.addRenderNode(getBlockPos());
                }
            }
        }
        for (BlockPos pos : connections) { //Now that we remapped connections, theres an OFF chance that some connections are still invalid, so lets validate them -- This can happen if the two nodes move in different directions at the same time
            BlockPos connectingPos = getWorldPos(pos);
            BlockEntity be = level.getBlockEntity(connectingPos);

            if (!(be instanceof BaseLaserBE)) {
                removeNode(getWorldPos(pos)); //Remove that node from this one - since that node is no longer a node, we can't update it. Its abandoned!
            }
        }
        for (BaseLaserBE be : connectionsToUpdate)
            be.discoverAllNodes(); //Tell the other node to re-discover their new (possibly disconnected) network(s)

        discoverAllNodes(); //Typically this isn't really needed, but in case its used in some future point i guess it can't hurt
    }

    /** Misc Methods for TE's */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        connections.clear();
        ListTag connections = tag.getList("connections", Tag.TAG_COMPOUND);
        for (int i = 0; i < connections.size(); i++) {
            BlockPos blockPos = NbtUtils.readBlockPos(connections.getCompound(i).getCompound("pos"));
            this.connections.add(blockPos);
        }
        renderedConnections.clear();
        ListTag renderedConnections = tag.getList("renderedConnections", Tag.TAG_COMPOUND);
        for (int i = 0; i < renderedConnections.size(); i++) {
            BlockPos blockPos = NbtUtils.readBlockPos(renderedConnections.getCompound(i).getCompound("pos"));
            this.renderedConnections.add(blockPos);
        }
        BlockPos originalPos = NbtUtils.readBlockPos(tag.getCompound("myWorldPos"));
        if (!originalPos.equals(getBlockPos()) && !originalPos.equals(BlockPos.ZERO))
            validateConnections(originalPos);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag connections = new ListTag();
        for (BlockPos blockPos : this.connections) {
            CompoundTag comp = new CompoundTag();
            comp.put("pos", NbtUtils.writeBlockPos(blockPos));
            connections.add(comp);
        }
        tag.put("connections", connections);
        ListTag renderedConnections = new ListTag();
        for (BlockPos blockPos : this.renderedConnections) {
            CompoundTag comp = new CompoundTag();
            comp.put("pos", NbtUtils.writeBlockPos(blockPos));
            renderedConnections.add(comp);
        }
        tag.put("renderedConnections", renderedConnections);
        tag.put("myWorldPos", NbtUtils.writeBlockPos(getBlockPos()));
    }

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().above(10).north(10).east(10), getBlockPos().below(10).south(10).west(10));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public void markDirtyClient() {
        this.setChanged();
        if (this.getLevel() != null) {
            BlockState state = this.getLevel().getBlockState(this.getBlockPos());
            this.getLevel().sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        //if (!level.isClientSide)
        //    validateConnections();
        super.clearRemoved();
    }


}
