package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FilterBasicHandler extends ItemAccessItemHandler {
    public ItemStack stack;

    public FilterBasicHandler(int size, ItemStack itemStack) {
        super(makeAccess(itemStack), LaserIODataComponents.ITEMSTACK_HANDLER.get(), size);
        this.stack = itemStack;
    }

    private static ItemAccess makeAccess(ItemStack stack) {
        return stack.isEmpty() ? ItemAccess.forStack(new ItemStack(Items.STONE)) : ItemAccess.forStack(stack);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return 1;
    }

    public void set(int index, ItemResource resource, int amount) {
        ItemResource accessResource = itemAccess.getResource();
        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) return;
        ItemResource updated = update(accessResource, index, resource, amount);
        if (updated.isEmpty()) return;
        try (Transaction tx = Transaction.openRoot()) {
            itemAccess.exchange(updated, accessAmount, tx);
            tx.commit();
        }
    }
}
