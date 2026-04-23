package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.containers.customslot.CardHolderSlot;
import com.direwolf20.laserio.common.containers.customslot.LaserNodeSlot;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.LaserIORegistration;
import com.direwolf20.laserio.util.CardHolderItemStackHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nullable;
import java.util.UUID;

public class LaserNodeContainer extends AbstractContainerMenu {
    public static int SLOTS = 25;
    public static final int CARDHOLDERSLOTS = 15;
    public static final int CARDSLOTS = 9;
    public Player playerEntity;
    private Inventory playerInventory;
    ContainerLevelAccess containerLevelAccess;
    public ItemStack cardHolder;
    public CardHolderItemStackHandler cardHolderHandler;
    public UUID cardHolderUUID;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public LaserNodeBE tile;
    public byte side;

    public LaserNodeContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this((LaserNodeBE) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), windowId, extraData.readByte(), playerInventory, player, new LaserNodeItemHandler(SLOTS), ContainerLevelAccess.NULL, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
    }

    public LaserNodeContainer(@Nullable LaserNodeBE tile, int windowId, byte side, Inventory playerInventory, Player player, LaserNodeItemHandler handler, ContainerLevelAccess containerLevelAccess, ItemStack cardHolder) {
        super(LaserIORegistration.LaserNode_Container.get(), windowId);
        this.playerEntity = player;
        this.tile = tile;
        this.side = side;
        this.playerInventory = playerInventory;
        this.containerLevelAccess = containerLevelAccess;
        if (handler != null) {
            addLaserNodeSlotBox(handler, 0, 62, 32, 3, 18, 3, 18);
            addLaserNodeSlotRange(handler, 9, 152, 78, 1, 18);
        }
        this.cardHolder = cardHolder;

        if (!cardHolder.isEmpty())
            cardHolderHandler = new CardHolderItemStackHandler(CardHolderContainer.SLOTS, ItemAccess.forStack(cardHolder));
        else
            cardHolderHandler = new CardHolderItemStackHandler(CardHolderContainer.SLOTS, ItemAccess.forStack(new ItemStack(Items.STONE)));
        addCardHolderSlotBox(cardHolderHandler, 0, -92, 32, 5, 18, 3, 18);
        cardHolderUUID = CardHolder.getUUID(cardHolder);

        layoutPlayerInventorySlots(8, 99);
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId >= 0) {
            if (slotId < SLOTS && slots.get(slotId) instanceof CardHolderSlot) {
                ItemStack carriedItem = getCarried();
                ItemStack stackInSlot = slots.get(slotId).getItem();
                if (stackInSlot.getMaxStackSize() == 1 && stackInSlot.getCount() > 1) {
                    if (!carriedItem.isEmpty() && !stackInSlot.isEmpty() && !ItemStack.isSameItemSameComponents(carriedItem, stackInSlot))
                        return;
                }
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
            Inventory playerInv = playerEntity.getInventory();
            for (int i = 0; i < playerInv.getNonEquipmentItems().size(); i++) {
                ItemStack itemStack = playerInv.getNonEquipmentItems().get(i);
                if (itemStack.getItem() instanceof CardHolder) {
                    if (CardHolder.getUUID(itemStack).equals(cardHolderUUID)) {
                        cardHolder = itemStack;
                        break;
                    }
                }
            }
        }
        return stillValid(containerLevelAccess, playerEntity, LaserIORegistration.LaserNode.get());
    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int fromSlot, int toSlot, boolean p_38907_) {
        boolean flag = false;
        int i = fromSlot;
        if (p_38907_) {
            i = toSlot - 1;
        }

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
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemstack)) {
                int j = itemstack.getCount() + itemStack.getCount();
                int maxSize = Math.min(slot.getMaxStackSize(), slot.getMaxStackSize(itemStack));
                if (j <= maxSize) {
                    itemStack.setCount(0);
                    itemstack.setCount(j);
                    slot.setByPlayer(itemstack);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    itemStack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.setByPlayer(itemstack);
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
            ItemStack stackToMove;
            if (stack.getMaxStackSize() == 1)
                stackToMove = stack.split(1);
            else
                stackToMove = stack;
            //Try to move 1 card to the node slots first, failing that, to the inventory!
            if (this.moveItemStackTo(stackToMove, 0, CARDSLOTS + 1, false)) {
                slot.set(stack);
                return ItemStack.EMPTY;
            } else if (this.moveItemStackTo(stackToMove, SLOTS, 36 + SLOTS, true)) {
                slot.set(stack);
                return ItemStack.EMPTY;
            } else {
                stack.grow(1);
                return ItemStack.EMPTY;
            }
        } else if (index < CARDSLOTS) { //If its a node CARD slot
            // Source is a ResourceHandlerSlot-backed LaserNodeSlot; stack is a detached copy.
            // moveItemStackTo mutates stack.count but never writes the decrement back to the
            // handler, so we must slot.set(stack) after moving to persist the removal.
            if (!cardHolder.isEmpty()) { //Do the below set of logic if we have a card holder, otherwise just try to move to inventory
                if (this.moveItemStackTo(stack, CARDSLOTS + 1, SLOTS, false)) { //Move to card holder
                    slot.set(stack);
                    if (!playerIn.level().isClientSide() && !(tile == null)) {
                        tile.updateThisNode();
                    }
                    return ItemStack.EMPTY;
                } else if (super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) { //Move to inventory
                    slot.set(stack);
                    if (!playerIn.level().isClientSide() && !(tile == null)) {
                        tile.updateThisNode();
                    }
                    return ItemStack.EMPTY;
                }
            } else {
                if (super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) { //Move to inventory
                    slot.set(stack);
                    if (!playerIn.level().isClientSide() && !(tile == null)) {
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
                        if (this.moveItemStackTo(stack, CARDSLOTS + 1, SLOTS, false)) { //Move to card holder
                            return ItemStack.EMPTY;
                        } else if (!super.moveItemStackTo(stack, SLOTS, 36 + SLOTS, true)) {
                            return ItemStack.EMPTY;
                        }
                        slot.onQuickCraft(stack, itemstack);
                    } else {
                        if (!super.moveItemStackTo(stack, 0, SLOTS - CARDHOLDERSLOTS, false)) {
                            return ItemStack.EMPTY;
                        }
                        if (!playerIn.level().isClientSide() && !(tile == null)) {
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
                        if (!playerIn.level().isClientSide() && !(tile == null)) {
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
            } else if (stack.getItem() instanceof BaseCard || stack.getItem() instanceof BaseFilter || stack.getItem() instanceof OverclockerCard) { //If its a baseCard - it must be in the inventory, since these don't fit in the other slot....
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


    private int addLaserNodeSlotRange(LaserNodeItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            if (index < 9)
                addSlot(new LaserNodeSlot(handler, handler::set, index, x, y));
            else
                addSlot(new ResourceHandlerSlot(handler, handler::set, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addLaserNodeSlotBox(LaserNodeItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addLaserNodeSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private int addCardHolderSlotRange(CardHolderItemStackHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new CardHolderSlot(handler, handler::set, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addCardHolderSlotBox(CardHolderItemStackHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addCardHolderSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, leftCol + col * 18, topRow + row * 18));
            }
        }
        // Hotbar
        topRow += 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, leftCol + col * 18, topRow));
        }
    }
}
