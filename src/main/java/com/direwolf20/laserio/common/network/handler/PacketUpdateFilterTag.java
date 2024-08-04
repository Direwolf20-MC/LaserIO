package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.FilterNBTContainer;
import com.direwolf20.laserio.common.containers.FilterTagContainer;
import com.direwolf20.laserio.common.items.filters.FilterNBT;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.direwolf20.laserio.common.network.data.UpdateFilterTagPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketUpdateFilterTag {
    public static final PacketUpdateFilterTag INSTANCE = new PacketUpdateFilterTag();

    public static PacketUpdateFilterTag get() {
        return INSTANCE;
    }

    public void handle(final UpdateFilterTagPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            if (container instanceof FilterTagContainer) {
                ItemStack stack = ((FilterTagContainer) container).filterItem;
                FilterTag.setAllowList(stack, payload.allowList());
                FilterTag.setTags(stack, payload.tags());
            }
            if (container instanceof FilterNBTContainer) {
                ItemStack stack = ((FilterNBTContainer) container).filterItem;
                FilterNBT.setAllowList(stack, payload.allowList());
                FilterNBT.setTags(stack, payload.tags());
            }
        });
    }
}
