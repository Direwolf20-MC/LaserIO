package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

public class CardCloner extends Item {

    public CardCloner(Properties properties) {
        super(properties);
    }

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
            tooltip.accept(toWrite);
            if (cardType.equals(""))
                return;

            int mode = stack.getOrDefault(LaserIODataComponents.CARD_TRANSFER_MODE, 0).intValue();

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
            tooltip.accept(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.channel", ChatFormatting.GRAY.getColor());
            int channel = stack.getOrDefault(LaserIODataComponents.CARD_CHANNEL, 0).intValue();

            toWrite.append(tooltipMaker(String.valueOf(channel), LaserNodeBERender.colors[channel].getRGB()));
            tooltip.accept(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.Filter", ChatFormatting.GRAY.getColor());
            ItemStack filterStack = getFilter(stack);
            if (filterStack.isEmpty())
                toWrite.append(tooltipMaker("laserio.tooltip.item.card.None", ChatFormatting.WHITE.getColor()));
            else
                toWrite.append(tooltipMaker("item.laserio." + filterStack.getItem(), ChatFormatting.DARK_AQUA.getColor()));
            tooltip.accept(toWrite);

            toWrite = tooltipMaker("laserio.tooltip.item.card.Overclockers", ChatFormatting.GRAY.getColor());
            ItemStack overclockStack = getOverclocker(stack);
            if (overclockStack.isEmpty())
                toWrite.append(tooltipMaker(String.valueOf(0), ChatFormatting.WHITE.getColor()));
            else
                toWrite.append(tooltipMaker(String.valueOf(overclockStack.getCount()), ChatFormatting.DARK_AQUA.getColor()));
            tooltip.accept(toWrite);
        }
    }

    public static void setItemType(ItemStack stack, String itemType) {
        stack.set(LaserIODataComponents.CARD_CLONER_ITEM_TYPE, itemType);
    }

    public static String getItemType(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_CLONER_ITEM_TYPE, "");
    }

    public static void saveSettings(ItemStack stack, DataComponentPatch dataComponentPatch) {
        stack.getComponentsPatch().entrySet().forEach(k -> stack.remove(k.getKey()));
        stack.applyComponents(dataComponentPatch);
    }

    public static DataComponentPatch getSettings(ItemStack stack) {
        return stack.getComponentsPatch();
    }

    public static ItemStack getFilter(ItemStack stack) {
        CardItemHandler cardItemHandler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        return cardItemHandler.getResource(0).toStack(cardItemHandler.getAmountAsInt(0));
    }

    public static int getOverclockCount(ItemStack stack) {
        CardItemHandler cardItemHandler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        ItemStack overclockStack = cardItemHandler.getResource(1).toStack(cardItemHandler.getAmountAsInt(1));
        if (overclockStack.isEmpty()) return 0;

        return overclockStack.getCount();
    }

    public static ItemStack getOverclocker(ItemStack stack) {
        String cardType = getItemType(stack);
        CardItemHandler cardItemHandler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        int slot = cardType.equals("card_energy") ? 0 : 1;
        return cardItemHandler.getResource(slot).toStack(cardItemHandler.getAmountAsInt(slot));
    }
}
