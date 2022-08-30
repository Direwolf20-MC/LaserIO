package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;

public class FilterCount extends BaseFilter {
    public FilterCount() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeItem(ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static int getSlotAmount(ItemStack stack, int getSlot) {
        CompoundTag compound = stack.getOrCreateTag();
        /**Get MB for fluids - should be less than 1000**/
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            if (slot == getSlot)
                return countTag.getInt("MBAmount");
        }
        return 0;
    }

    public static void setSlotAmount(ItemStack stack, int getSlot, int setMBAmount) {
        CompoundTag compound = stack.getOrCreateTag();
        /**Special handling for slots with > 127 items in them**/
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            if (slot == getSlot) {
                countTag.putInt("MBAmount", setMBAmount);
                if (setMBAmount == 0)
                    countTag.putInt("Count", 0);
                else
                    countTag.putInt("Count", Math.max(1, (int) Math.floor(setMBAmount / 1000)));
            }
        }
    }

    public static int getSlotCount(ItemStack stack, int getSlot) {
        CompoundTag compound = stack.getOrCreateTag();
        /**Special handling for slots with > 127 items in them**/
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            if (slot == getSlot)
                return countTag.getInt("Count");
        }
        return 0;
    }

    public static void setSlotCount(ItemStack stack, int getSlot, int setCount) {
        CompoundTag compound = stack.getOrCreateTag();
        /**Special handling for slots with > 127 items in them**/
        ListTag countList = compound.getList("counts", Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            if (slot == getSlot) {
                int mbAmt = getSlotAmount(stack, i);
                int mbCount = (int) Math.floor(mbAmt / 1000);
                if (mbCount == setCount || mbAmt == 0) {
                    countTag.putInt("Count", setCount);
                } else {
                    countTag.putInt("Count", setCount);
                    countTag.putInt("MBAmount", setCount * 1000 + (mbAmt % 1000));
                }
            }
        }
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
            /*int mbAmt = getSlotAmount(stack, i);
            if (mbAmt > 0)
                itemStack.setCount((int)Math.floor(mbAmt/1000));
            else*/
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
            countTag.putInt("Slot", i);
            if (doesItemStackHoldFluids(itemStack)) {
                int mbAmt = getSlotAmount(stack, i);
                if (mbAmt > 0) {
                    countTag.putInt("Count", Math.max(1, (int) Math.floor(mbAmt / 1000)));
                    countTag.putInt("MBAmount", mbAmt);
                } else {
                    countTag.putInt("Count", itemStack.getCount());
                    countTag.putInt("MBAmount", itemStack.getCount() * 1000);
                }
            } else {
                countTag.putInt("Count", itemStack.getCount());
                countTag.putInt("MBAmount", 0);
            }
            /*int mbAmt = getSlotAmount(stack, i);
            if (mbAmt > 0)
                countTag.putInt("Count", (int)Math.floor(mbAmt/1000));
            else
                countTag.putInt("Count", itemStack.getCount());
            if (doesItemStackHoldFluids(itemStack))
                countTag.putInt("MBAmount", itemStack.getCount() * 1000);
            else
                countTag.putInt("MBAmount", 0);*/
            countList.add(countTag);
        }
        stack.getOrCreateTag().put("counts", countList);
        return handler;
    }

    public static boolean doesItemStackHoldFluids(ItemStack stack) {
        LazyOptional<IFluidHandlerItem> fluidHandlerLazyOptional = FluidUtil.getFluidHandler(stack);
        if (!fluidHandlerLazyOptional.isPresent()) {
            return false;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        IFluidHandler fluidHandler = fluidHandlerLazyOptional.resolve().get();
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            fluidStack = fluidHandler.getFluidInTank(tank);
            if (!fluidStack.isEmpty())
                break;
        }
        if (fluidStack.isEmpty()) {
            return false;
        }
        return true;
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
