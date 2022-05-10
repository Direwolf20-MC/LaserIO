package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.DireButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customslot.CardItemSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketOpenFilter;
import com.direwolf20.laserio.common.network.packets.PacketUpdateCard;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CardItemScreen extends AbstractContainerScreen<CardItemContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/itemcard.png");

    protected final CardItemContainer container;
    private byte currentMode;
    private byte currentChannel;
    private byte currentItemExtractAmt;
    private short currentPriority;
    private byte currentSneaky;
    private ItemStack card;
    private ChannelButton channelButton;

    private final String[] sneakyNames = {
            "screen.laserio.default",
            "screen.laserio.down",
            "screen.laserio.up",
            "screen.laserio.north",
            "screen.laserio.south",
            "screen.laserio.west",
            "screen.laserio.east",
    };

    public CardItemScreen(CardItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.card = container.cardItem;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
        translatableComponents[0] = new TranslatableComponent("screen.laserio.insert");
        translatableComponents[1] = new TranslatableComponent("screen.laserio.extract");
        translatableComponents[2] = new TranslatableComponent("screen.laserio.stock");
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, translatableComponents[currentMode], mouseX, mouseY);
        }
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 35, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TextComponent(String.valueOf(currentChannel)), mouseX, mouseY);
        }
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent(String.valueOf(sneakyNames[currentSneaky + 1])), mouseX, mouseY);
        }
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        currentMode = BaseCard.getTransferMode(card);
        currentChannel = BaseCard.getChannel(card);
        currentItemExtractAmt = BaseCard.getItemExtractAmt(card);
        currentPriority = BaseCard.getPriority(card);
        currentSneaky = BaseCard.getSneaky(card);

        DireButton plusButton = new DireButton(getGuiLeft() + 140, getGuiTop() + 14, 10, 10, new TranslatableComponent("-"), (button) -> {
            if (currentMode == 0) {
                int change = -1;
                if (Screen.hasShiftDown()) change *= 10;
                if (Screen.hasControlDown()) change *= 64;
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                int change = -1;
                if (Screen.hasShiftDown()) change *= 10;
                if (Screen.hasControlDown()) change *= 64;
                currentItemExtractAmt = (byte) (Math.max(currentItemExtractAmt + change, 1));
            }
        });
        DireButton minusButton = new DireButton(getGuiLeft() + 154, getGuiTop() + 14, 10, 10, new TranslatableComponent("+"), (button) -> {
            if (currentMode == 0) {
                int change = 1;
                if (Screen.hasShiftDown()) change *= 10;
                if (Screen.hasControlDown()) change *= 64;
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                int change = 1;
                if (Screen.hasShiftDown()) change *= 10;
                if (Screen.hasControlDown()) change *= 64;
                currentItemExtractAmt = (byte) (Math.min(currentItemExtractAmt + change, Math.max(container.getSlot(1).getItem().getCount() * 16, 1)));
            }
        });

        ResourceLocation[] allowListTextures = new ResourceLocation[3];
        allowListTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeinserter.png");
        allowListTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeextractor.png");
        allowListTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeprovider.png");

        leftWidgets.add(new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, allowListTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
        }));

        this.channelButton = new ChannelButton(getGuiLeft() + 5, getGuiTop() + 35, 16, 16, currentChannel, (button) -> {
            currentChannel = BaseCard.nextChannel(card);
            ((ChannelButton) button).setChannel(currentChannel);
        });
        leftWidgets.add(channelButton);

        leftWidgets.add(plusButton);
        leftWidgets.add(minusButton);

        ResourceLocation[] sneakyTextures = new ResourceLocation[7];
        sneakyTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky.png");
        sneakyTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-down.png");
        sneakyTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-up.png");
        sneakyTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-north.png");
        sneakyTextures[4] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-south.png");
        sneakyTextures[5] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-west.png");
        sneakyTextures[6] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-east.png");
        Button sneakyButton = new ToggleButton(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, sneakyTextures, currentSneaky + 1, (button) -> {
            currentSneaky = BaseCard.nextSneaky(card);
            ((ToggleButton) button).setTexturePosition(currentSneaky + 1);
        });
        leftWidgets.add(sneakyButton);

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }
    }

    private boolean showExtractAmt() {
        return card.getItem() instanceof CardItem && BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT;
    }

    private boolean showPriority() {
        return card.getItem() instanceof CardItem && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.INSERT;
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        if (showExtractAmt()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.extractamt").getString() + ":", 57, 5, Color.DARK_GRAY.getRGB());
            String extractAmt = Integer.toString(currentItemExtractAmt);
            font.draw(stack, new TextComponent(extractAmt).getString(), 150 - font.width(extractAmt) / 2, 5, Color.DARK_GRAY.getRGB());
        }
        if (showPriority()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.priority").getString() + ":", 97, 5, Color.DARK_GRAY.getRGB());
            String priority = Integer.toString(currentPriority);
            //drawCenteredString(stack, font, priority, 110 + font.width("-4096"), 5, Color.DARK_GRAY.getRGB());
            font.draw(stack, new TextComponent(priority).getString(), 153 - font.width(priority) / 2, 5, Color.DARK_GRAY.getRGB());
        }
        //super.renderLabels(matrixStack, x, y);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
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
        PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel, currentItemExtractAmt, currentPriority, currentSneaky));
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

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 35, 16, 16, x, y)) {
            if (btn == 0)
                currentChannel = BaseCard.nextChannel(card);
            else if (btn == 1)
                currentChannel = BaseCard.previousChannel(card);
            channelButton.setChannel(currentChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        if (hoveredSlot == null || hoveredSlot.getItem().isEmpty() || !(hoveredSlot.getItem().getItem() instanceof BaseFilter))
            return super.mouseClicked(x, y, btn);

        if (btn == 1 && hoveredSlot instanceof CardItemSlot) { //Right click
            int slot = hoveredSlot.getSlotIndex();
            PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel, currentItemExtractAmt, currentPriority, currentSneaky));
            PacketHandler.sendToServer(new PacketOpenFilter(slot));
            return true;
        }
        return super.mouseClicked(x, y, btn);
    }
}
