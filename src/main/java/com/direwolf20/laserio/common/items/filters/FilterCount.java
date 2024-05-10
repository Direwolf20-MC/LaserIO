package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class FilterCount extends BaseFilter {
    public FilterCount() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterCountContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    /** Get MB for fluids - should be less than 1000 **/
    public static int getSlotAmount(ItemStack stack, int getSlot) {
        if (!stack.has(LaserIODataComponents.FILTER_COUNT_MBAMT))
            return 0; //If we don't have the data, it must be zero, as it was never set
        List<Integer> slotAmts = stack.get(LaserIODataComponents.FILTER_COUNT_MBAMT);
        return slotAmts.get(getSlot);
    }

    public static void setSlotAmount(ItemStack stack, int getSlot, int setMBAmount) {
        if (!stack.has(LaserIODataComponents.FILTER_COUNT_MBAMT)) {
            List<Integer> list = new ArrayList<>(FilterCountContainer.SLOTS);
            for (int i = 0; i < FilterCountContainer.SLOTS; i++) {
                list.add(0); //Prefill with a list of size 0
            }
            stack.set(LaserIODataComponents.FILTER_COUNT_MBAMT, list);
        }
        List<Integer> slotAmts = stack.get(LaserIODataComponents.FILTER_COUNT_MBAMT);
        int amtToPut = setMBAmount % 1000;
        int stackSize = setMBAmount / 1000;
        setSlotCount(stack, getSlot, stackSize);
        slotAmts.set(getSlot, amtToPut);

        //Now check if they are all zeros, and remove if so
        boolean allZeros = true;
        for (int i = 0; i < FilterCountContainer.SLOTS; i++) {
            if (slotAmts.get(i) != 0) {
                allZeros = false;
                break;
            }
        }
        if (allZeros)
            stack.remove(LaserIODataComponents.FILTER_COUNT_MBAMT);
        else
            stack.set(LaserIODataComponents.FILTER_COUNT_MBAMT, slotAmts);
    }

    /**Special handling for slots with > 127 items in them**/
    public static int getSlotCount(ItemStack stack, int getSlot) {
        if (!stack.has(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS))
            return 0; //If we don't have the data, it must be zero, as it was never set
        List<Integer> slotAmts = stack.get(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS);
        return slotAmts.get(getSlot);
    }

    public static void setSlotCount(ItemStack stack, int getSlot, int setCount) {
        if (!stack.has(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS)) {
            List<Integer> list = new ArrayList<>(FilterCountContainer.SLOTS);
            for (int i = 0; i < FilterCountContainer.SLOTS; i++) {
                list.add(0); //Prefill with a list of size 0
            }
            stack.set(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS, list);
        }
        List<Integer> slotAmts = stack.get(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS);

        slotAmts.set(getSlot, setCount);
        /*if (!(mbCount == setCount || mbAmt == 0)) {
            setSlotAmount(stack, getSlot, setCount * 1000 + (mbAmt % 1000));
        }*/

        //Now check if they are all zeros, and remove if so
        boolean allZeros = true;
        for (int i = 0; i < FilterCountContainer.SLOTS; i++) {
            if (slotAmts.get(i) != 0) {
                allZeros = false;
                break;
            }
        }
        if (allZeros)
            stack.remove(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS);
        else
            stack.set(LaserIODataComponents.FILTER_COUNT_SLOT_COUNTS, slotAmts);
    }

    public static FilterCountHandler getInventory(ItemStack stack) {
        FilterCountHandler handler = new FilterCountHandler(FilterCountContainer.SLOTS, stack);
        for (int i = 0; i < FilterCountContainer.SLOTS; i++) {
            ItemStack itemStack = handler.getStackInSlot(i);
            //itemStack.setCount(getSlotCount(stack, i));
            handler.setStackInSlot(i, itemStack);
        }
        return handler;
    }

    /*public static FilterCountHandler setInventory(ItemStack stack, FilterCountHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        ListTag countList = new ListTag();
        for (int i = 0; i < handler.getSlots(); i++) {
            CompoundTag countTag = new CompoundTag();
            ItemStack itemStack = handler.getStackInSlot(i);
            countTag.putInt("Slot", i);
            if (doesItemStackHoldFluids(itemStack) || (MekanismIntegration.isLoaded() && doesItemStackHoldChemicals(itemStack))) {
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
            countList.add(countTag);
        }
        stack.getOrCreateTag().put("counts", countList);
        return handler;
    }*/

    public static boolean doesItemStackHoldFluids(ItemStack stack) {
        Optional<IFluidHandlerItem> fluidHandlerLazyOptional = FluidUtil.getFluidHandler(stack);
        if (!fluidHandlerLazyOptional.isPresent()) {
            return false;
        }
        FluidStack fluidStack = FluidStack.EMPTY;
        IFluidHandler fluidHandler = fluidHandlerLazyOptional.get();
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
        return stack.getOrDefault(LaserIODataComponents.FILTER_ALLOW, true);
    }

    public static boolean setAllowList(ItemStack stack, boolean allowList) {
        if (!allowList)
            stack.remove(LaserIODataComponents.FILTER_ALLOW);
        else
            stack.set(LaserIODataComponents.FILTER_ALLOW, true);
        return allowList;
    }
}
