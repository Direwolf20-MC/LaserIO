package com.direwolf20.laserio.util;

/*public class ItemStackHandlerProvider implements INBTSerializable<CompoundTag> {
    private final ItemStackHandler holder;

    public ItemStackHandlerProvider(int nSlots) {
        holder = new ItemStackHandler(nSlots) {
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
        };
    }

    /*@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, holder.cast());
    }*/

    /*@Override
    public CompoundTag serializeNBT() {
        return holder.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        holder.deserializeNBT(nbt);
    }
}*/
