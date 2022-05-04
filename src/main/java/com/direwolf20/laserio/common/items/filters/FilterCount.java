package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class FilterCount extends BaseFilter {
    public FilterCount() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterCountHandler handler = getInventory(itemstack);

        ContainerData slotCounts = new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 15)
                    return getInventory(itemstack).getStackInSlot(index).getCount();
                else
                    throw new IllegalArgumentException("Invalid index: " + index);
            }

            @Override
            public void set(int index, int value) {
                throw new IllegalStateException("Cannot set values through IIntArray");
            }

            @Override
            public int getCount() {
                return 15;
            }
        };


        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, player, handler, itemstack, slotCounts), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeItem(ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterCountHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        FilterCountHandler handler = new FilterCountHandler(FilterCountContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        /**Special handling for slots with > 127 items in them**/
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            ItemStack itemStack = handler.getStackInSlot(slot);
            itemStack.setCount(countTag.getInt("Count"));
            handler.setStackInSlot(slot, itemStack);
        }
        return !compound.contains("inv") ? setInventory(stack, new FilterCountHandler(FilterCountContainer.SLOTS, stack)) : handler;
    }

    public static FilterCountHandler setInventory(ItemStack stack, FilterCountHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        /**Special handling for slots with > 127 items in them**/
        ListTag countList = new ListTag();
        for (int i = 0; i < handler.getSlots(); i++) {
            CompoundTag countTag = new CompoundTag();
            ItemStack itemStack = handler.getStackInSlot(i);
            if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                countTag.putInt("Slot", i);
                countTag.putInt("Count", itemStack.getCount());
                countList.add(countTag);
            }
        }
        stack.getOrCreateTag().put("counts", countList);
        return handler;
    }

    /** Filter Counts are always allowLists **/
    public static boolean getAllowList(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("allowList") ? setAllowList(stack, true) : compound.getBoolean("allowList");
    }

    public static boolean setAllowList(ItemStack stack, boolean allowList) {
        stack.getOrCreateTag().putBoolean("allowList", true);
        return true;
    }
}
