package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

import java.util.List;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

public class CardCloner extends Item {
    public CardCloner() {
        super(new Properties()
                .stacksTo(1));
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
            String cardType = getItemType(stack);
            MutableComponent toWrite = tooltipMaker("laserio.tooltip.item.filter.type", ChatFormatting.GRAY.getColor());
            int cardColor = ChatFormatting.WHITE.getColor();
            if (cardType.equals("card_item"))
                cardColor = ChatFormatting.GREEN.getColor();
            else if (cardType.equals("card_fluid"))
                cardColor = ChatFormatting.BLUE.getColor();
            else if (cardType.equals("card_energy"))
                cardColor = ChatFormatting.YELLOW.getColor();
            else if (cardType.equals("card_redstone"))
                cardColor = ChatFormatting.RED.getColor();
            if (cardType.equals(""))
                toWrite.append(tooltipMaker("laserio.tooltip.item.card.None", cardColor));
            else
                toWrite.append(tooltipMaker("item.laserio." + cardType, cardColor));
            tooltip.add(toWrite);
            if (cardType.equals(""))
                return;

            CompoundTag compoundTag = stack.getOrCreateTag().getCompound("settings");
            int mode = !compoundTag.contains("mode") ? 0 : compoundTag.getByte("mode");;
            String currentMode = BaseCard.TransferMode.values()[mode].toString();
            toWrite = tooltipMaker("laserio.tooltip.item.card.mode", ChatFormatting.GRAY.getColor());
            int modeColor = ChatFormatting.GRAY.getColor();
            if (currentMode.equals("EXTRACT"))
                modeColor = ChatFormatting.RED.getColor();
            else if (currentMode.equals("INSERT"))
                modeColor = ChatFormatting.GREEN.getColor();
            else if (currentMode.equals("STOCK"))
                modeColor = ChatFormatting.BLUE.getColor();
            else if (currentMode.equals("SENSOR"))
                modeColor = ChatFormatting.YELLOW.getColor();
            toWrite.append(tooltipMaker("laserio.tooltip.item.card.mode." + currentMode, modeColor));
            tooltip.add(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.channel", ChatFormatting.GRAY.getColor());
            int channel = !compoundTag.contains("channel") ? 0 : compoundTag.getByte("channel");;
            toWrite.append(tooltipMaker(String.valueOf(channel), LaserNodeBERender.colors[channel].getRGB()));
            tooltip.add(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.Filter", ChatFormatting.GRAY.getColor());
            ItemStack filterStack = getFilter(stack);
            if (filterStack.isEmpty())
                toWrite.append(tooltipMaker("laserio.tooltip.item.card.None", ChatFormatting.WHITE.getColor()));
            else
                toWrite.append(tooltipMaker("item.laserio." + filterStack.getItem(), ChatFormatting.DARK_AQUA.getColor()));
            tooltip.add(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.Overclockers", ChatFormatting.GRAY.getColor());
            ItemStack overclockStack = getOverclocker(stack);
            if (overclockStack.isEmpty())
                toWrite.append(tooltipMaker(String.valueOf(0), ChatFormatting.WHITE.getColor()));
            else
                toWrite.append(tooltipMaker(String.valueOf(overclockStack.getCount()), ChatFormatting.DARK_AQUA.getColor()));
            tooltip.add(toWrite);
        }
    }

    public static void setItemType(ItemStack stack, String itemType) {
        stack.getOrCreateTag().putString("itemType", itemType);
    }

    public static String getItemType(ItemStack stack) {
        return stack.getOrCreateTag().getString("itemType");
    }

    public static void saveSettings(ItemStack stack, CompoundTag tag) {
        stack.getOrCreateTag().put("settings", tag);
    }

    public static CompoundTag getSettings(ItemStack stack) {
        return stack.getOrCreateTag().getCompound("settings");
    }

    public static String getFilterType(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack filterStack = itemStackHandler.getStackInSlot(0);
        if (filterStack.isEmpty()) return "";

        return filterStack.getItem().toString();
    }

    public static ItemStack getFilter(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack filterStack = itemStackHandler.getStackInSlot(0);
        return filterStack;
    }

    public static int getOverclockCount(ItemStack stack) {
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack overclockStack = itemStackHandler.getStackInSlot(1);
        if (overclockStack.isEmpty()) return 0;

        return overclockStack.getCount();
    }

    public static ItemStack getOverclocker(ItemStack stack) {
        String cardType = getItemType(stack);
        CompoundTag compoundTag = getSettings(stack);
        ItemStackHandler itemStackHandler = new ItemStackHandler(CardItemContainer.SLOTS);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inv"));
        ItemStack overclockStack;
        if (cardType.equals("card_energy"))
            overclockStack = itemStackHandler.getStackInSlot(0);
        else
            overclockStack = itemStackHandler.getStackInSlot(1);
        return overclockStack;
    }
}