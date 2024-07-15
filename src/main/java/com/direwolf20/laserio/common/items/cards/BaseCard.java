package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.containers.customhandler.DireItemContainerContents;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

public class BaseCard extends Item {
    protected BaseCard.CardType CARDTYPE;

    public enum CardType {
        ITEM,
        FLUID,
        ENERGY,
        REDSTONE,
        CHEMICAL,
        MISSING
    }

    public enum TransferMode {
        INSERT,
        EXTRACT,
        STOCK,
        SENSOR
    }

    public BaseCard() {
        super(new Item.Properties()
                .stacksTo(1));

    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Screen.hasShiftDown();

        if (!sneakPressed) {
            tooltip.add(Component.translatable("laserio.tooltip.item.show_settings")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            String currentMode = getNamedTransferMode(stack).toString();
            MutableComponent toWrite = tooltipMaker("laserio.tooltip.item.card.mode", ChatFormatting.GRAY.getColor());
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
            int channel = getChannel(stack);
            toWrite.append(tooltipMaker(String.valueOf(channel), LaserNodeBERender.colors[channel].getRGB()));
            tooltip.add(toWrite);

            int sneakyMode = getSneaky(stack);
            if (sneakyMode != -1) {
                toWrite = tooltipMaker("laserio.tooltip.item.card.sneaky", ChatFormatting.GRAY.getColor());
                toWrite.append(tooltipMaker("laserio.tooltip.item.card.sneaky." + Direction.values()[sneakyMode].toString().toUpperCase(Locale.ROOT), ChatFormatting.DARK_GREEN.getColor()));
                tooltip.add(toWrite);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    /**
     * ItemStack sensitive version of getContainerItem. Returns a full ItemStack
     * instance of the result.
     * Custom Implementation by Dire: get multiples
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    public NonNullList<ItemStack> getContainerItems(ItemStack itemStack) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(2, ItemStack.EMPTY);
        nonnulllist.set(0, getInventory(itemStack).getStackInSlot(0));
        nonnulllist.set(1, getInventory(itemStack).getStackInSlot(1));

        return nonnulllist;
    }


    /**
     * If you override hasCraftingRemainingItem you MUST override this as well
     * Note: The real logic happens above in getContainerItems(Itemstack)
     * This is only here to deal with autocrafters who might crash
     */
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    /**
     * ItemStack sensitive version of hasContainerItem
     *
     * @param stack The current item stack
     * @return True if this item has a 'container'
     */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return !(getInventory(stack).getStackInSlot(0).isEmpty() && getInventory(stack).getStackInSlot(1).isEmpty());
    }

    public static CardItemHandler getInventory(ItemStack stack) {
        CardItemHandler cardItemHandler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        return cardItemHandler;
    }

    public static CardItemHandler setInventory(ItemStack stack, CardItemHandler handler) {
        List<ItemStack> stacklist = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            stacklist.add(handler.getStackInSlot(i));
        }
        stack.set(LaserIODataComponents.ITEMSTACK_HANDLER, DireItemContainerContents.fromItems(stacklist));
        return handler;
    }

    public static byte setTransferMode(ItemStack card, byte mode) {
        if (mode == 0)
            card.remove(LaserIODataComponents.CARD_TRANSFER_MODE);
        else
            card.set(LaserIODataComponents.CARD_TRANSFER_MODE, mode);
        return mode;
    }

    public static byte getTransferMode(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_TRANSFER_MODE, 0).byteValue();
    }

    public static byte nextTransferMode(ItemStack card) {
        byte mode = getTransferMode(card);
        return setTransferMode(card, (byte) (mode == 3 ? 0 : mode + 1));
    }

    public static TransferMode getNamedTransferMode(ItemStack card) {
        return TransferMode.values()[getTransferMode(card)];
    }

    public static byte setChannel(ItemStack card, byte channel) {
        if (channel == 0)
            card.remove(LaserIODataComponents.CARD_CHANNEL);
        else
            card.set(LaserIODataComponents.CARD_CHANNEL, channel);
        return channel;
    }

    public static byte getChannel(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_CHANNEL, 0).byteValue();
    }

    public static byte nextChannel(ItemStack card) {
        byte k = getChannel(card);
        return setChannel(card, (byte) (k == 15 ? 0 : k + 1));
    }

    public static byte previousChannel(ItemStack card) {
        byte k = getChannel(card);
        return setChannel(card, (byte) (k == 0 ? 15 : k - 1));
    }

    public static int setExtractSpeed(ItemStack card, int itemextractspeed) {
        if (itemextractspeed == 20)
            card.remove(LaserIODataComponents.CARD_EXTRACT_SPEED);
        else
            card.set(LaserIODataComponents.CARD_EXTRACT_SPEED, itemextractspeed);
        return itemextractspeed;
    }

