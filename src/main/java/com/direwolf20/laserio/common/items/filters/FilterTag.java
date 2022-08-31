package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterTagContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import com.direwolf20.laserio.util.MiscTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

import java.util.ArrayList;
import java.util.List;

public class FilterTag extends BaseFilter {
    public FilterTag() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterBasicHandler handler = getInventory(itemstack);
        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterTagContainer(windowId, playerInventory, player, handler, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
            buf.writeItem(ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        FilterBasicHandler handler = new FilterBasicHandler(FilterTagContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new FilterBasicHandler(FilterTagContainer.SLOTS, stack)) : handler;
    }

    public static FilterBasicHandler setInventory(ItemStack stack, FilterBasicHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static void addTag(ItemStack card, String tag) {
        List<String> tags = getTags(card);
        if (!tags.contains(tag)) {
            tags.add(tag);
            CompoundTag compound = card.getOrCreateTag();
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
    }

    public static void removeTag(ItemStack card, String tag) {
        List<String> tags = getTags(card);
        if (tags.contains(tag)) {
            tags.remove(tag);
            CompoundTag compound = card.getOrCreateTag();
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
    }

    public static void clearTags(ItemStack card) {
        List<String> tags = new ArrayList();
        CompoundTag compound = card.getOrCreateTag();
        compound.put("tags", MiscTools.stringListToNBT(tags));
    }

    public static void setTags(ItemStack card, List<String> tagsIn) {
        CompoundTag compound = card.getOrCreateTag();
        compound.put("tags", MiscTools.stringListToNBT(tagsIn));
    }

    public static void setTags(ItemStack card, CompoundTag tagsTag) {
        CompoundTag compound = card.getOrCreateTag();
        compound.put("tags", tagsTag.getList("tags", Tag.TAG_COMPOUND));
    }

    public static List<String> getTags(ItemStack card) {
        List<String> tags = new ArrayList();
        CompoundTag compound = card.getOrCreateTag();
        if (compound.contains("tags")) {
            ListTag listNBT = compound.getList("tags", Tag.TAG_COMPOUND);
            tags = new ArrayList<>(MiscTools.NBTToStringList(listNBT));
        } else {
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
        return tags;
    }
}
