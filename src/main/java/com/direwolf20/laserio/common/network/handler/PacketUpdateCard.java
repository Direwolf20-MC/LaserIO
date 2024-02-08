package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.network.data.UpdateCardPayload;
import com.direwolf20.laserio.integration.mekanism.CardChemical;
import com.direwolf20.laserio.setup.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

public class PacketUpdateCard {
    public static final PacketUpdateCard INSTANCE = new PacketUpdateCard();

    public static PacketUpdateCard get() {
        return INSTANCE;
    }

    public void handle(final UpdateCardPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> senderOptional = context.player();
            if (senderOptional.isEmpty())
                return;
            Player sender = senderOptional.get();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            if (container instanceof CardItemContainer || container instanceof CardEnergyContainer) {
                ItemStack stack;
                if (container instanceof CardEnergyContainer)
                    stack = ((CardEnergyContainer) container).cardItem;
                else
                    stack = ((CardItemContainer) container).cardItem;
                BaseCard.setTransferMode(stack, payload.mode());
                BaseCard.setChannel(stack, payload.channel());
                int extractAmt = payload.extractAmt();
                int overClockerCount = 0;
                if (stack.getItem() instanceof CardItem) {
                    overClockerCount = container.getSlot(1).getItem().getCount();
                    if (extractAmt > Math.max(overClockerCount * 16, 8)) {
                        extractAmt = (byte) Math.max(overClockerCount * 16, 8);
                    }
                    CardItem.setItemExtractAmt(stack, (byte) extractAmt);
                    short ticks = payload.ticks();
                    if (ticks < Math.max(20 - overClockerCount * 5, 1))
                        ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                    BaseCard.setExtractSpeed(stack, ticks);
                } else if (stack.getItem() instanceof CardFluid) {
                    overClockerCount = container.getSlot(1).getItem().getCount();
                    if (extractAmt > Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_FLUID.get(), Config.BASE_MILLI_BUCKETS_FLUID.get())) {
                        extractAmt = Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_FLUID.get(), Config.BASE_MILLI_BUCKETS_FLUID.get());
                    }
                    CardFluid.setFluidExtractAmt(stack, extractAmt);
                    short ticks = payload.ticks();
                    if (ticks < Math.max(20 - overClockerCount * 5, 1))
                        ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                    BaseCard.setExtractSpeed(stack, ticks);
                } else if (stack.getItem() instanceof CardEnergy) {
                    int max = Config.MAX_FE_TICK.get();
                    if (extractAmt > max) {
                        extractAmt = max;
                    }
                    CardEnergy.setEnergyExtractAmt(stack, extractAmt);
                    short ticks = payload.ticks();
                    if (ticks < 1)
                        ticks = (short) 1;
                    CardEnergy.setExtractSpeed(stack, ticks);
                    CardEnergy.setExtractLimitPercent(stack, payload.extractLimit());
                    CardEnergy.setInsertLimitPercent(stack, payload.insertLimit());
                } else if (stack.getItem() instanceof CardChemical) {
                    overClockerCount = container.getSlot(1).getItem().getCount();
                    if (extractAmt > Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_CHEMICAL.get(), Config.BASE_MILLI_BUCKETS_CHEMICAL.get())) {
                        extractAmt = Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_CHEMICAL.get(), Config.BASE_MILLI_BUCKETS_CHEMICAL.get());
                    }
                    CardChemical.setChemicalExtractAmt(stack, extractAmt);
                    short ticks = payload.ticks();
                    if (ticks < Math.max(20 - overClockerCount * 5, 1))
                        ticks = (short) Math.max(20 - overClockerCount * 5, 1);
                    BaseCard.setExtractSpeed(stack, ticks);
                }
                BaseCard.setPriority(stack, payload.priority());
                BaseCard.setSneaky(stack, payload.sneaky());
                BaseCard.setExact(stack, payload.exact());
                BaseCard.setRoundRobin(stack, payload.roundRobin());
                BaseCard.setRegulate(stack, payload.regulate());
                BaseCard.setRedstoneMode(stack, payload.redstoneMode());
                BaseCard.setRedstoneChannel(stack, payload.redstoneChannel());
                BaseCard.setAnd(stack, payload.andMode());
            }
        });
    }
}
