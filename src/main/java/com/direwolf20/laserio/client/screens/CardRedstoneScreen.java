package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardRedstoneContainer;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.common.network.packets.PacketUpdateRedstoneCard;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;

public class CardRedstoneScreen extends AbstractCardScreen<CardRedstoneContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/redstonecard.png");

    protected final CardRedstoneContainer container;
    protected byte currentMode;
    protected byte currentRedstoneChannel;
    protected boolean currentStrong;

    public CardRedstoneScreen(CardRedstoneContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        Button modeButton = buttons.get("mode");
        if (MiscTools.inBounds(modeButton.x, modeButton.y, modeButton.getWidth(), modeButton.getHeight(), mouseX, mouseY)) {
            TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
            translatableComponents[0] = new TranslatableComponent("screen.laserio.input");
            translatableComponents[1] = new TranslatableComponent("screen.laserio.output");
            this.renderTooltip(matrixStack, translatableComponents[currentMode], mouseX, mouseY);
        }
        if (currentMode == 1) {
            Button strongButton = buttons.get("strong");
            if (MiscTools.inBounds(strongButton.x, strongButton.y, strongButton.getWidth(), strongButton.getHeight(), mouseX, mouseY)) {
                TranslatableComponent translatableComponents[] = new TranslatableComponent[2];
                translatableComponents[0] = new TranslatableComponent("screen.laserio.weak");
                translatableComponents[1] = new TranslatableComponent("screen.laserio.strong");
                this.renderTooltip(matrixStack, translatableComponents[currentStrong ? 1 : 0], mouseX, mouseY);
            }
        }
        Button channelButton = buttons.get("channel");
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.redstonechannel").append(String.valueOf(currentRedstoneChannel)), mouseX, mouseY);
        }
    }

    public void addModeButton() {
        ResourceLocation[] modeTextures = new ResourceLocation[2];
        modeTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstoneinput.png");
        modeTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstoneoutput.png");
        buttons.put("mode", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = CardRedstone.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            modeChange();
        }));
    }

    public void addStrongButton() {
        ResourceLocation[] strongTextures = new ResourceLocation[2];
        strongTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonelow.png");
        strongTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonehigh.png");
        buttons.put("strong", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, strongTextures, currentStrong ? 1 : 0, (button) -> {
            currentStrong = !currentStrong;
            ((ToggleButton) button).setTexturePosition(currentStrong ? 1 : 0);
        }));
    }

    public void addChannelButton() {
        buttons.put("channel", new ChannelButton(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, currentRedstoneChannel, (button) -> {
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
        addBackButton();

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
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        /*stack.pushPose();
        stack.scale(0.5f, 0.5f, 0.5f);
        if (showExtractAmt()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.extractamt").getString() + ":", 5*2, 45*2, Color.DARK_GRAY.getRGB());
        }
        if (showPriority()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.priority").getString() + ":", 5*2, 50*2, Color.DARK_GRAY.getRGB());
        }
        stack.popPose();*/
        //super.renderLabels(matrixStack, x, y);
    }

    @Override
    public Component cardTypeName(){
        return new TranslatableComponent("item.laserio.card_redstone");
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
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

    private static TranslatableComponent getTrans(String key, Object... args) {
        return new TranslatableComponent(LaserIO.MODID + "." + key, args);
    }

    public void saveSettings() {
        PacketHandler.sendToServer(new PacketUpdateRedstoneCard(currentMode, currentRedstoneChannel, currentStrong));
    }

    @Override
    public void openNode() {
        saveSettings();
        PacketHandler.sendToServer(new PacketOpenNode(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        ChannelButton channelButton = ((ChannelButton) buttons.get("channel"));
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentRedstoneChannel = CardRedstone.nextRedstoneChannel(card);
            else if (btn == 1)
                currentRedstoneChannel = CardRedstone.previousRedstoneChannel(card);
            channelButton.setChannel(currentRedstoneChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(x, y, btn);
    }
}
