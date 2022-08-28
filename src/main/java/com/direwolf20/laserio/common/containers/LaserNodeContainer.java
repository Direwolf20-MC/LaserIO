package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.Registration;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.UUID;

public class LaserNodeContainer extends AbstractContainerMenu {
    public static int SLOTS = 25;
    public static final int CARDHOLDERSLOTS = 15;
    public static final int CARDSLOTS = 9;
    public Player playerEntity;
    private IItemHandler playerInventory;
    ContainerLevelAccess containerLevelAccess;
    public ItemStack cardHolder;
    public IItemHandler cardHolderHandler;
    public UUID cardHolderUUID;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public LaserNodeBE tile;
    public byte side;

    public static final Direction[] DIRS = {
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
    };

    public LaserNodeContainer(int windowId, Inventory playerInventory, Player player, FriendlyByteBuf extraData) {
        this((LaserNodeBE) playerInventory.player.level.getBlockEntity(extraData.readBlockPos()), windowId, extraData.readByte(), playerInventory, player, new LaserNodeItemHandler(SLOTS), ContainerLevelAccess.NULL, extraData.readItem());
    }

    public LaserNodeContainer(@Nullable LaserNodeBE tile, int windowId, byte side, Inventory playerInventory, Player player, LaserNodeItemHandler handler, ContainerLevelAccess containerLevelAccess, ItemStack cardHolder) {
        super(Registration.LaserNode_Container.get(), windowId);
        this.playerEntity = player;
        this.tile = tile;
        this.side = side;
        this.playerInventory = new InvWrapper(playerInventory);
        this.containerLevelAccess = containerLevelAccess;
        if (handler != null) {
            addSlotBox(handler, 0, 62, 32, 3, 18, 3, 18);
            addSlotRange(handler, 9, 152, 78, 1, 18);
        }
        this.cardHolder = cardHolder;
        //if (!cardHolder.equals(ItemStack.EMPTY)) {
        this.cardHolderHandler = cardHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(new ItemStackHandler(CardHolderContainer.SLOTS));
        addSlotBox(cardHolderHandler, 0, -42, 32, 5, 18, 3, 18);
        cardHolderUUID = CardHolder.getUUID(cardHolder);
        //}
        layoutPlayerInventorySlots(8, 99);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0) {
            if (slotId < SLOTS && slots.get(slotId) instanceof CardHolderSlot) {
                ItemStack carriedItem = getCarried();
                ItemStack stackInSlot = slots.get(slotId).getItem();
                if (!carriedItem.isEmpty() && !stackInSlot.isEmpty() && !ItemStack.isSameItemSameTags(carriedItem, stackInSlot))
                    return;
            } else {
                ItemStack slotItem = slots.get(slotId).getItem();
                if (slotItem.getItem() instanceof CardHolder)
                    return;
            }
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        if (cardHolder.isEmpty() && cardHolderUUID != null) {
            //System.out.println("Lost card holder!");
            Inventory playerInventory = playerEntity.getInventory();
            for (int i = 0; i < playerInventory.items.size(); i++) {
                ItemStack itemStack = playerInventory.items.get(i);
                if (itemStack.getItem() instanceof CardHolder) {
                    if (CardHolder.getUUID(itemStack).equals(cardHolderUUID)) {
                        cardHolder = itemStack;
                        break;
                    }
                }
            }
        }
        return stillValid(containerLevelAccess, playerEntity, Registration.LaserNode.get());
    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int fromSlot, int toSlot, boolean p_38907_) {
        //return super.moveItemStackTo(itemStack, fromSlot, toSlot, p_38907_);
        //System.out.println(itemStack + ":" + fromSlot + ":" + toSlot + ":" + p_38907_);
        boolean flag = false;
        int i = fromSlot;
        if (p_38907_) {
            i = toSlot - 1;
        }

        //if (itemStack.isStackable()) {
        while (!itemStack.isEmpty()) {
            if (p_38907_) {
                if (i < fromSlot) {
                    break;
                }
            } else if (i >= toSlot) {
                break;
            }

            Slot slot = this.slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemstack)) {
                int j = itemstack.getCount() + itemStack.getCount();
                int maxSize = Math.min(slot.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                if (j <= maxSize) {
                    itemStack.setCount(0);
                    itemstack.setCount(j);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    itemStack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.setChanged();
                    flag = true;
                }
            }

            if (p_38907_) {
                --i;
            } else {
                ++i;
            }
        }
        //}

