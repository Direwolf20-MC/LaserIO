package com.direwolf20.laserio.common.containers.customhandler;

import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CardItemHandler extends ItemAccessItemHandler {
    public ItemStack stack;

    public CardItemHandler(int size, ItemStack itemStack) {
        super(makeAccess(itemStack), LaserIODataComponents.ITEMSTACK_HANDLER.get(), size);
        this.stack = itemStack;
    }

    private static ItemAccess makeAccess(ItemStack stack) {
        // ItemAccess.forStack requires non-empty; fallback to a throwaway dummy for empty contexts
        return stack.isEmpty() ? ItemAccess.forStack(new ItemStack(Items.STONE)) : ItemAccess.forStack(stack);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (!super.isValid(index, resource)) return false;
        if (resource.isEmpty()) return true;
        if (this.stack.getItem() instanceof CardEnergy)
            return resource.getItem() instanceof OverclockerCard;
        if (index == 0)
            return resource.getItem() instanceof BaseFilter;
        return resource.getItem() instanceof OverclockerCard;
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        if (this.stack.getItem() instanceof CardEnergy)
            return 4;
        if (index == 0)
            return 1;
        return 4;
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