    public static int getExtractSpeed(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_EXTRACT_SPEED, 20);
    }

    public static int setMaxBackoff(ItemStack card, byte itemextractspeed) {
        if (itemextractspeed == 0)
            card.remove(LaserIODataComponents.CARD_MAX_BACKOFF);
        else
            card.set(LaserIODataComponents.CARD_MAX_BACKOFF, itemextractspeed);
        return itemextractspeed;
    }

    public static byte getMaxBackoff(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_MAX_BACKOFF, (byte) 0);
    }

    public static short setPriority(ItemStack card, short priority) {
        if (priority == 0)
            card.remove(LaserIODataComponents.CARD_PRIORITY);
        else
            card.set(LaserIODataComponents.CARD_PRIORITY, priority);
        return priority;
    }

    public static short getPriority(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_PRIORITY, 0).shortValue();
    }

    public static ItemStack getFilter(ItemStack card) {
        CardItemHandler cardItemHandler = getInventory(card);
        return cardItemHandler.getStackInSlot(0);
    }

    public static byte setSneaky(ItemStack card, byte sneaky) {
        if (sneaky == -1)
            card.remove(LaserIODataComponents.CARD_SNEAKY);
        else
            card.set(LaserIODataComponents.CARD_SNEAKY, sneaky);
        return sneaky;
    }

    public static byte getSneaky(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_SNEAKY, -1).byteValue();
    }

    public static byte nextSneaky(ItemStack card) {
        byte k = getSneaky(card);
        return setSneaky(card, (byte) (k == 5 ? -1 : k + 1));
    }

    public static byte previousSneaky(ItemStack card) {
        byte k = getSneaky(card);
        return setSneaky(card, (byte) (k == -1 ? 5 : k - 1));
    }

    public static boolean getRegulate(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_REGULATE, false);
    }

    public static boolean setRegulate(ItemStack stack, boolean regulate) {
        if (!regulate)
            stack.remove(LaserIODataComponents.CARD_REGULATE);
        else
            stack.set(LaserIODataComponents.CARD_REGULATE, regulate);
        return regulate;
    }

    public static int getRoundRobin(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_ROUND_ROBIN, 0);
    }

    public static int setRoundRobin(ItemStack stack, int roundRobin) {
        if (roundRobin == 0)
            stack.remove(LaserIODataComponents.CARD_ROUND_ROBIN);
        else
            stack.set(LaserIODataComponents.CARD_ROUND_ROBIN, roundRobin);
        return roundRobin;
    }

    public static byte getRedstoneMode(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_REDSTONE_MODE, 0).byteValue();
    }

    public static byte setRedstoneMode(ItemStack stack, byte redstoneMode) {
        if (redstoneMode == 0)
            stack.remove(LaserIODataComponents.CARD_REDSTONE_MODE);
        else
            stack.set(LaserIODataComponents.CARD_REDSTONE_MODE, redstoneMode);
        return redstoneMode;
    }

    public static byte nextRedstoneMode(ItemStack card) {
        byte mode = getRedstoneMode(card);
        return setRedstoneMode(card, (byte) (mode == 2 ? 0 : mode + 1));
    }

    public static boolean getExact(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_EXACT, false);
    }

    public static boolean setExact(ItemStack stack, boolean exact) {
        if (!exact)
            stack.remove(LaserIODataComponents.CARD_EXACT);
        else
            stack.set(LaserIODataComponents.CARD_EXACT, exact);
        return exact;
    }

    public static byte setRedstoneChannel(ItemStack card, byte redstonechannel) {
        if (redstonechannel == 0)
            card.remove(LaserIODataComponents.CARD_REDSTONE_CHANNEL);
        else
            card.set(LaserIODataComponents.CARD_REDSTONE_CHANNEL, redstonechannel);
        return redstonechannel;
    }

    public static byte getRedstoneChannel(ItemStack card) {
        return card.getOrDefault(LaserIODataComponents.CARD_REDSTONE_CHANNEL, 0).byteValue();
    }

    public static byte nextRedstoneChannel(ItemStack card) {
        byte k = getRedstoneChannel(card);
        return setRedstoneChannel(card, (byte) (k == 15 ? 0 : k + 1));
    }

    public static byte previousRedstoneChannel(ItemStack card) {
        byte k = getRedstoneChannel(card);
        return setRedstoneChannel(card, (byte) (k == 0 ? 15 : k - 1));
    }

    public static boolean getAnd(ItemStack stack) {
        return stack.getOrDefault(LaserIODataComponents.CARD_AND_MODE, false);
    }

    public static boolean setAnd(ItemStack stack, boolean and) {
        if (!and)
            stack.remove(LaserIODataComponents.CARD_AND_MODE);
        else
            stack.set(LaserIODataComponents.CARD_AND_MODE, and);
        return and;
    }
}
