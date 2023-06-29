package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.DimBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorAdvBE extends BaseLaserBE {
    protected DimBlockPos partnerDimBlockPos;

    public LaserConnectorAdvBE(BlockPos pos, BlockState state) {
        super(Registration.LaserConnectorAdv_BE.get(), pos, state);
    }

    public DimBlockPos getPartnerDimBlockPos() {
        return partnerDimBlockPos;
    }

    public void setPartnerDimBlockPos(DimBlockPos partnerDimBlockPos) {
        this.partnerDimBlockPos = partnerDimBlockPos;
        markDirtyClient();
    }

    public boolean isPartnerNodeConnected(DimBlockPos pos) {
        return partnerDimBlockPos != null && partnerDimBlockPos.equals(pos);
    }

    public void handleAdvancedConnection(LaserConnectorAdvBE be) {
        DimBlockPos connectingDimPos = new DimBlockPos(be.getLevel(), be.getBlockPos());
        if (isPartnerNodeConnected(connectingDimPos)) { //If these nodes are already connected, disconnect them
            removePartnerConnection();
        } else {
            addPartnerConnection(connectingDimPos, be);
        }
    }

    /**
     * @param connectingDimPos The Position in world you're connecting this TE to.
     * @param be            The block entity being connected to this one (And vice versa)
     *                      Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */

    public void addPartnerConnection(DimBlockPos connectingDimPos, LaserConnectorAdvBE be) {
        if (getPartnerDimBlockPos() != null) { //Advanced Connections are 1-1
            removePartnerConnection();
        }

        if (be.getPartnerDimBlockPos() != null) { //Advanced Connections are 1-1
                be.removePartnerConnection();
        }

        setPartnerDimBlockPos(connectingDimPos); // Add that node to this one
        be.setPartnerDimBlockPos(new DimBlockPos(getLevel(), getBlockPos())); // Add this node to that one
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
        if (getPartnerDimBlockPos() != null) {
            DimBlockPos partnerDimPos = getPartnerDimBlockPos();
            BlockEntity partnerBE = partnerDimPos.getLevel(level.getServer()).getBlockEntity(partnerDimPos.blockPos);
            if (partnerBE instanceof LaserConnectorAdvBE be) {
                be.setPartnerDimBlockPos(null); // Remove this node from that one
                be.discoverAllNodes();
            }
        }
        setPartnerDimBlockPos(null); // Remove that node from this one
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
        if (tag.contains("partnerDimPos"))
            setPartnerDimBlockPos(new DimBlockPos(tag.getCompound("partnerDimPos")));
        else
            setPartnerDimBlockPos(null);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (getPartnerDimBlockPos() != null)
            tag.put("partnerDimPos", getPartnerDimBlockPos().toNBT());
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
