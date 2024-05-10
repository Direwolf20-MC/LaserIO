package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class FilterBasic extends BaseFilter {
    public FilterBasic() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterBasicHandler handler = getInventory(itemstack);
        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterBasicContainer(windowId, playerInventory, player, handler, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        FilterBasicHandler handler = new FilterBasicHandler(FilterBasicContainer.SLOTS, stack);
        return handler;
    }
}
