package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.client.events.EventTooltip;
import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

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
            tooltip.add(Component.translatable("laserio.tooltip.item.show_settings")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            MutableComponent toWrite = tooltipMaker("laserio.tooltip.item.filter.type", ChatFormatting.GRAY.getColor());
            boolean allowMode = getAllowList(stack);
            String allowString = allowMode ? "laserio.tooltip.item.filter.type.allow" : "laserio.tooltip.item.filter.type.deny";
            int allowColor = allowMode ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
            toWrite.append(tooltipMaker(allowString, allowColor));
            tooltip.add(toWrite);

            if (!(stack.getItem() instanceof FilterTag)) {
                toWrite = tooltipMaker("laserio.tooltip.item.filter.nbt", ChatFormatting.GRAY.getColor());
                boolean nbtMode = getCompareNBT(stack);
                String nbtString = nbtMode ? "laserio.tooltip.item.filter.nbt.allow" : "laserio.tooltip.item.filter.nbt.deny";
                int nbtColor = nbtMode ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
                toWrite.append(tooltipMaker(nbtString, nbtColor));
                tooltip.add(toWrite);
            }

            if (!(stack.getItem() instanceof FilterTag)) {

            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return Optional.of(new EventTooltip.CopyPasteTooltipComponent.Data(itemStack));
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
