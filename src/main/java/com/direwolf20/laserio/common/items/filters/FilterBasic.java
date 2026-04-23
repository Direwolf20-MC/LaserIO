package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class FilterBasic extends BaseFilter {
    public FilterBasic(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.PASS;

        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterBasicContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, ItemStack.EMPTY);
            buf.writeByte(-1);
            buf.writeVarInt(-1);
        }));

        return InteractionResult.PASS;
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        FilterBasicHandler handler = new FilterBasicHandler(FilterBasicContainer.SLOTS, stack);
        return handler;
    }
}
