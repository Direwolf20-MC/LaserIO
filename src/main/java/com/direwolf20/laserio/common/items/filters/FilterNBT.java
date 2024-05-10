package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterNBTContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterBasicHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FilterNBT extends BaseFilter {
    public FilterNBT() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        FilterBasicHandler handler = getInventory(itemstack);
        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new FilterNBTContainer(windowId, playerInventory, player, handler, itemstack), Component.translatable("")), (buf -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemstack);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, ItemStack.EMPTY);
        }));

        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }

    public static FilterBasicHandler getInventory(ItemStack stack) {
        FilterBasicHandler handler = new FilterBasicHandler(FilterBasicContainer.SLOTS, stack);
        return handler;
    }

    //TODO Re-Implement
    /*public static FilterBasicHandler setInventory(ItemStack stack, FilterBasicHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }*/

    /*public static void addTag(ItemStack card, String tag) {
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
    }*/
}
