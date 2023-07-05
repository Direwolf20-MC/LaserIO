package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.CardCloner;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
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

    public static boolean returnItemToholder(LaserNodeContainer container, ItemStack itemStack, boolean simulate) {
        if (itemStack.isEmpty()) return true;
        int neededReturn = itemStack.getCount();
        Map<Integer, Integer> returnStackMap = new HashMap<>();
        for (int returnSlot = LaserNodeContainer.CARDSLOTS + 1; returnSlot < LaserNodeContainer.SLOTS; returnSlot++) {
            ItemStack possibleReturnStack = container.getSlot(returnSlot).getItem();
            if (possibleReturnStack.isEmpty() || (possibleReturnStack.is(itemStack.getItem()) && possibleReturnStack.getCount() < possibleReturnStack.getMaxStackSize())) {
                int roomAvailable = possibleReturnStack.getMaxStackSize() - possibleReturnStack.getCount();
                int amtFit = (neededReturn - roomAvailable < 0) ? neededReturn : neededReturn - roomAvailable;
                returnStackMap.put(returnSlot, amtFit);
                neededReturn = neededReturn - amtFit;
                if (neededReturn == 0) {
                    if (simulate) {
                        return true;
                    }
                    break;
                }
            }
        }
        if (neededReturn > 0 || returnStackMap.isEmpty()) //If we didn't return everything we needed to, return false
            return false;
        for (Map.Entry<Integer, Integer> entry : returnStackMap.entrySet()) {
            ItemStack possibleReturnStack = container.getSlot(entry.getKey()).getItem();
            if (possibleReturnStack.isEmpty()) {
                container.getSlot(entry.getKey()).set(itemStack);
                //In *THEORY* this should never be needed but who knows!
                possibleReturnStack = container.getSlot(entry.getKey()).getItem();
                possibleReturnStack.setCount(entry.getValue());
            } else {
                possibleReturnStack.grow(entry.getValue());
            }
        }
        return true; //Since we got here we can assume we updated everything
    }

    public static boolean getItemFromHolder(LaserNodeContainer container, ItemStack itemStack, boolean simulate) {
        if (itemStack.isEmpty()) return true;
        int neededCount = itemStack.getCount();
        Map<Integer, Integer> findStackMap = new HashMap<>();
        for (int getSlot = LaserNodeContainer.CARDSLOTS + 1; getSlot < LaserNodeContainer.SLOTS; getSlot++) {
            ItemStack possibleStack = container.getSlot(getSlot).getItem();
            if (possibleStack.is(itemStack.getItem())) {
                int stackAvailable = possibleStack.getCount();
                int amtFound = (neededCount - stackAvailable < 0) ? neededCount : stackAvailable;
                findStackMap.put(getSlot, amtFound);
                neededCount = neededCount - amtFound;
                if (neededCount == 0) {
                    if (simulate) {
                        return true;
                    }
                    break;
                }
            }
        }
        if (neededCount > 0 || findStackMap.isEmpty()) //If we didn't find everything we needed to, return false
            return false;
        for (Map.Entry<Integer, Integer> entry : findStackMap.entrySet()) {
            ItemStack possibleStack = container.getSlot(entry.getKey()).getItem();
            possibleStack.shrink(entry.getValue());
        }
        return true; //Since we got here we can assume we updated everything
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

                LaserNodeContainer laserNodeContainer = (LaserNodeContainer) container;

                if (player.containerMenu.getCarried().isEmpty())
                    return;

                ItemStack slotStack = container.getSlot(msg.slot).getItem();
                ItemStack clonerStack = container.getCarried();
                if (msg.copy) { //copy mode
                    CardCloner.setItemType(clonerStack, slotStack.getItem().toString());
                    CompoundTag compoundTag = slotStack.getTag() == null ? new CompoundTag() : slotStack.getTag();
                    CardCloner.saveSettings(clonerStack, compoundTag);
                    playSound(player, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT.getLocation().toString()))));
                } else {
                    if (slotStack.getItem().toString().equals(CardCloner.getItemType(clonerStack))) {
                        CardItemHandler cardItemHandler = BaseCard.getInventory(slotStack);
                        ItemStack filterNeeded = CardCloner.getFilter(clonerStack);
                        ItemStack existingFilter = cardItemHandler.getStackInSlot(0);
                        ItemStack overclockersNeeded = CardCloner.getOverclocker(clonerStack);
                        ItemStack existingOverclockers = cardItemHandler.getStackInSlot(1);
                        boolean filterSatisfied = false;
                        boolean filterNeedsReturn = false;
                        boolean overclockSatisfied = false;
                        boolean overclockNeedsReturn = false;
                        if (existingFilter.is(filterNeeded.getItem())) { //If the right filters there, do nothing
                            filterSatisfied = true;
                        } else {
                            if (!existingFilter.isEmpty()) { //If we have the wrong filter, and theres an existing one, remove it first
                                filterNeedsReturn = !returnItemToholder(laserNodeContainer, existingFilter, true);
                            }
                            if (!filterNeedsReturn) { //If the filter can be returned or doesn't need to be
                                filterSatisfied = getItemFromHolder(laserNodeContainer, filterNeeded, true);
                            }
                        }
                        if (existingOverclockers.getCount() == overclockersNeeded.getCount()) { //If we have the right number of overclockers
                            overclockSatisfied = true;
                        } else {
                            if (existingOverclockers.getCount() > overclockersNeeded.getCount()) { //If we have too many overclockers
                                int amtReturn = existingOverclockers.getCount() - overclockersNeeded.getCount();
                                ItemStack returnStack = new ItemStack(existingOverclockers.getItem(), amtReturn);
                                overclockNeedsReturn = !returnItemToholder(laserNodeContainer, returnStack, true);
                                overclockSatisfied = true;
                            } else { //If we don't have enough
                                int amtNeeded = overclockersNeeded.getCount() - existingOverclockers.getCount();
                                ItemStack findStack = new ItemStack(overclockersNeeded.getItem(), amtNeeded);
                                overclockSatisfied = getItemFromHolder(laserNodeContainer, findStack, true);
                            }
                        }
                        if (filterSatisfied && !filterNeedsReturn && overclockSatisfied && !overclockNeedsReturn) {
                            if (!existingFilter.is(filterNeeded.getItem())) { //Now that we're doing it for real, check to make sure the filter needs switching
                                boolean success = returnItemToholder(laserNodeContainer, existingFilter, false);
                                if (!success) {
                                    //Drop item in world
                                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), existingFilter);
                                    player.level().addFreshEntity(itemEntity);
                                }
                                getItemFromHolder(laserNodeContainer, filterNeeded, false);
                            }
                            if (existingOverclockers.getCount() != overclockersNeeded.getCount()) { //If we need to work with Overclockers
                                if (existingOverclockers.getCount() > overclockersNeeded.getCount()) { //If we have too many overclockers
                                    int amtReturn = existingOverclockers.getCount() - overclockersNeeded.getCount();
                                    ItemStack returnStack = new ItemStack(existingOverclockers.getItem(), amtReturn);
                                    boolean success = returnItemToholder(laserNodeContainer, returnStack, false);
                                    if (!success) {
                                        //Drop item in world
                                        ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), returnStack);
                                        player.level().addFreshEntity(itemEntity);
                                    }
                                } else { //If we don't have enough
                                    int amtNeeded = overclockersNeeded.getCount() - existingOverclockers.getCount();
                                    ItemStack findStack = new ItemStack(overclockersNeeded.getItem(), amtNeeded);
                                    getItemFromHolder(laserNodeContainer, findStack, false);
                                }
                            }
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
