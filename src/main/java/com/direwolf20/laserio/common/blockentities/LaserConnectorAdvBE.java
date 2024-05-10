package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.setup.Registration;
import com.direwolf20.laserio.util.MiscTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LaserConnectorAdvBE extends BaseLaserBE {
    protected GlobalPos partnerGlobalPos;

    public LaserConnectorAdvBE(BlockPos pos, BlockState state) {
        super(Registration.LaserConnectorAdv_BE.get(), pos, state);
    }

    public GlobalPos getPartnerGlobalPos() {
        return partnerGlobalPos;
    }

    public void setPartnerGlobalPos(GlobalPos partnerGlobalPos) {
        this.partnerGlobalPos = partnerGlobalPos;
        markDirtyClient();
    }

    public boolean isPartnerNodeConnected(GlobalPos pos) {
        return partnerGlobalPos != null && partnerGlobalPos.equals(pos);
    }

    public void handleAdvancedConnection(LaserConnectorAdvBE be) {
        GlobalPos connectingDimPos = new GlobalPos(be.getLevel().dimension(), be.getBlockPos());
        if (isPartnerNodeConnected(connectingDimPos)) { //If these nodes are already connected, disconnect them
            removePartnerConnection();
        } else {
            addPartnerConnection(connectingDimPos, be);
        }
    }

    /**
     * @param connectingDimPos The Position in world you're connecting this TE to.
     * @param be               The block entity being connected to this one (And vice versa)
     *                         Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */

    public void addPartnerConnection(GlobalPos connectingDimPos, LaserConnectorAdvBE be) {
        if (getPartnerGlobalPos() != null) { //Advanced Connections are 1-1
            removePartnerConnection();
        }

        if (be.getPartnerGlobalPos() != null) { //Advanced Connections are 1-1
            be.removePartnerConnection();
        }

        setPartnerGlobalPos(connectingDimPos); // Add that node to this one
        be.setPartnerGlobalPos(new GlobalPos(getLevel().dimension(), getBlockPos())); // Add this node to that one
        if (getColor().equals(getDefaultColor()) && !(be.getColor().equals(be.getDefaultColor())))
            setColor(be.getColor(), getWrenchAlpha());
        else if (be.getColor().equals(be.getDefaultColor()) && !(getColor().equals(getDefaultColor())))
            be.setColor(getColor(), getWrenchAlpha());
        else
            setColor(be.getColor(), getWrenchAlpha());
        discoverAllNodes(); //Re discover this new network

    }

    /**
     * Disconnects This Pos from Target Pos, and disconnects Target Pos from This pos
     */

    public void removePartnerConnection() {
        if (getPartnerGlobalPos() != null) {
            GlobalPos partnerDimPos = getPartnerGlobalPos();
            BlockEntity partnerBE = MiscTools.getLevel(level.getServer(), partnerDimPos).getBlockEntity(partnerDimPos.pos());
            if (partnerBE instanceof LaserConnectorAdvBE be) {
                be.setPartnerGlobalPos(null); // Remove this node from that one
                be.discoverAllNodes();
            }
        }
        setPartnerGlobalPos(null); // Remove that node from this one
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
        //If we got here, it means the Adv Laser connector was moved, and so we assume it needs to update its partner
        GlobalPos partner = getPartnerGlobalPos();
        if (partner == null) {
            removePartnerConnection();
            super.validateConnections(originalPos);
            return;
        }
        BlockEntity be = MiscTools.getLevel(getLevel().getServer(), partner).getBlockEntity(partner.pos());
        if (be instanceof LaserConnectorAdvBE laserConnectorAdvBE) {
            addPartnerConnection(partner, laserConnectorAdvBE); //If the partner still exists at the old spot, connect them, else remove
        } else {
            removePartnerConnection();
        }
        super.validateConnections(originalPos);
    }

    /** Misc Methods for TE's */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("partnerDimPos"))
            setPartnerGlobalPos(MiscTools.nbtToGlobalPos(tag.getCompound("partnerDimPos")));
        else
            setPartnerGlobalPos(null);
        super.loadAdditional(tag, provider);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (getPartnerGlobalPos() != null)
            tag.put("partnerDimPos", MiscTools.globalPosToNBT(getPartnerGlobalPos()));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(pkt.getTag(), lookupProvider);
    }

}
