package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.items.upgrades.OverclockerCard;
import com.direwolf20.laserio.common.items.upgrades.OverclockerNode;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Storage backend for the card holder. Cards have {@code maxStackSize == 1} but the holder needs to stack
 * up to 64 of them per slot. The vanilla {@link net.minecraft.world.item.component.ItemContainerContents}
 * (used by {@link net.neoforged.neoforge.transfer.item.ItemAccessItemHandler}) strict-validates counts on
 * read via {@link net.minecraft.world.item.ItemStackTemplate#create()}, returning EMPTY for any count above
 * the item's max — which silently wiped earlier cards. This handler stores into a custom
 * {@link CardHolderContents} component instead.
 */
public class CardHolderItemStackHandler extends ItemAccessResourceHandler<ItemResource> {
    private final net.minecraft.world.item.Item validItem;

    public CardHolderItemStackHandler(int size, ItemAccess itemAccess) {
        super(itemAccess, size);
        this.validItem = itemAccess.getResource().getItem();
    }

    private CardHolderContents getContents(ItemResource accessResource) {
        return accessResource.getOrDefault(LaserIODataComponents.CARD_HOLDER_CONTENTS, CardHolderContents.EMPTY);
    }

    @Override
    protected ItemResource getResourceFrom(ItemResource accessResource, int index) {
        if (!accessResource.is(validItem)) return ItemResource.EMPTY;
        return getContents(accessResource).getResource(index);
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (!accessResource.is(validItem)) return 0;
        return getContents(accessResource).getCount(index);
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
        CardHolderContents updated = getContents(accessResource).with(index, newResource, newAmount);
        return accessResource.with(LaserIODataComponents.CARD_HOLDER_CONTENTS, updated);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (!itemAccess.getResource().is(validItem)) return false;
        if (resource.isEmpty()) return true;
        return resource.getItem() instanceof BaseCard
                || resource.getItem() instanceof BaseFilter
                || resource.getItem() instanceof OverclockerCard
                || resource.getItem() instanceof OverclockerNode;
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
