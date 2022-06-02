package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackHandlerProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<ItemStackHandler> holder;

    public ItemStackHandlerProvider(int nSlots) {
        holder = LazyOptional.of(() -> new ItemStackHandler(nSlots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return (stack.getItem() instanceof BaseCard);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return 64;
            }
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, holder.cast());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CompoundTag serializeNBT() {
        return holder.map(ItemStackHandler::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        holder.ifPresent(c -> c.deserializeNBT(nbt));
    }
}
