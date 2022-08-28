package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class FilterBasic extends BaseFilter {
    public FilterBasic() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterBasicHandler handler = getInventory(itemstack);
        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterBasicContainer(windowId, playerInventory, player, handler, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeItem(ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        FilterBasicHandler handler = new FilterBasicHandler(FilterBasicContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new FilterBasicHandler(FilterBasicContainer.SLOTS, stack)) : handler;
    }

    public static FilterBasicHandler setInventory(ItemStack stack, FilterBasicHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }
}
