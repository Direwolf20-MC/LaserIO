package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
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
                return (stack.getItem() instanceof BaseCard || stack.getItem() instanceof BaseFilter || stack.getItem() instanceof OverclockerCard || stack.getItem() instanceof OverclockerNode);
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
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, holder.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return holder.map(ItemStackHandler::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        holder.ifPresent(c -> c.deserializeNBT(nbt));
    }
}
