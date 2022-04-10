package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.BasicFilterContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
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
        //TODO Fix dupe bug with multiple cards in hand when right clicked.
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterBasicHandler handler = getInventory(itemstack);
        NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(windowId, playerInventory, player, handler, itemstack), new TranslatableComponent("")), (buf -> {
            buf.writeItem(itemstack);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        FilterBasicHandler handler = new FilterBasicHandler(BasicFilterContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new FilterBasicHandler(BasicFilterContainer.SLOTS, stack)) : handler;
    }

    public static FilterBasicHandler setInventory(ItemStack stack, FilterBasicHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static boolean getAllowList(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("allowList") ? setAllowList(stack, false) : compound.getBoolean("allowList");
    }

    public static boolean setAllowList(ItemStack stack, boolean allowList) {
        stack.getOrCreateTag().putBoolean("allowList", allowList);
        return allowList;
    }

    public static boolean getCompareNBT(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("compareNBT") ? setCompareNBT(stack, false) : compound.getBoolean("compareNBT");
    }

    public static boolean setCompareNBT(ItemStack stack, boolean compareNBT) {
        stack.getOrCreateTag().putBoolean("compareNBT", compareNBT);
        return compareNBT;
    }
}
