package com.direwolf20.laserio.client.screens;

import java.util.HashMap;
import java.util.Map;

import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.NumberButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.common.network.packets.PacketUpdateCard;
import com.direwolf20.laserio.setup.Config;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class CardEnergyScreen extends AbstractContainerScreen<CardEnergyContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/energycard.png");

    protected final CardEnergyContainer container;
    protected byte currentMode;
    protected byte currentChannel;
    protected byte currentRedstoneChannel;
    protected int currentEnergyExtractAmt;
    protected short currentPriority;
    protected byte currentSneaky;
    protected int currentTicks;
    protected boolean currentExact;
    protected int currentRoundRobin;
    protected boolean currentRegulate;
    protected int currentExtractLimitPercent;
    protected int currentInsertLimitPercent;
    protected final ItemStack card;
    protected Map<String, Button> buttons = new HashMap<>();
    protected byte currentRedstoneMode;

    protected final String[] sneakyNames = {
            "screen.laserio.default",
            "screen.laserio.down",
            "screen.laserio.up",
            "screen.laserio.north",
            "screen.laserio.south",
            "screen.laserio.west",
            "screen.laserio.east",
    };

    public CardEnergyScreen(CardEnergyContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.card = container.cardItem;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        Button modeButton = buttons.get("mode");
        if (MiscTools.inBounds(modeButton.getX(), modeButton.getY(), modeButton.getWidth(), modeButton.getHeight(), mouseX, mouseY)) {
            MutableComponent translatableComponents[] = new MutableComponent[4];
            translatableComponents[0] = Component.translatable("screen.laserio.insert");
            translatableComponents[1] = Component.translatable("screen.laserio.extract");
            translatableComponents[2] = Component.translatable("screen.laserio.stock");
            translatableComponents[3] = Component.translatable("screen.laserio.sensor");
            guiGraphics.renderTooltip(font, translatableComponents[currentMode], mouseX, mouseY);
        }
        Button channelButton = buttons.get("channel");
        if (MiscTools.inBounds(channelButton.getX(), channelButton.getY(), channelButton.getWidth(), channelButton.getHeight(), mouseX, mouseY)) {
            if (currentMode != 3)
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.channel").append(String.valueOf(currentChannel)), mouseX, mouseY);
        }
        Button redstoneChannelButton = buttons.get("redstoneChannel");
        if (MiscTools.inBounds(redstoneChannelButton.getX(), redstoneChannelButton.getY(), redstoneChannelButton.getWidth(), redstoneChannelButton.getHeight(), mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.redstonechannel").append(String.valueOf(currentRedstoneChannel)), mouseX, mouseY);
        }
        Button sneakyButton = buttons.get("sneaky");
        if (MiscTools.inBounds(sneakyButton.getX(), sneakyButton.getY(), sneakyButton.getWidth(), sneakyButton.getHeight(), mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable(String.valueOf(sneakyNames[currentSneaky + 1])), mouseX, mouseY);
        }
        Button amountButton = buttons.get("amount");
        if (MiscTools.inBounds(amountButton.getX(), amountButton.getY(), amountButton.getWidth(), amountButton.getHeight(), mouseX, mouseY)) {
            if (showExtractAmt()) {
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.extractamt"), mouseX, mouseY);
            }
            if (showPriority()) {
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.priority"), mouseX, mouseY);
            }
        }
        Button regulate = buttons.get("regulate");
        if (MiscTools.inBounds(regulate.getX(), regulate.getY(), regulate.getWidth(), regulate.getHeight(), mouseX, mouseY)) {
            if (showRegulate()) {
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.regulate"), mouseX, mouseY);
            }
        }
        Button roundrobin = buttons.get("roundrobin");
        if (MiscTools.inBounds(roundrobin.getX(), roundrobin.getY(), roundrobin.getWidth(), roundrobin.getHeight(), mouseX, mouseY)) {
            if (showRoundRobin()) {
                MutableComponent translatableComponents[] = new MutableComponent[3];
                translatableComponents[0] = Component.translatable("screen.laserio.false");
                translatableComponents[1] = Component.translatable("screen.laserio.true");
                translatableComponents[2] = Component.translatable("screen.laserio.enforced");
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.roundrobin").append(translatableComponents[currentRoundRobin]), mouseX, mouseY);
            }
        }
        Button redstoneMode = buttons.get("redstoneMode");
        if (MiscTools.inBounds(redstoneMode.getX(), redstoneMode.getY(), redstoneMode.getWidth(), redstoneMode.getHeight(), mouseX, mouseY)) {
            MutableComponent translatableComponents[] = new MutableComponent[3];
            translatableComponents[0] = Component.translatable("screen.laserio.ignored");
            translatableComponents[1] = Component.translatable("screen.laserio.low");
            translatableComponents[2] = Component.translatable("screen.laserio.high");
            guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.redstoneMode").append(translatableComponents[currentRedstoneMode]), mouseX, mouseY);
        }
        Button exact = buttons.get("exact");
        if (MiscTools.inBounds(exact.getX(), exact.getY(), exact.getWidth(), exact.getHeight(), mouseX, mouseY)) {
            if (showExactAmt()) { //Exact is the same conditions as ExtractAmt
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.exact"), mouseX, mouseY);
            }
        }
        Button speedButton = buttons.get("speed");
        if (MiscTools.inBounds(speedButton.getX(), speedButton.getY(), speedButton.getWidth(), speedButton.getHeight(), mouseX, mouseY)) {
            if (showExactAmt()) {
                guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.tickSpeed"), mouseX, mouseY);
            }
        }
        Button limitButton = buttons.get("limit");
        if (MiscTools.inBounds(limitButton.getX(), limitButton.getY(), limitButton.getWidth(), limitButton.getHeight(), mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.laserio.energylimit"), mouseX, mouseY);
        }
    }

    public void addAmtButton() {
        buttons.put("amount", new NumberButton(getGuiLeft() + 125, getGuiTop() + 25, 46, 12, currentMode == 0 ? currentPriority : currentEnergyExtractAmt, (button) -> {
            changeAmount(-1);
        }));
    }

    public void addLimitButton() {
        buttons.put("limit", new NumberButton(getGuiLeft() + 147, getGuiTop() + 53, 24, 12, showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent, (button) -> {
            changeLimitAmount(-1);
        }));
    }

    public void addModeButton() {
        ResourceLocation[] modeTextures = new ResourceLocation[4];
        modeTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeinserter.png");
        modeTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeextractor.png");
        modeTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modestocker.png");
        modeTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modesensor.png");
        buttons.put("mode", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            ((NumberButton) buttons.get("amount")).setValue(currentMode == 0 ? currentPriority : currentEnergyExtractAmt);
            ((NumberButton) buttons.get("limit")).setValue(showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent);
            modeChange();
        }));
    }

    public void addRedstoneButton() {
        ResourceLocation[] redstoneTextures = new ResourceLocation[3];
        redstoneTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstoneignore.png");
        redstoneTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonelow.png");
        redstoneTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonehigh.png");
        buttons.put("redstoneMode", new ToggleButton(getGuiLeft() + 105, getGuiTop() + 5, 16, 16, redstoneTextures, currentRedstoneMode, (button) -> {
            currentRedstoneMode = (byte) (currentRedstoneMode == 2 ? 0 : currentRedstoneMode + 1);
            ((ToggleButton) button).setTexturePosition(currentRedstoneMode);
        }));
    }

    public void addRedstoneChannelButton() {
        buttons.put("redstoneChannel", new ChannelButton(getGuiLeft() + 125, getGuiTop() + 5, 16, 16, currentRedstoneChannel, (button) -> {
            currentRedstoneChannel = CardRedstone.nextRedstoneChannel(card);
            ((ChannelButton) button).setChannel(currentRedstoneChannel);
        }));
    }

    @Override
    public void init() {
        super.init();
        currentMode = BaseCard.getTransferMode(card);
        currentChannel = BaseCard.getChannel(card);
        currentEnergyExtractAmt = CardEnergy.getEnergyExtractAmt(card);
        currentPriority = BaseCard.getPriority(card);
        currentSneaky = BaseCard.getSneaky(card);
        currentTicks = CardEnergy.getExtractSpeed(card);
        currentExact = BaseCard.getExact(card);
        currentRoundRobin = BaseCard.getRoundRobin(card);
        currentRegulate = BaseCard.getRegulate(card);
        currentExtractLimitPercent = CardEnergy.getExtractLimitPercent(card);
        currentInsertLimitPercent = CardEnergy.getInsertLimitPercent(card);
        currentRedstoneMode = CardEnergy.getRedstoneMode(card);
        currentRedstoneChannel = BaseCard.getRedstoneChannel(card);

        addAmtButton();
        addLimitButton();

        buttons.put("speed", new NumberButton(getGuiLeft() + 147, getGuiTop() + 39, 24, 12, currentTicks, (button) -> {
            changeTick(-1);
        }));

        ResourceLocation[] exactTextures = new ResourceLocation[2];
        exactTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exactfalse.png");
        exactTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exacttrue.png");
        buttons.put("exact", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 25, 16, 16, exactTextures, currentExact ? 1 : 0, (button) -> {
            currentExact = !currentExact;
            ((ToggleButton) button).setTexturePosition(currentExact ? 1 : 0);
        }));

        ResourceLocation[] roundRobinTextures = new ResourceLocation[3];
        roundRobinTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinfalse.png");
        roundRobinTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobintrue.png");
        roundRobinTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinenforced.png");
        buttons.put("roundrobin", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, roundRobinTextures, currentRoundRobin, (button) -> {
            currentRoundRobin = currentRoundRobin == 2 ? 0 : currentRoundRobin + 1;
            ((ToggleButton) button).setTexturePosition(currentRoundRobin);
        }));

        ResourceLocation[] regulateTextures = new ResourceLocation[2];
        regulateTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatefalse.png");
        regulateTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatetrue.png");
        buttons.put("regulate", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, regulateTextures, currentRegulate ? 1 : 0, (button) -> {
            currentRegulate = !currentRegulate;
            ((ToggleButton) button).setTexturePosition(currentRegulate ? 1 : 0);
        }));

        addModeButton();
        addRedstoneButton();
        addRedstoneChannelButton();

        buttons.put("channel", new ChannelButton(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, currentChannel, (button) -> {
            currentChannel = BaseCard.nextChannel(card);
            ((ChannelButton) button).setChannel(currentChannel);
        }));

        ResourceLocation[] sneakyTextures = new ResourceLocation[7];
        sneakyTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky.png");
        sneakyTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-down.png");
        sneakyTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-up.png");
        sneakyTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-north.png");
        sneakyTextures[4] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-south.png");
        sneakyTextures[5] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-west.png");
        sneakyTextures[6] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-east.png");
        buttons.put("sneaky", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 5, 16, 16, sneakyTextures, currentSneaky + 1, (button) -> {
            currentSneaky = BaseCard.nextSneaky(card);
            ((ToggleButton) button).setTexturePosition(currentSneaky + 1);
        }));

        if (container.direction != -1) {
            buttons.put("return", new ExtendedButton(getGuiLeft() - 25, getGuiTop() + 1, 25, 20, Component.literal("<--"), (button) -> {
                openNode();
            }));
        }

        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            addRenderableWidget(button.getValue());
        }

        modeChange();
    }

    public void modeChange() {
        Button speedButton = buttons.get("speed");
        Button exactButton = buttons.get("exact");
        Button rrButton = buttons.get("roundrobin");
        Button regulateButton = buttons.get("regulate");
        Button channelButton = buttons.get("channel");
        Button amountButton = buttons.get("amount");
        if (currentMode == 0) { //insert
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            removeWidget(speedButton);
            removeWidget(exactButton);
            removeWidget(rrButton);
            removeWidget(regulateButton);
        } else if (currentMode == 1) { //extract
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(rrButton))
                addRenderableWidget(rrButton);
            removeWidget(regulateButton);
        } else if (currentMode == 2) { //stock
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(regulateButton))
                addRenderableWidget(regulateButton);
            removeWidget(rrButton);
        } else if (currentMode == 3) { //sensor
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            removeWidget(rrButton);
            removeWidget(regulateButton);
            removeWidget(channelButton);
            removeWidget(amountButton);
        }
    }

    public void changeAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 100;
        int max = Config.MAX_FE_TICK.get();
        if (change < 0) {
            if (currentMode == 0) {
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                currentEnergyExtractAmt = (Math.max(currentEnergyExtractAmt + change, 100));
            }
        } else {
            if (currentMode == 0) {
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                currentEnergyExtractAmt = (Math.min(currentEnergyExtractAmt + change, max));
            }
        }
    }

    public void changeLimitAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 100;
        if (change < 0) {
            if (showExtractLimit()) {
                currentExtractLimitPercent = Math.max(currentExtractLimitPercent + change, 0);
            } else {
                currentInsertLimitPercent = Math.max(currentInsertLimitPercent + change, 0);
            }
        } else {
            if (showExtractLimit()) {
                currentExtractLimitPercent = Math.min(currentExtractLimitPercent + change, 100);
            } else {
                currentInsertLimitPercent = Math.min(currentInsertLimitPercent + change, 100);
            }
        }
    }

    public void changeTick(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 64;
        if (change < 0) {
            currentTicks = (Math.max(currentTicks + change, 1));
        } else {
            currentTicks = (Math.min(currentTicks + change, 1200));
        }
    }

    private boolean showExtractAmt() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT;
    }

    private boolean showExactAmt() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT;
    }

    private boolean showPriority() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.INSERT;
    }

    private boolean showRegulate() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.STOCK;
    }

    private boolean showRoundRobin() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.EXTRACT;
    }

    private boolean showExtractLimit() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.EXTRACT;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveSettings();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_);
        if (p_keyPressed_1_ == 256 || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();

            return true;
        }

        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }


    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void setExtract(NumberButton amountButton, int btn) {
        int change = currentMode == 0 ? 1 : 100;
        if (btn == 0)
            changeAmount(change);
        else if (btn == 1)
            changeAmount(change * -1);
        amountButton.setValue(currentMode == 0 ? currentPriority : currentEnergyExtractAmt);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void setLimitExtract(NumberButton amountButton, int btn) {
        if (btn == 0)
            changeLimitAmount(1);
        else if (btn == 1)
            changeLimitAmount(-1);
        amountButton.setValue(showExtractLimit() ? currentExtractLimitPercent : currentInsertLimitPercent);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void saveSettings() {
        PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel, currentEnergyExtractAmt, currentPriority, currentSneaky, (short) currentTicks, currentExact, currentRegulate, (byte) currentRoundRobin, currentExtractLimitPercent, currentInsertLimitPercent, currentRedstoneMode, currentRedstoneChannel, false));
    }

    public void openNode() {
        saveSettings();
        PacketHandler.sendToServer(new PacketOpenNode(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        ChannelButton channelButton = ((ChannelButton) buttons.get("channel"));
        if ((currentMode != 3) && MiscTools.inBounds(channelButton.getX(), channelButton.getY(), channelButton.getWidth(), channelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentChannel = BaseCard.nextChannel(card);
            else if (btn == 1)
                currentChannel = BaseCard.previousChannel(card);
            channelButton.setChannel(currentChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        ToggleButton sneakyButton = ((ToggleButton) buttons.get("sneaky"));
        if (MiscTools.inBounds(sneakyButton.getX(), sneakyButton.getY(), sneakyButton.getWidth(), sneakyButton.getHeight(), x, y)) {
            if (btn == 0) {
                currentSneaky = BaseCard.nextSneaky(card);
                sneakyButton.setTexturePosition(currentSneaky + 1);
            } else if (btn == 1) {
                currentSneaky = BaseCard.previousSneaky(card);
                sneakyButton.setTexturePosition(currentSneaky + 1);
            }
            sneakyButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        ChannelButton redstoneChannelButton = ((ChannelButton) buttons.get("redstoneChannel"));
        if (MiscTools.inBounds(redstoneChannelButton.getX(), redstoneChannelButton.getY(), redstoneChannelButton.getWidth(), redstoneChannelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentRedstoneChannel = BaseCard.nextRedstoneChannel(card);
            else if (btn == 1)
                currentRedstoneChannel = BaseCard.previousRedstoneChannel(card);
            redstoneChannelButton.setChannel(currentRedstoneChannel);
            redstoneChannelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        NumberButton amountButton = ((NumberButton) buttons.get("amount"));
        if (MiscTools.inBounds(amountButton.getX(), amountButton.getY(), amountButton.getWidth(), amountButton.getHeight(), x, y)) {
            setExtract(amountButton, btn);
            return true;
        }
        NumberButton limitButton = ((NumberButton) buttons.get("limit"));
        if (MiscTools.inBounds(limitButton.getX(), limitButton.getY(), limitButton.getWidth(), limitButton.getHeight(), x, y)) {
            setLimitExtract(limitButton, btn);
            return true;
        }

        NumberButton speedButton = ((NumberButton) buttons.get("speed"));
        if (MiscTools.inBounds(speedButton.getX(), speedButton.getY(), speedButton.getWidth(), speedButton.getHeight(), x, y)) {
            if (btn == 0)
                changeTick(1);
            else if (btn == 1)
                changeTick(-1);
            speedButton.setValue(currentTicks);
            speedButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(x, y, btn);
    }

}