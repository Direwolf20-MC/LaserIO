package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.NumberButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.data.GhostSlotPayload;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import com.direwolf20.laserio.common.network.data.UpdateCardPayload;
import com.direwolf20.laserio.common.network.data.UpdateFilterPayload;
import com.direwolf20.laserio.setup.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

public class CardFluidScreen extends CardItemScreen {

    public int currentFluidExtractAmt;
    public final int filterStartX;
    public final int filterStartY;
    public final int filterEndX;
    public final int filterEndY;

    public CardFluidScreen(CardItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        filterStartX = 35;
        filterStartY = 16;
        filterEndX = 125;
        filterEndY = 70;
    }

    @Override
    public void init() {
        this.currentFluidExtractAmt = CardFluid.getFluidExtractAmt(card);
        super.init();
        this.renderFluids = true;
    }

    @Override
    public void addAmtButton() {
        buttons.put("amount", new NumberButton(leftPos + 141, topPos + 25, 30, 12, currentMode == 0 ? currentPriority : currentFluidExtractAmt, (button) -> {
            changeAmount(-1);
        }));
    }

    @Override
    public void addModeButton() {
        Identifier[] modeTextures = new Identifier[4];
        modeTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modeinserter.png");
        modeTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modeextractor.png");
        modeTextures[2] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modestocker.png");
        modeTextures[3] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modesensor.png");
        buttons.put("mode", new ToggleButton(leftPos + 5, topPos + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            ((NumberButton) buttons.get("amount")).setValue(currentMode == 0 ? currentPriority : currentFluidExtractAmt);
            modeChange();
        }));
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack itemStack, Slot slot, @Nullable String itemCount) {
        if (slot instanceof FilterBasicSlot && !itemStack.isEmpty()) {
            FluidStack fluidStack = LaserGuiGraphicsFluid.getFluidForStack(itemStack);
            if (!fluidStack.isEmpty()) {
                LaserGuiGraphicsFluid.renderFluidSprite(graphics, fluidStack, slot.x, slot.y, 16);
                int filterSlotIndex = slot.index - CardItemContainer.SLOTS;
                LaserGuiGraphicsFluid.renderFilterCountOverlay(graphics, filter, filterSlotIndex, slot.x, slot.y);
                return;
            }
        }
        super.renderSlotContents(graphics, itemStack, slot, itemCount);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            if (hoveredSlot instanceof FilterBasicSlot) {
                FluidStack fluidStack = FluidUtil.getFirstStackContained(itemstack);
                if (!fluidStack.isEmpty()) {
                    guiGraphics.setTooltipForNextFrame(this.font, fluidStack.getHoverName(), mouseX, mouseY);
                    return;
                }
            }
            guiGraphics.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, mouseX, mouseY);
        }
    }

    @Override
    public void changeAmount(int change) {
        if (Minecraft.getInstance().hasShiftDown()) change *= 10;
        if (Minecraft.getInstance().hasControlDown()) change *= 100;
        int overClockerCount = container.getSlot(1).getItem().getCount();
        if (change < 0) {
            if (currentMode == 0) {
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                currentFluidExtractAmt = (Math.max(currentFluidExtractAmt + change, 1));
            }
        } else {
            if (currentMode == 0) {
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                currentFluidExtractAmt = (Math.min(currentFluidExtractAmt + change, Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_FLUID.get(), Config.BASE_MILLI_BUCKETS_FLUID.get())));
            }
        }
    }

    @Override
    public boolean filterSlot(int btn) {
        ItemStack slotStack = hoveredSlot.getItem();
        if (!FilterCount.doesItemStackHoldFluids(slotStack))
            return super.filterSlot(btn);
        if (slotStack.isEmpty()) return true;
        if (btn == 2) {
            slotStack.setCount(0);
            ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, slotStack.getCount(), 0));
            return true;
        }
        int amt = (btn == 0) ? 1 : -1;
        int filterSlot = hoveredSlot.index - CardItemContainer.SLOTS;
        int currentMBAmt = FilterCount.getSlotAmount(filter, filterSlot) + (FilterCount.getSlotCount(filter, filterSlot) * 1000);
        if (Minecraft.getInstance().hasShiftDown()) amt *= 10;
        if (Minecraft.getInstance().hasControlDown()) amt *= 100;
        int newMBAmt = currentMBAmt + amt;
        if (newMBAmt < 0) newMBAmt = 0;
        if (newMBAmt > 4096000) newMBAmt = 4096000;
        FilterCount.setSlotAmount(slotStack, filterSlot, newMBAmt);
        ClientPacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, 1, newMBAmt));
        return true;
    }

    @Override
    public void setExtract(NumberButton amountButton, int btn) {
        if (btn == 0)
            changeAmount(1);
        else if (btn == 1)
            changeAmount(-1);
        amountButton.setValue(currentMode == 0 ? currentPriority : currentFluidExtractAmt);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void openNode() {
        saveSettings();
        ClientPacketDistributor.sendToServer(new OpenNodePayload(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        return super.mouseScrolled(mouseX, mouseY, delta, deltaY);
    }

    @Override
    public void saveSettings() {
        if (showFilter)
            ClientPacketDistributor.sendToServer(new UpdateFilterPayload(isAllowList == 1, isCompareNBT == 1));
        ClientPacketDistributor.sendToServer(new UpdateCardPayload(currentMode, currentChannel, currentFluidExtractAmt, currentPriority, currentSneaky, (short) currentTicks, currentExact, currentRegulate, (byte) currentRoundRobin, 0, 0, currentRedstoneMode, currentRedstoneChannel, currentAndMode));
    }
}
