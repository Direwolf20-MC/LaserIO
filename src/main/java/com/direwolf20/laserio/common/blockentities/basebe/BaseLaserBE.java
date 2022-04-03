package com.direwolf20.laserio.common.blockentities.basebe;

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
import java.util.Set;

public class BaseLaserBE extends BlockEntity {
    protected final Set<BlockPos> connections = new HashSet<>();
    protected final Set<BlockPos> renderedConnections = new HashSet<>();

    public BaseLaserBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    public boolean addNode(BlockPos pos) {
        boolean success = connections.add(getRelativePos(pos));
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    public BlockPos getWorldPos(BlockPos relativePos) {
        return getBlockPos().offset(relativePos);
    }

    public BlockPos getRelativePos(BlockPos worldPos) {
        return worldPos.subtract(getBlockPos());
    }

    public boolean addRenderNode(BlockPos pos) {
        boolean success = renderedConnections.add(getRelativePos(pos));
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    public boolean removeNode(BlockPos pos) {
        boolean success = connections.remove(getRelativePos(pos));
        renderedConnections.remove(getRelativePos(pos));
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
        BaseLaserBE be = (BaseLaserBE) level.getBlockEntity(pos);
        if (!(this instanceof BaseLaserBE || be instanceof BaseLaserBE))
            return false;

        boolean success = addNode(pos);
        if (success) {
            addRenderNode(pos);
            return be.addNode(getBlockPos());
        }
        return false;
    }

    public boolean removeConnection(BlockPos pos) {
        boolean success = removeNode(pos);
        if (success) {
            BaseLaserBE be = (BaseLaserBE) level.getBlockEntity(pos);
            be.removeNode(getBlockPos());
        }
        return success;
    }

    public Set<BlockPos> getConnections() {
        return connections;
    }

    public Set<BlockPos> getWorldConnections() {
        Set<BlockPos> worldConnections = new HashSet<>();
        for (BlockPos relativePos : connections)
            worldConnections.add(getWorldPos(relativePos));
        return worldConnections;
    }

    public Set<BlockPos> getRenderedConnections() {
        return renderedConnections;
    }

    public void disconnectAllNodes() {
        for (BlockPos pos : connections) {
            BlockEntity be = level.getBlockEntity(getWorldPos(pos));
            if (be instanceof BaseLaserBE) {
                ((BaseLaserBE) be).removeNode(getRelativePos(be.getBlockPos()));
            }
        }
    }

    //Misc Methods for TE's
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

    /*@Override
    public void setRemoved() {
        if (!level.isClientSide())
            disconnectAllNodes();
        super.setRemoved();
    }*/

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().above(10).north(10).east(10), getBlockPos().below(10).south(10).west(10));
    }

}