        if (!itemStack.isEmpty()) {
            if (p_38907_) {
                i = toSlot - 1;
            } else {
                i = fromSlot;
            }

            while (true) {
                if (p_38907_) {
                    if (i < fromSlot) {
                        break;
                    }
                } else if (i >= toSlot) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(itemStack) && slot1.getItem().getCount() < slot1.getMaxStackSize(itemStack)) {
                    if (itemStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(itemStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(itemStack.split(slot1.getMaxStackSize(itemStack)));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (p_38907_) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        if (slot instanceof CardHolderSlot || slot instanceof LaserNodeSlot)
            return false;
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        ItemStack stack = slot.getItem();
        if (slot instanceof CardHolderSlot) { //If we click on a cardHolder slot
            ItemStack stackToMove = stack.split(1);
            //Try to move 1 card to the node slots first, failing that, to the inventory!
            if (this.moveItemStackTo(stackToMove, 0, CARDSLOTS, false)) {
                return ItemStack.EMPTY;
            } else if (this.moveItemStackTo(stackToMove, SLOTS, 36 + SLOTS, true)) {
                return ItemStack.EMPTY;
            } else {
                stack.grow(1);
                return ItemStack.EMPTY;
            }
        } else if (index < CARDSLOTS) { //If its a node CARD slot
            if (!cardHolder.isEmpty()) { //Do the below set of logic if we have a card holder, otherwise just try to move to inventory
                if (this.moveItemStackTo(stack, CARDSLOTS + 1, SLOTS, false)) { //Move to card holder
                    if (!playerIn.level.isClientSide() && !(tile == null)) {
                        tile.updateThisNode();
                    }
                    return ItemStack.EMPTY;
                } else if (super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) { //Move to inventory
                    if (!playerIn.level.isClientSide() && !(tile == null)) {
                        tile.updateThisNode();
                    }
                    return ItemStack.EMPTY;
                }
            } else {
                if (super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) { //Move to inventory
                    if (!playerIn.level.isClientSide() && !(tile == null)) {
                        tile.updateThisNode();
                    }
                    return ItemStack.EMPTY;
                }
            }
        } else { //If its not a cardHolder slot nor a Card slot in the node - it must be the overclocker slot or the inventory....
            if (stack.getItem() instanceof OverclockerNode) {
                itemstack = stack.copy();
                //If its one of the 9 slots at the top try to move it into your inventory
                if (!cardHolder.isEmpty()) { //Do the below set of logic if we have a card holder, otherwise just try to move to inventory
                    if (index < SLOTS) {
                        if (!super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) {
                            return ItemStack.EMPTY;
                        }
                        slot.onQuickCraft(stack, itemstack);
                    } else {
                        if (!super.moveItemStackTo(stack, 0, SLOTS - CARDHOLDERSLOTS, false)) {
                            return ItemStack.EMPTY;
                        }
                        if (!playerIn.level.isClientSide() && !(tile == null)) {
                            tile.updateThisNode();
                        }
                    }
                } else {
                    if (index < SLOTS) {
                        if (!super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) {
                            return ItemStack.EMPTY;
                        }
                        slot.onQuickCraft(stack, itemstack);
                    } else {
                        if (!super.moveItemStackTo(stack, 0, SLOTS - CARDHOLDERSLOTS, false)) {
                            return ItemStack.EMPTY;
                        }
                        if (!playerIn.level.isClientSide() && !(tile == null)) {
                            tile.updateThisNode();
                        }
                    }
                }

                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (stack.getCount() == itemstack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(playerIn, stack);
                return itemstack;
            } else if (stack.getItem() instanceof BaseCard) { //If its a baseCard - it must be in the inventory, since these don't fit in the other slot....
                if (!cardHolder.isEmpty()) { //Do the below set of logic if we have a card holder, otherwise just try to move to inventory
                    if (super.moveItemStackTo(stack, 0, CARDSLOTS, false))
                        return ItemStack.EMPTY;
                    else if (this.moveItemStackTo(stack, CARDSLOTS + 1, SLOTS, false)) //Move to Card Holder
                        return ItemStack.EMPTY;
                } else {
                    if (super.moveItemStackTo(stack, 0, CARDSLOTS, false)) //Move to node
                        return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }


    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (handler instanceof LaserNodeItemHandler && index < 9)
                addSlot(new LaserNodeSlot(handler, index, x, y));
            else if ((handler.getSlots() == CardHolderContainer.SLOTS))
                addSlot(new CardHolderSlot(handler, index, x, y));
            else
                addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    public Direction getDirection(){
        if (side == -1)
            return null;
        return DIRS[side];
    }

    public BlockState getBlockStateFaced(){
        if (tile == null)
            return null;
        var dir = getDirection();
        if (dir == null)
            return null;
        var world = playerEntity.getLevel();
        return world.getBlockState(tile.getBlockPos().relative(dir));
    }
}
