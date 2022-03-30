package com.direwolf20.laserio.common.blockentities.basebe;

import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class BaseLaserBE extends BlockEntity {
    protected final Set<BlockPos> connections = new HashSet<>();

    public BaseLaserBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type,pos,state);
    }

    //TODO See why block position's aren't saved on reload

    //TODO See if we still need to MarkDirtyClient
    public boolean addNode(BlockPos pos) {
        boolean success = connections.add(pos);
        if (success) {
            //markDirtyClient();
        }
        return success;
    }

    public boolean removeNode(BlockPos pos) {
        boolean success = connections.remove(pos);
        if (success) {
            //markDirtyClient();
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
            if (!be.addNode(this.getBlockPos())) {
                removeNode(pos);
                return false;
            }
        }
        return success;
    }

    public boolean removeConnection(BlockPos pos) {
        boolean success = removeNode(pos);
        if (success) {
            BaseLaserBE be = (BaseLaserBE) level.getBlockEntity(pos);
            be.removeNode(this.getBlockPos());
        }
        return success;
    }

    public Set<BlockPos> getConnections() {
        return connections;
    }

    public void disconnectAllNodes() {
        for (BlockPos pos : connections) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BaseLaserBE) {
                ((BaseLaserBE) be).removeNode(this.getBlockPos());
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
    }

    @Override
    public void setRemoved() {
        if (!level.isClientSide())
            disconnectAllNodes();
        super.setRemoved();
    }

}
