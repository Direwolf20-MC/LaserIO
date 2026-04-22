package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CardHolderItemStackHandler extends ItemAccessItemHandler {
    public CardHolderItemStackHandler(int size, ItemAccess itemAccess) {
        super(itemAccess, LaserIODataComponents.ITEMSTACK_HANDLER.get(), size);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (!super.isValid(index, resource)) return false;
        if (resource.isEmpty()) return true;
        return (resource.getItem() instanceof BaseCard
                || resource.getItem() instanceof BaseFilter
                || resource.getItem() instanceof OverclockerCard
                || resource.getItem() instanceof OverclockerNode);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return 64;
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
