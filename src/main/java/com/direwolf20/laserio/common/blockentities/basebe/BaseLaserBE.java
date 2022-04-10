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

public class BaseLaserBE extends BlockEntity {
    protected final Set<BlockPos> connections = new HashSet<>();
    protected final Set<BlockPos> renderedConnections = new HashSet<>();

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

    /**Add another node to this ones connected list*/
    public boolean addNode(BlockPos pos) {
        boolean success = connections.add(getRelativePos(pos));
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    /**Helpers to translate between relative/world pos*/
    public BlockPos getWorldPos(BlockPos relativePos) {
        return getBlockPos().offset(relativePos);
    }

    public BlockPos getRelativePos(BlockPos worldPos) {
        return worldPos.subtract(getBlockPos());
    }

    /**Only one of the nodes should render the laser connection - doesn't really matter which one*/
    public boolean addRenderNode(BlockPos pos) {
        boolean success = renderedConnections.add(getRelativePos(pos));
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    /**Remove another nodes location from the list of connected nodes*/
    public boolean removeNode(BlockPos pos) {
        boolean success = connections.remove(getRelativePos(pos));
        renderedConnections.remove(getRelativePos(pos)); //Remove it from the rendered list as well
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    /**
     * @param pos The Position in world you're connecting this TE to.
     * @return Was the connection successful
     * Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */
    public boolean addConnection(BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseLaserBE))
            return false;
        BaseLaserBE be = (BaseLaserBE) blockEntity;

        if (addNode(pos)) {
            addRenderNode(pos);
            boolean success = be.addNode(getBlockPos());
            if (success)
                discoverAllNodes();
            return success;
        }
        return false;
    }

    /**
     * @param pos The Position in world you're disconnection this TE from.
     * @return Was the disconnect successful
     * Disconnects This Pos from Target Pos, and disconnects Target Pos from This pos
     */
    public boolean removeConnection(BlockPos pos) {
        boolean success = removeNode(pos);
        if (success) {
            BaseLaserBE be = (BaseLaserBE) level.getBlockEntity(pos);
            be.removeNode(getBlockPos());
            discoverAllNodes();
            be.discoverAllNodes();
        }
        return success;
    }

    /**Get the connections relative coordinates*/
    public Set<BlockPos> getConnections() {
        return connections;
    }

    /**Get the connections world coordinates*/
    public Set<BlockPos> getWorldConnections() {
        Set<BlockPos> worldConnections = new HashSet<>();
        for (BlockPos relativePos : connections)
            worldConnections.add(getWorldPos(relativePos));
        return worldConnections;
    }

    public Set<BlockPos> getRenderedConnections() {
        return renderedConnections;
    }

    /**Disconnect ALL connected nodes - called when the block is broken for example*/
    public void disconnectAllNodes() {
        for (BlockPos pos : connections) {
            BlockEntity be = level.getBlockEntity(getWorldPos(pos));
            if (be instanceof BaseLaserBE) {
                ((BaseLaserBE) be).removeNode(getRelativePos(be.getBlockPos()));
            }
        }
    }

    /**Misc Methods for TE's*/
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

}
