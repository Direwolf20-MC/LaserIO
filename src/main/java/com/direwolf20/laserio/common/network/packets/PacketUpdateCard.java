package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateCard {
    byte mode;
    byte channel;
    int extractAmt;
    short priority;
    byte sneaky;
    short ticks;
    boolean exact;
    boolean regulate;
    byte roundRobin;
    int extractLimit;
    int insertLimit;
    byte redstoneMode;
    byte redstoneChannel;
    boolean andMode;

    public PacketUpdateCard(byte mode, byte channel, int extractAmt, short priority, byte sneaky, short ticks, boolean exact, boolean regulate, byte roundRobin, int extractLimit, int insertLimit, byte redstoneMode, byte redstoneChannel, boolean andMode) {
        this.mode = mode;
        this.channel = channel;
        this.extractAmt = extractAmt;
        this.priority = priority;
        this.sneaky = sneaky;
        this.ticks = ticks;
        this.exact = exact;
        this.regulate = regulate;
        this.roundRobin = roundRobin;
        this.extractLimit = extractLimit;
        this.insertLimit = insertLimit;
        this.redstoneMode = redstoneMode;
        this.redstoneChannel = redstoneChannel;
        this.andMode = andMode;
    }

    public static void encode(PacketUpdateCard msg, FriendlyByteBuf buffer) {
        buffer.writeByte(msg.mode);
        buffer.writeByte(msg.channel);
        buffer.writeInt(msg.extractAmt);
        buffer.writeShort(msg.priority);
        buffer.writeByte(msg.sneaky);
        buffer.writeShort(msg.ticks);
        buffer.writeBoolean(msg.exact);
        buffer.writeBoolean(msg.regulate);
        buffer.writeByte(msg.roundRobin);
        buffer.writeInt(msg.extractLimit);
        buffer.writeInt(msg.insertLimit);
        buffer.writeByte(msg.redstoneMode);
        buffer.writeByte(msg.redstoneChannel);
        buffer.writeBoolean(msg.andMode);
    }

    public static PacketUpdateCard decode(FriendlyByteBuf buffer) {
        return new PacketUpdateCard(buffer.readByte(), buffer.readByte(), buffer.readInt(), buffer.readShort(), buffer.readByte(), buffer.readShort(), buffer.readBoolean(), buffer.readBoolean(), buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readByte(), buffer.readByte(), buffer.readBoolean());
    }

    public static class Handler {
        public static void handle(PacketUpdateCard msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;

                if (container instanceof CardItemContainer || container instanceof CardEnergyContainer) {
                    ItemStack stack;
                    if (container instanceof CardEnergyContainer)
                        stack = ((CardEnergyContainer) container).cardItem;
                    else
                        stack = ((CardItemContainer) container).cardItem;
                    BaseCard.setTransferMode(stack, msg.mode);
                    BaseCard.setChannel(stack, msg.channel);
                    int extractAmt = msg.extractAmt;
                    int overClockerCount = 0;
                    if (stack.getItem() instanceof CardItem) {
                        overClockerCount = container.getSlot(1).getItem().getCount();
                        if (extractAmt > Math.max(overClockerCount * 16, 8)) {
                            extractAmt = (byte) Math.max(overClockerCount * 16, 8);
                        }
                        CardItem.setItemExtractAmt(stack, (byte) extractAmt);
                        short ticks = msg.ticks;
                        if (ticks < Math.max(20 - overClockerCount * 5, 1))
                            ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                        BaseCard.setExtractSpeed(stack, ticks);
                    } else if (stack.getItem() instanceof CardFluid) {
                        overClockerCount = container.getSlot(1).getItem().getCount();
                        if (extractAmt > Math.max(overClockerCount * 2000, 1000)) {
                            extractAmt = Math.max(overClockerCount * 2000, 1000);
                        }
                        CardFluid.setFluidExtractAmt(stack, extractAmt);
                        short ticks = msg.ticks;
                        if (ticks < Math.max(20 - overClockerCount * 5, 1))
                            ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                        BaseCard.setExtractSpeed(stack, ticks);
                    } else if (stack.getItem() instanceof CardEnergy) {
                        int overClockers = container.getSlot(0).getItem().getCount();
                        int max = 1000;
                        switch (overClockers) {
                            case 1:
                                max = 4000;
                                break;
                            case 2:
                                max = 16000;
                                break;
                            case 3:
                                max = 32000;
                                break;
                            case 4:
                                max = 100000;
                                break;
                        }
                        if (extractAmt > max) {
                            extractAmt = max;
                        }
                        CardEnergy.setEnergyExtractAmt(stack, extractAmt);
                        short ticks = msg.ticks;
                        if (ticks < 1)
                            ticks = (short) 1;
                        CardEnergy.setExtractSpeed(stack, ticks);
                        CardEnergy.setExtractLimitPercent(stack, msg.extractLimit);
                        CardEnergy.setInsertLimitPercent(stack, msg.insertLimit);
                    }
                    BaseCard.setPriority(stack, msg.priority);
                    BaseCard.setSneaky(stack, msg.sneaky);
                    BaseCard.setExact(stack, msg.exact);
                    BaseCard.setRoundRobin(stack, msg.roundRobin);
                    BaseCard.setRegulate(stack, msg.regulate);
                    BaseCard.setRedstoneMode(stack, msg.redstoneMode);
                    BaseCard.setRedstoneChannel(stack, msg.redstoneChannel);
                    BaseCard.setAnd(stack, msg.andMode);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
