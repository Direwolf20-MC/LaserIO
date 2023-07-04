package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.CardCloner;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PacketCopyPasteCard {
    int slot;
    boolean copy;

    public PacketCopyPasteCard(int slot, boolean copy) {
        this.slot = slot;
        this.copy = copy;
    }

    public static void encode(PacketCopyPasteCard msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slot);
        buffer.writeBoolean(msg.copy);
    }

    public static PacketCopyPasteCard decode(FriendlyByteBuf buffer) {
        return new PacketCopyPasteCard(buffer.readInt(), buffer.readBoolean());
    }

    public static void playSound(ServerPlayer player, Holder<SoundEvent> soundEventHolder) {
        // Get player's position
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // Create the packet
        ClientboundSoundPacket packet = new ClientboundSoundPacket(
                soundEventHolder, // The sound event
                SoundSource.MASTER, // The sound category
                x, y, z, // The sound location
                1, // The volume, 1 is normal, higher is louder
                1, // The pitch, 1 is normal, higher is higher pitch
                player.level().getRandom().nextLong() // A random for some reason?
        );

        // Send the packet to the player
        player.connection.send(packet);
    }

    public static class Handler {
        public static void handle(PacketCopyPasteCard msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                AbstractContainerMenu container = player.containerMenu;
                if (container == null)
                    return;

                if (!(container instanceof LaserNodeContainer))
                    return;

                if (player.containerMenu.getCarried().isEmpty())
                    return;

                ItemStack slotStack = container.getSlot(msg.slot).getItem();
                ItemStack clonerStack = container.getCarried();
                if (msg.copy) { //copy mode
                    CardCloner.setItemType(clonerStack, slotStack.getItem().toString());
                    CardCloner.saveSettings(clonerStack, slotStack.getOrCreateTag());
                    playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT.getLocation().toString()))));
                } else {
                    if (slotStack.getItem().toString().equals(CardCloner.getItemType(clonerStack))) {
                        String filterType = CardCloner.getFilterType(clonerStack);
                        boolean hasFilter = false;
                        boolean needsReturn = false;
                        if (filterType.equals("")) { //If theres no filter
                            hasFilter = true;
                        } else {
                            Item filterItemNeeded = ForgeRegistries.ITEMS.getValue(new ResourceLocation("laserio", filterType));
                            CardItemHandler cardItemHandler = BaseCard.getInventory(slotStack);
                            ItemStack existingFilter = cardItemHandler.getStackInSlot(0);
                            if (existingFilter.is(filterItemNeeded)) {
                                hasFilter = true;
                            } else {
                                if (!((LaserNodeContainer) container).cardHolder.isEmpty()) {
                                    for (int slot = LaserNodeContainer.CARDSLOTS + 1; slot < LaserNodeContainer.SLOTS; slot++) {
                                        ItemStack stackInHolder = container.getSlot(slot).getItem();
                                        if (stackInHolder.getItem().equals(filterItemNeeded)) {
                                            if (!existingFilter.isEmpty()) {
                                                needsReturn = true;
                                                for (int returnSlot = LaserNodeContainer.CARDSLOTS + 1; returnSlot < LaserNodeContainer.SLOTS; returnSlot++) {
                                                    ItemStack possibleReturnStack = container.getSlot(returnSlot).getItem();
                                                    if (possibleReturnStack.is(existingFilter.getItem()) && possibleReturnStack.getCount() < possibleReturnStack.getMaxStackSize()) {
                                                        possibleReturnStack.grow(1);
                                                        needsReturn = false;
                                                        break;
                                                    }
                                                }
                                            }
                                            stackInHolder.shrink(1);
                                            hasFilter = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (hasFilter && !needsReturn) {
                            ItemStack tempStack = slotStack.copy();
                            tempStack.setTag(CardCloner.getSettings(clonerStack));
                            container.getSlot(msg.slot).set(tempStack);
                            playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.UI_BUTTON_CLICK.get().getLocation().toString()))));
                            ((LaserNodeContainer)container).tile.updateThisNode();
                        } else {
                            playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
                        }
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
