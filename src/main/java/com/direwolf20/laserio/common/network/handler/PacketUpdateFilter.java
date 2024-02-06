package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.data.UpdateFilterPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

public class PacketUpdateFilter {
    public static final PacketUpdateFilter INSTANCE = new PacketUpdateFilter();

    public static PacketUpdateFilter get() {
        return INSTANCE;
    }

    public void handle(final UpdateFilterPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> senderOptional = context.player();
            if (senderOptional.isEmpty())
                return;
            Player sender = senderOptional.get();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            if (container instanceof CardItemContainer) {
                ItemStack stack = container.slots.get(0).getItem();
                if (stack.isEmpty()) return;
                FilterBasic.setAllowList(stack, payload.allowList());
                FilterBasic.setCompareNBT(stack, payload.compareNBT());
            }
            if (container instanceof FilterBasicContainer) {
                ItemStack stack = ((FilterBasicContainer) container).filterItem;
                FilterBasic.setAllowList(stack, payload.allowList());
                FilterBasic.setCompareNBT(stack, payload.compareNBT());
            }
            if (container instanceof FilterCountContainer) {
                ItemStack stack = ((FilterCountContainer) container).filterItem;
                FilterCount.setAllowList(stack, payload.allowList());
                FilterCount.setCompareNBT(stack, payload.compareNBT());
            }
        });
    }
}
