package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.NumberButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.data.GhostSlotPayload;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import com.direwolf20.laserio.common.network.data.UpdateCardPayload;
import com.direwolf20.laserio.common.network.data.UpdateFilterPayload;
import com.direwolf20.laserio.integration.mekanism.CardChemical;
import com.direwolf20.laserio.integration.mekanism.MekanismStatics;
import com.direwolf20.laserio.setup.Config;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class CardChemicalScreen extends CardItemScreen {

    public int currentChemicalExtractAmt;
    public final int filterStartX;
    public final int filterStartY;
    public final int filterEndX;
    public final int filterEndY;

    public CardChemicalScreen(CardItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        filterStartX = 35;
        filterStartY = 16;
        filterEndX = 125;
        filterEndY = 70;
    }

    @Override
    public void init() {
        this.currentChemicalExtractAmt = CardChemical.getChemicalExtractAmt(card);
        super.init();
        this.renderChemicals = true;
    }

    @Override
    public void addAmtButton() {
        buttons.put("amount", new NumberButton(getGuiLeft() + 139, getGuiTop() + 25, 32, 12, currentMode == 0 ? currentPriority : currentChemicalExtractAmt, (button) -> {
            changeAmount(-1);
        }));
    }

    @Override
    public void addModeButton() {
        ResourceLocation[] modeTextures = {
                ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modeinserter.png"),
                ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modeextractor.png"),
                ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modestocker.png"),
                ResourceLocation.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/modesensor.png")
        };
        buttons.put("mode", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            ((NumberButton) buttons.get("amount")).setValue(currentMode == 0 ? currentPriority : currentChemicalExtractAmt);
            modeChange();
        }));
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            if (hoveredSlot instanceof FilterBasicSlot) {
                ChemicalStack<?> chemicalStack = MekanismStatics.getFirstChemicalOnItemStack(itemstack);
                if (chemicalStack.isEmpty())
                    pGuiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, pX, pY);
                else
                    pGuiGraphics.renderTooltip(this.font, chemicalStack.getTextComponent(), pX, pY);
                return;
            }
            pGuiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, pX, pY);
        }
    }

    @Override
    public void changeAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 100;
        int overClockerCount = container.getSlot(1).getItem().getCount();
        if (change < 0) {
            if (currentMode == 0) {
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                currentChemicalExtractAmt = (Math.max(currentChemicalExtractAmt + change, 1));
            }
        } else {
            if (currentMode == 0) {
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                currentChemicalExtractAmt = (Math.min(currentChemicalExtractAmt + change, Math.max(overClockerCount * Config.MULTIPLIER_MILLI_BUCKETS_CHEMICAL.get(), Config.BASE_MILLI_BUCKETS_CHEMICAL.get())));
            }
        }
    }

    @Override
    public boolean filterSlot(int btn) {
        ItemStack slotStack = hoveredSlot.getItem();
        if (!MekanismStatics.doesItemStackHoldChemicals(slotStack))
            return super.filterSlot(btn);
        if (slotStack.isEmpty()) return true;
        if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            slotStack.setCount(0);
            PacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, slotStack.getCount(), 0));
            return true;
        }
        int amt = (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) ? 1 : -1;
        int filterSlot = hoveredSlot.index - CardItemContainer.SLOTS;
        int currentMBAmt = FilterCount.getSlotAmount(filter, filterSlot) + (FilterCount.getSlotCount(filter, filterSlot) * 1000);
        if (Screen.hasShiftDown()) amt *= 10;
        if (Screen.hasControlDown()) amt *= 100;
        int newMBAmt = currentMBAmt + amt;
        if (newMBAmt < 0) newMBAmt = 0;
        if (newMBAmt > 4096000) newMBAmt = 4096000;
        FilterCount.setSlotAmount(slotStack, filterSlot, newMBAmt);
        PacketDistributor.sendToServer(new GhostSlotPayload(hoveredSlot.index, slotStack, 1, newMBAmt));
        return true;
    }

    @Override
    public void setExtract(NumberButton amountButton, int btn) {
        if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            changeAmount(1);
        else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            changeAmount(-1);
        amountButton.setValue(currentMode == 0 ? currentPriority : currentChemicalExtractAmt);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void openNode() {
        saveSettings();
        PacketDistributor.sendToServer(new OpenNodePayload(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        return super.mouseScrolled(mouseX, mouseY, delta, deltaY);
    }

    @Override
    public void saveSettings() {
        if (showFilter)
            PacketDistributor.sendToServer(new UpdateFilterPayload(isAllowList == 1, isCompareNBT == 1));
        PacketDistributor.sendToServer(new UpdateCardPayload(currentMode, currentChannel, currentChemicalExtractAmt, currentPriority, currentSneaky, (short) currentTicks, currentExact, currentRegulate, (byte) currentRoundRobin, 0, 0, currentRedstoneMode, currentRedstoneChannel, currentAndMode, currentMaxBackoff));
    }
}
