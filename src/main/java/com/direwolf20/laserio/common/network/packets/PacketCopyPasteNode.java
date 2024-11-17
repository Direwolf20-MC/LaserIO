package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.items.CardCloner;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;


public class PacketCopyPasteNode {
    public enum NodeCloneModes {
        CLEAR,
        COPY,
        PASTE
    }

    private final BlockPos pos;
    private final NodeCloneModes action;

    public PacketCopyPasteNode(BlockPos pos,NodeCloneModes action) {
        this.pos = pos;
        this.action = action;
    }

    public static void encode(PacketCopyPasteNode msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeEnum(msg.action);
    }

    public static PacketCopyPasteNode decode(FriendlyByteBuf buf) {
        return new PacketCopyPasteNode(buf.readBlockPos(), buf.readEnum(NodeCloneModes.class));
    }

    public static Map<Item, Integer> getCardsInNode(ItemStackHandler[] faceInventories){
        Map<Item, Integer> existingCards = new HashMap<>();
        for (ItemStackHandler face : faceInventories) {
            for (int slot = 0; slot < LaserNodeContainer.CARDSLOTS; slot++) {
                ItemStack card = face.getStackInSlot(slot);
                if (card.isEmpty()) continue;
                int currentCardCount = existingCards.getOrDefault(card.getItem(), 0);
                existingCards.put(card.getItem(), currentCardCount + card.getCount());
                BaseCard.getCardContents(card).forEach((k, v) -> existingCards.merge(k, v, Integer::sum));
            }
            // Node Overclock Card Slot
            ItemStack card = face.getStackInSlot(LaserNodeContainer.CARDSLOTS);
            if (card.isEmpty()) continue;
            int currentCardCount = existingCards.getOrDefault(card.getItem(), 0);
            existingCards.put(card.getItem(), currentCardCount + card.getCount());
        }
        return existingCards;
    }

    public static class Handler {
        public static void handle(PacketCopyPasteNode msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;
                if (!(player.getMainHandItem().getItem() instanceof CardCloner))
                    return;


                ItemStack clonerStack = player.getMainHandItem();
                CompoundTag newTag = clonerStack.getOrCreateTag();
                if(msg.action == NodeCloneModes.CLEAR){
                    if (newTag.contains("nodeData")) {
                        clonerStack.removeTagKey("nodeData");
                    }
                } else{
                    BlockEntity be = player.level().getBlockEntity(msg.pos);
                    if (!(be instanceof LaserNodeBE laserNode))
                        return;
                    if(msg.action.equals(NodeCloneModes.COPY)){
                        CompoundTag nodeTag = new CompoundTag();
                        laserNode.saveAdditional(nodeTag);
                        nodeTag.putInt("cloned", nodeTag.hashCode());
                        CardCloner.saveNodeData(clonerStack, nodeTag);
                        PacketCopyPasteCard.playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.NOTE_BLOCK_BIT.get().getLocation().toString()))));
                    } else if(msg.action.equals(NodeCloneModes.PASTE)){
                        if (newTag.contains("nodeData")) {
                            ItemStack cardHolder = LaserNode.findCardHolders(player);
                            ItemStackHandler[] currentNodeFaces = Arrays.stream(laserNode.nodeSideCaches).map(cache -> cache.itemHandler).toArray(ItemStackHandler[]::new);
                            Map<Item, Integer> existingCards = getCardsInNode(currentNodeFaces);
                            Map<Item, Integer> neededCards = getCardsInNode(CardCloner.getNodeData(clonerStack));
                            Map<Item, Integer> holderCards = CardHolder.getHolderCardCounts(cardHolder);

                            Map<Item, Integer> cardHolderDelta = new HashMap<>();
                            existingCards.forEach((k, v) -> cardHolderDelta.merge(k, v, Integer::sum));
                            neededCards.forEach((k, v) -> cardHolderDelta.merge(k, -v, Integer::sum));
                            Map<Item, Integer> netCards = new HashMap<>();
                            holderCards.forEach((k, v) -> netCards.merge(k, v, Integer::sum));
                            cardHolderDelta.forEach((k, v) -> netCards.merge(k, v, Integer::sum));

                            // Make sure there are enough of all items to paste successfully.
                            if (netCards.entrySet().stream().allMatch(e -> e.getValue() >= 0)) {

                                for (var entry : cardHolderDelta.entrySet()){
                                    Item cardType = entry.getKey();
                                    int remainingQuantity = entry.getValue();
                                    ItemStack excessItems = ItemStack.EMPTY;
                                    while (remainingQuantity != 0 && excessItems.isEmpty()) {
                                        if (remainingQuantity > 0){
                                            System.out.println("adding");
                                            int maxStackSize = 64;
                                            int stack_size = Math.min(remainingQuantity, maxStackSize);
                                            ItemStack cardDeposit = new ItemStack(cardType, stack_size);
                                            remainingQuantity -= stack_size;
                                            excessItems = CardHolder.addCardToInventory(cardHolder, cardDeposit);
                                        } else if (remainingQuantity < 0) {
                                            System.out.println("removing");
                                            ItemStack retrievedCards = CardHolder.requestCardFromInventory(cardHolder, new ItemStack(cardType, Math.abs(remainingQuantity)));
                                            remainingQuantity += retrievedCards.getCount();
                                            if (retrievedCards.getCount() != remainingQuantity){
                                                // retrieved amount should never be too small since we check the total card counts,
                                                // but just in case, break out of the loop.
                                                remainingQuantity = 0;
                                            }
                                        }
                                    }
                                    if (!excessItems.isEmpty()) {
                                        // Drop any cards that could not fit into the cardHolder
                                        for(int i = 0; i < excessItems.getCount(); i++){
                                            ItemStack individualCard = new ItemStack(excessItems.getItem(), 1);
                                            ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), individualCard);
                                            player.level().addFreshEntity(itemEntity);
                                        }
                                    }
                                }

                                laserNode.load(newTag.getCompound("nodeData"));
                                laserNode.updateThisNode();
                                PacketCopyPasteCard.playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.EXPERIENCE_ORB_PICKUP.getLocation().toString()))));
                            } else {
                                player.displayClientMessage(Component.translatable("message.laserio.cloner.cardcount"), true);
                                PacketCopyPasteCard.playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                            }
                        } else {
                            player.displayClientMessage(Component.translatable("message.laserio.cloner.nodata"), true);
                            PacketCopyPasteCard.playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}

