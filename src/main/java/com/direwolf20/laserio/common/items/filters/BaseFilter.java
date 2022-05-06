package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BaseFilter extends Item {
    public BaseFilter() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Minecraft mc = Minecraft.getInstance();

        if (world == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Screen.hasShiftDown();

        if (!sneakPressed) {
            //tooltip.add(new TranslatableComponent("laserio.tooltip.item.show_settings")
            //        .withStyle(ChatFormatting.GRAY));
        } else {
            //tooltip.add(new TranslatableComponent("laserio.tooltip.item.filter.type"));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    public static boolean getAllowList(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        return !compound.contains("allowList") ? setAllowList(stack, true) : compound.getBoolean("allowList");
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
