package com.direwolf20.laserio.common.items.filters;

import com.direwolf20.laserio.client.events.EventTooltip;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.function.Consumer;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

public class BaseFilter extends Item {
    public BaseFilter(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, display, tooltip, flagIn);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = mc.hasShiftDown();

        if (!sneakPressed) {
            tooltip.accept(Component.translatable("laserio.tooltip.item.show_settings")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            MutableComponent toWrite = tooltipMaker("laserio.tooltip.item.filter.type", ChatFormatting.GRAY.getColor());
            boolean allowMode = getAllowList(stack);
            String allowString = allowMode ? "laserio.tooltip.item.filter.type.allow" : "laserio.tooltip.item.filter.type.deny";
            int allowColor = allowMode ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
            toWrite.append(tooltipMaker(allowString, allowColor));
            tooltip.accept(toWrite);

            if (!(stack.getItem() instanceof FilterTag) && !(stack.getItem() instanceof FilterNBT)) {
                toWrite = tooltipMaker("laserio.tooltip.item.filter.nbt", ChatFormatting.GRAY.getColor());
                boolean nbtMode = getCompareNBT(stack);
                String nbtString = nbtMode ? "laserio.tooltip.item.filter.nbt.allow" : "laserio.tooltip.item.filter.nbt.deny";
                int nbtColor = nbtMode ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
                toWrite.append(tooltipMaker(nbtString, nbtColor));
                tooltip.accept(toWrite);
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
        return stack.getOrDefault(LaserIODataComponents.FILTER_ALLOW, true);
    }

    public static boolean setAllowList(ItemStack stack, boolean allowList) {
        if (allowList)
            stack.remove(LaserIODataComponents.FILTER_ALLOW);
        else
            stack.set(LaserIODataComponents.FILTER_ALLOW, allowList);
        return allowList;
    }

    public static boolean getCompareNBT(ItemStack stack) {
        if (stack.getItem() instanceof FilterNBT)
            return stack.getOrDefault(LaserIODataComponents.FILTER_COMPARE, true);
        return stack.getOrDefault(LaserIODataComponents.FILTER_COMPARE, false);
    }

    public static boolean setCompareNBT(ItemStack stack, boolean compareNBT) {
        if (!compareNBT)
            stack.remove(LaserIODataComponents.FILTER_COMPARE);
        else
            stack.set(LaserIODataComponents.FILTER_COMPARE, compareNBT);
        return compareNBT;
    }
}
