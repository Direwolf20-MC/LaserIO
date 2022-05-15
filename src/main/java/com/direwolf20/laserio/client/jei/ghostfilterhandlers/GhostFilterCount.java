package com.direwolf20.laserio.client.jei.ghostfilterhandlers;

import com.direwolf20.laserio.client.screens.FilterCountScreen;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GhostFilterCount implements IGhostIngredientHandler<FilterCountScreen> {
    @Override
    public <I> List<Target<I>> getTargets(FilterCountScreen gui, I ingredient, boolean doStart) {
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
                        slot.set((ItemStack) ingredient);
                        gui.getMenu().handler.setStackInSlotSave(slot.index, (ItemStack) ingredient); //We do this for continuity between client/server -- not needed in cardItemScreen
                        PacketHandler.sendToServer(new PacketGhostSlot(slot.index, (ItemStack) ingredient, ((ItemStack) ingredient).getCount()));
                        //RS.NETWORK_HANDLER.sendToServer(new SetFilterSlotMessage(slot.index, (ItemStack) ingredient));
                    }
                });
           /*} else if (ingredient instanceof FluidStack && slot instanceof FluidFilterSlot) {
                targets.add(new Target<I>() {
                    @Override
                    public Rect2i getArea() {
                        return bounds;
                    }

                    @Override
                    public void accept(I ingredient) {
                        RS.NETWORK_HANDLER.sendToServer(new SetFluidFilterSlotMessage(slot.index, StackUtils.copy((FluidStack) ingredient, FluidAttributes.BUCKET_VOLUME)));
                    }
                });*/
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {
        // NO OP
    }
}