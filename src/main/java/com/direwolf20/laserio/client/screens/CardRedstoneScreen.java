package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardRedstoneContainer;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import com.direwolf20.laserio.common.network.data.UpdateRedstoneCardPayload;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class CardRedstoneScreen extends AbstractContainerScreen<CardRedstoneContainer> {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/redstonecard.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 256;

    protected final CardRedstoneContainer container;
    protected byte currentMode;
    protected byte currentRedstoneChannel;
    protected boolean currentStrong;
    protected final ItemStack card;
    protected Map<String, Button> buttons = new HashMap<>();

    public CardRedstoneScreen(CardRedstoneContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.card = container.cardItem;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        Button modeButton = buttons.get("mode");
        if (MiscTools.inBounds(modeButton.getX(), modeButton.getY(), modeButton.getWidth(), modeButton.getHeight(), mouseX, mouseY)) {
            MutableComponent translatableComponents[] = new MutableComponent[3];
            translatableComponents[0] = Component.translatable("screen.laserio.input");
            translatableComponents[1] = Component.translatable("screen.laserio.output");
            guiGraphics.setTooltipForNextFrame(font, translatableComponents[currentMode], mouseX, mouseY);
        }
        if (currentMode == 1) {
            Button strongButton = buttons.get("strong");
            if (MiscTools.inBounds(strongButton.getX(), strongButton.getY(), strongButton.getWidth(), strongButton.getHeight(), mouseX, mouseY)) {
                MutableComponent translatableComponents[] = new MutableComponent[2];
                translatableComponents[0] = Component.translatable("screen.laserio.weak");
                translatableComponents[1] = Component.translatable("screen.laserio.strong");
                guiGraphics.setTooltipForNextFrame(font, translatableComponents[currentStrong ? 1 : 0], mouseX, mouseY);
            }
        }
        Button channelButton = buttons.get("channel");
        if (MiscTools.inBounds(channelButton.getX(), channelButton.getY(), channelButton.getWidth(), channelButton.getHeight(), mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(font, Component.translatable("screen.laserio.redstonechannel").append(String.valueOf(currentRedstoneChannel)), mouseX, mouseY);
        }
    }

    public void addModeButton() {
        Identifier[] modeTextures = new Identifier[2];
        modeTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/redstoneinput.png");
        modeTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/redstoneoutput.png");
        buttons.put("mode", new ToggleButton(leftPos + 5, topPos + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = CardRedstone.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            modeChange();
        }));
    }

    public void addStrongButton() {
        Identifier[] strongTextures = new Identifier[2];
        strongTextures[0] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/redstonelow.png");
        strongTextures[1] = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/buttons/redstonehigh.png");
        buttons.put("strong", new ToggleButton(leftPos + 5, topPos + 25, 16, 16, strongTextures, currentStrong ? 1 : 0, (button) -> {
            currentStrong = !currentStrong;
            ((ToggleButton) button).setTexturePosition(currentStrong ? 1 : 0);
        }));
    }

    public void addChannelButton() {
        buttons.put("channel", new ChannelButton(leftPos + 5, topPos + 65, 16, 16, currentRedstoneChannel, (button) -> {
            currentRedstoneChannel = CardRedstone.nextRedstoneChannel(card);
            ((ChannelButton) button).setChannel(currentRedstoneChannel);
        }));
    }

    @Override
    public void init() {
        super.init();
        currentMode = CardRedstone.getTransferMode(card);
        currentRedstoneChannel = CardRedstone.getRedstoneChannel(card);
        currentStrong = CardRedstone.getStrong(card);
        addModeButton();
        addChannelButton();
        addStrongButton();

        if (container.direction != -1) {
            buttons.put("return", new ExtendedButton(leftPos - 25, topPos + 1, 25, 20, Component.literal("<--"), (button) -> {
                openNode();
            }));
        }

        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            addRenderableWidget(button.getValue());
        }

        modeChange();
    }

    public void modeChange() {
        Button strongButton = buttons.get("strong");
        if (currentMode == 0) {
            removeWidget(strongButton);
        } else if (currentMode == 1) { //extract
            if (!renderables.contains(strongButton))
                addRenderableWidget(strongButton);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Intentionally empty — original screen suppressed the default title labels.
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
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
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (event.isEscape() || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        return super.mouseScrolled(mouseX, mouseY, delta, deltaY);
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }

    public void saveSettings() {
        ClientPacketDistributor.sendToServer(new UpdateRedstoneCardPayload(currentMode, currentRedstoneChannel, currentStrong));
    }

    public void openNode() {
        saveSettings();
        ClientPacketDistributor.sendToServer(new OpenNodePayload(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        int btn = event.button();
        ChannelButton channelButton = ((ChannelButton) buttons.get("channel"));
        if (MiscTools.inBounds(channelButton.getX(), channelButton.getY(), channelButton.getWidth(), channelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentRedstoneChannel = CardRedstone.nextRedstoneChannel(card);
            else if (btn == 1)
                currentRedstoneChannel = CardRedstone.previousRedstoneChannel(card);
            channelButton.setChannel(currentRedstoneChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }
}
