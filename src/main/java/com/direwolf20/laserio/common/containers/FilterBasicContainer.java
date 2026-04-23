package com.direwolf20.laserio.common.containers;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.LaserIORegistration;
import com.direwolf20.laserio.util.NodeSideCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;


public class FilterBasicContainer extends AbstractContainerMenu {
    public static final int SLOTS = 15;
    public FilterBasicHandler handler;
    public ItemStack filterItem;
    public ItemStack sourceCard = ItemStack.EMPTY;
    public Player playerEntity;
    private Inventory playerInventory;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public byte direction = -1;
    public int slotIndex = -1;

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInventory, player, ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData));
        this.sourceCard = ItemStack.OPTIONAL_STREAM_CODEC.decode(extraData);
        this.direction = extraData.readByte();
        this.slotIndex = extraData.readVarInt();
    }

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, ItemStack filterItem) {
        super(LaserIORegistration.FilterBasic_Container.get(), windowId);
        playerEntity = player;
        this.handler = new FilterBasicHandler(SLOTS, filterItem);
        this.playerInventory = playerInventory;
        this.filterItem = filterItem;
        if (handler != null)
            addSlotBox(handler, 0, 44, 22, 5, 18, 3, 18);

        layoutPlayerInventorySlots(8, 84);
    }

    public FilterBasicContainer(int windowId, Inventory playerInventory, Player player, BlockPos sourcePos, ItemStack filterItem, ItemStack sourceCard, byte direction, int slotIndex) {
        this(windowId, playerInventory, player, filterItem);
        this.sourceContainer = sourcePos;
        this.sourceCard = sourceCard;
        this.direction = direction;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < SLOTS) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem().copy();
            if (ItemStack.isSameItemSameComponents(currentStack, filterItem)) return ItemStack.EMPTY;
            currentStack.setCount(1);
            //Only do this if we click from the players inventory
            if (index >= SLOTS) {
                for (int i = 0; i < SLOTS; i++) { //Prevents the same item from going in there more than once.
                    if (ItemStack.isSameItemSameComponents(this.slots.get(i).getItem(), currentStack)) //Don't limit tags
                        return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(currentStack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return itemstack;
    }

    private int addSlotRange(FilterBasicHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new FilterBasicSlot(handler, handler::set, index, x, y, false));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(FilterBasicHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
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

    @Override
    public void removed(Player playerIn) {
        Level world = playerIn.level();
        if (!world.isClientSide()) {
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                BlockEntity blockEntity = world.getBlockEntity(sourceContainer);
                if (blockEntity instanceof LaserNodeBE node) {
                    writeFilterBackToNode(node);
                    node.updateThisNode();
                }
            }
        }
        super.removed(playerIn);
    }

    private void writeFilterBackToNode(LaserNodeBE node) {
        if (sourceCard.isEmpty() || direction < 0 || direction >= 6 || slotIndex < 0) return;
        // Push filterItem into sourceCard's internal card inventory (slot 0). That handler wraps
        // ItemAccess.forStack(sourceCard), so the write updates sourceCard's components in place.
        CardItemHandler cardHandler = BaseCard.getInventory(sourceCard);
        if (cardHandler != null) cardHandler.set(0, ItemResource.of(filterItem), filterItem.getCount());
        // Then push sourceCard back to the node's side handler so the component changes persist.
        NodeSideCache cache = node.nodeSideCaches[direction];
        if (cache != null && slotIndex < cache.itemHandler.size())
            cache.itemHandler.set(slotIndex, ItemResource.of(sourceCard), sourceCard.getCount());
    }
}
