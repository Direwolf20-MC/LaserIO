package com.direwolf20.laserio.client.jei.ghostfilterhandlers;

import com.direwolf20.laserio.client.screens.CardItemScreen;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GhostFilterCard implements IGhostIngredientHandler<CardItemScreen> {
    @Override
    public <I> List<Target<I>> getTargets(CardItemScreen gui, I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        for (Slot slot : gui.getMenu().slots) {
            if (!slot.isActive()) {
                continue;
            }

            Rect2i bounds = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16); //RS Had this as 17 17

            if (ingredient instanceof ItemStack && (slot instanceof FilterBasicSlot)) {
                targets.add(new Target<I>() {
                    @Override
                    public Rect2i getArea() {
                        return bounds;
                    }

                    @Override
                    public void accept(I ingredient) {
                        ItemStack itemStack = (ItemStack) ingredient;
                        slot.set(itemStack.copy());
                        PacketHandler.sendToServer(new PacketGhostSlot(slot.index, itemStack, itemStack.getCount()));
                        //RS.NETWORK_HANDLER.sendToServer(new SetFilterSlotMessage(slot.index, (ItemStack) ingredient));
                    }
                });
            } else if (ingredient instanceof FluidStack && (slot instanceof FilterBasicSlot)) {
                targets.add(new Target<I>() {
                    @Override
                    public Rect2i getArea() {
                        return bounds;
                    }

                    @Override
                    public void accept(I ingredient) {
                        ItemStack itemStack = new ItemStack(((FluidStack) ingredient).getFluid().getBucket(), 1);
                        slot.set(itemStack.copy());
                        PacketHandler.sendToServer(new PacketGhostSlot(slot.index, itemStack, itemStack.getCount()));
                        //RS.NETWORK_HANDLER.sendToServer(new SetFluidFilterSlotMessage(slot.index, StackUtils.copy((FluidStack) ingredient, FluidAttributes.BUCKET_VOLUME)));
                    }
                });
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {
        // NO OP
    }
}