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
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketUpdateFilter {
    public static final PacketUpdateFilter INSTANCE = new PacketUpdateFilter();

    public static PacketUpdateFilter get() {
        return INSTANCE;
    }

    public void handle(final UpdateFilterPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            if (container instanceof CardItemContainer cardItemContainer) {
                ItemStack stack = cardItemContainer.handler.getStackInSlot(0);
                if (stack.isEmpty()) return;
                FilterBasic.setAllowList(stack, payload.allowList());
                FilterBasic.setCompareNBT(stack, payload.compareNBT());
                cardItemContainer.handler.setStackInSlot(0, stack);
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
