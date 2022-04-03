package com.direwolf20.laserio.common.blockentities;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.CustomItemHandlers.NodeItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class LaserNodeBE extends BaseLaserBE {
    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    private final ItemStackHandler[] itemHandler = new ItemStackHandler[6];
    private final LazyOptional<IItemHandler>[] handler = new LazyOptional[6];
    private final IItemHandler EMPTY = new ItemStackHandler(0);

    private final Set<BlockPos> otherNodesInNetwork = new HashSet<>();

    public LaserNodeBE(BlockPos pos, BlockState state) {
        super(Registration.LaserNode_BE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            final int j = direction.ordinal();
            itemHandler[j] = new NodeItemHandler(9, this);
            handler[j] = LazyOptional.of(() -> itemHandler[j]);
        }
    }

    public void tickServer() {
        for (Direction direction : Direction.values()) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack card = itemHandler[direction.ordinal()].getStackInSlot(slot);
                if (card.getItem() instanceof BaseCard) {
                    if (BaseCard.getTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        sendItems(card, direction);
                    } else if (BaseCard.getTransferMode(card).equals(BaseCard.TransferMode.EXTRACT)) {
                        getItems(card, direction);
                    }
                }
            }
        }
    }

    public void sendItems(ItemStack card, Direction direction) {
        IItemHandler adjacentInventory = getAttachedInventory(direction);
        if (adjacentInventory != null) {
            System.out.println("Sending from: " + getBlockPos().relative(direction));
            System.out.println("Slot count:" + adjacentInventory.getSlots() + ".  First Item: " + adjacentInventory.getStackInSlot(0));
        }
    }

    public void getItems(ItemStack card, Direction direction) {
        IItemHandler adjacentInventory = getAttachedInventory(direction);
        if (adjacentInventory != null) {
            System.out.println("Getting for: " + getBlockPos().relative(direction));
        }
    }

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
