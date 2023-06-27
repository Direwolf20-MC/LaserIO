package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorAdvBE extends BaseLaserBE {
    protected BlockPos partnerBlockPos;

    public LaserConnectorAdvBE(BlockPos pos, BlockState state) {
        super(Registration.LaserConnectorAdv_BE.get(), pos, state);
    }

    public BlockPos getPartnerBlockPos() {
        return partnerBlockPos;
    }

    public void setPartnerBlockPos(BlockPos partnerBlockPos) {
        this.partnerBlockPos = partnerBlockPos;
        markDirtyClient();
    }

    public boolean isPartnerNodeConnected(BlockPos pos) {
        return partnerBlockPos == null ? false : partnerBlockPos.equals(pos);
    }

    public void handleAdvancedConnection(LaserConnectorAdvBE be) {
        BlockPos connectingPos = be.getBlockPos();
        if (isPartnerNodeConnected(connectingPos)) { //If these nodes are already connected, disconnect them
            removePartnerConnection();
        } else {
            addPartnerConnection(connectingPos, be);
        }
    }

    /**
     * @param connectingPos The Position in world you're connecting this TE to.
     * @param be            The block entity being connected to this one (And vice versa)
     *                      Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */

    public void addPartnerConnection(BlockPos connectingPos, LaserConnectorAdvBE be) {
        if (getPartnerBlockPos() != null) { //Advanced Connections are 1-1
            removePartnerConnection();
        }

        if (be.getPartnerBlockPos() != null) { //Advanced Connections are 1-1
                be.removePartnerConnection();
        }

        setPartnerBlockPos(connectingPos); // Add that node to this one
        be.setPartnerBlockPos(getBlockPos()); // Add this node to that one
        if (getColor().equals(getDefaultColor()) && !(be.getColor().equals(be.getDefaultColor())))
            setColor(be.getColor(), getWrenchAlpha());
        else if (be.getColor().equals(be.getDefaultColor()) && !(getColor().equals(getDefaultColor())))
            be.setColor(getColor(), getWrenchAlpha());
        else
            setColor(be.getColor(), getWrenchAlpha());
        discoverAllNodes(); //Re discover this new network

    }

    /**
     *   Disconnects This Pos from Target Pos, and disconnects Target Pos from This pos
     */

    public void removePartnerConnection() {
        if (getPartnerBlockPos() != null) {
            BlockEntity partnerBE = level.getBlockEntity(getPartnerBlockPos());
            if (partnerBE instanceof LaserConnectorAdvBE be) {
                be.setPartnerBlockPos(null); // Remove this node from that one
                be.discoverAllNodes();
            }
        }
        setPartnerBlockPos(null); // Remove that node from this one
        discoverAllNodes(); //Re discover on both nodes in case we have separated 2 networks
    }

    /** Disconnect ALL connected nodes - called when the block is broken for example */
    @Override
    public void disconnectAllNodes() {
        removePartnerConnection();
        super.disconnectAllNodes();
    }

    /** Validates the connections are still valid -- for use if a block is moved **/
    @Override
    public void validateConnections(BlockPos originalPos) {
        //TODO Handle this for ADV Lasers
        super.validateConnections(originalPos);
    }

    /** Misc Methods for TE's */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("partnerPos"))
            setPartnerBlockPos(NbtUtils.readBlockPos(tag.getCompound("partnerPos")));
        else
            setPartnerBlockPos(null);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (getPartnerBlockPos() != null)
            tag.put("partnerPos", NbtUtils.writeBlockPos(getPartnerBlockPos()));
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

}
