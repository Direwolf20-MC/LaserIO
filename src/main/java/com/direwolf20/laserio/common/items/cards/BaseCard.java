package com.direwolf20.laserio.common.items.cards;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.setup.ModSetup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
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
        MISSING
    }

    public enum TransferMode {
        INSERT,
        EXTRACT,
        STOCK,
        SENSOR
    }

    public BaseCard() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP)
                .stacksTo(1));

    }

    public CardType getCardType() {
        return CARDTYPE;
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
     * ItemStack sensitive version of hasContainerItem
     *
     * @param stack The current item stack
     * @return True if this item has a 'container'
     */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return !(getInventory(stack).getStackInSlot(0).equals(ItemStack.EMPTY) && getInventory(stack).getStackInSlot(1).equals(ItemStack.EMPTY));
    }

    public static CardItemHandler getInventory(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("inv")) return new CardItemHandler(CardItemContainer.SLOTS, stack);
        CardItemHandler handler = new CardItemHandler(CardItemContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        if (handler.getSlots() < CardItemContainer.SLOTS)
            handler.reSize(CardItemContainer.SLOTS);
        return handler;
    }

    public static CardItemHandler setInventory(ItemStack stack, CardItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                stack.getOrCreateTag().put("inv", handler.serializeNBT());
                return handler;
            }
        }
        stack.removeTagKey("inv");
        return handler;
    }

    public static byte setTransferMode(ItemStack card, byte mode) {
        if (mode == 0)
            card.removeTagKey("mode");
        else
            card.getOrCreateTag().putByte("mode", mode);
        return mode;
    }

    public static byte getTransferMode(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("mode")) return (byte) 0;
        return compound.getByte("mode");
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
            card.removeTagKey("channel");
        else
            card.getOrCreateTag().putByte("channel", channel);
        return channel;
    }

    public static byte getChannel(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("channel")) return (byte) 0;
        return compound.getByte("channel");
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
            card.removeTagKey("itemextractspeed");
        else
            card.getOrCreateTag().putInt("itemextractspeed", itemextractspeed);
        return itemextractspeed;
    }

    public static int getExtractSpeed(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("itemextractspeed")) return 20;
        return compound.getInt("itemextractspeed");
    }

    public static short setPriority(ItemStack card, short priority) {
        if (priority == 0)
            card.removeTagKey("priority");
        else
            card.getOrCreateTag().putShort("priority", priority);
        return priority;
    }

    public static short getPriority(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("priority")) return (short) 0;
        return compound.getShort("priority");
    }

    public static ItemStack getFilter(ItemStack card) {
        CardItemHandler cardItemHandler = getInventory(card);
        return cardItemHandler.getStackInSlot(0);
    }

    public static byte setSneaky(ItemStack card, byte sneaky) {
        if (sneaky == -1)
            card.removeTagKey("sneaky");
        else
            card.getOrCreateTag().putByte("sneaky", sneaky);
        return sneaky;
    }

    public static byte getSneaky(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("sneaky")) return (byte) -1;
        return compound.getByte("sneaky");
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
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("regulate")) return false;
        return compound.getBoolean("regulate");
    }

    public static boolean setRegulate(ItemStack stack, boolean regulate) {
        if (!regulate)
            stack.removeTagKey("regulate");
        else
            stack.getOrCreateTag().putBoolean("regulate", regulate);
        return regulate;
    }

    public static int getRoundRobin(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("roundRobin")) return 0;
        return compound.getInt("roundRobin");
    }

    public static int setRoundRobin(ItemStack stack, int roundRobin) {
        if (roundRobin == 0)
            stack.removeTagKey("roundRobin");
        else
            stack.getOrCreateTag().putInt("roundRobin", roundRobin);
        return roundRobin;
    }

    public static byte getRedstoneMode(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("redstoneMode")) return 0;
        return compound.getByte("redstoneMode");
    }

    public static byte setRedstoneMode(ItemStack stack, byte redstoneMode) {
        if (redstoneMode == 0)
            stack.removeTagKey("redstoneMode");
        else
            stack.getOrCreateTag().putByte("redstoneMode", redstoneMode);
        return redstoneMode;
    }

    public static byte nextRedstoneMode(ItemStack card) {
        byte mode = getRedstoneMode(card);
        return setRedstoneMode(card, (byte) (mode == 2 ? 0 : mode + 1));
    }

    public static boolean getExact(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("exact")) return false;
        return compound.getBoolean("exact");
    }

    public static boolean setExact(ItemStack stack, boolean exact) {
        if (!exact)
            stack.removeTagKey("exact");
        else
            stack.getOrCreateTag().putBoolean("exact", exact);
        return exact;
    }

    public static byte setRedstoneChannel(ItemStack card, byte redstonechannel) {
        if (redstonechannel == 0)
            card.removeTagKey("redstonechannel");
        else
            card.getOrCreateTag().putByte("redstonechannel", redstonechannel);
        return redstonechannel;
    }

    public static byte getRedstoneChannel(ItemStack card) {
        CompoundTag compound = card.getTag();
        if (compound == null || !compound.contains("redstonechannel")) return (byte) 0;
        return compound.getByte("redstonechannel");
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
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("and")) return false;
        return compound.getBoolean("and");
    }

    public static boolean setAnd(ItemStack stack, boolean and) {
        if (!and)
            stack.removeTagKey("and");
        else
            stack.getOrCreateTag().putBoolean("and", and);
        return and;
    }
}
