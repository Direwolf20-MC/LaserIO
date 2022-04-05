package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.ItemCardContainer;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketUpdateCard;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemCardScreen extends AbstractContainerScreen<ItemCardContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/itemcard.png");

    protected final ItemCardContainer container;
    private byte currentMode;
    private byte currentChannel;
    private ItemStack card;
    //private boolean isWhitelist;
    //private boolean isNBTFilter;

    public ItemCardScreen(ItemCardContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.card = container.cardItem;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        currentMode = BaseCard.getTransferMode(card);
        currentChannel = BaseCard.getChannel(card);
        Button sizeButton;
        int baseX = width / 2, baseY = height / 2;
        int left = baseX - 85;
        int top = baseY - 100;


        leftWidgets.add(sizeButton = new Button(left, 0, 50, 20, new TranslatableComponent(BaseCard.TransferMode.values()[currentMode].name(), currentMode), (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            button.setMessage(new TranslatableComponent(BaseCard.TransferMode.values()[currentMode].name(), currentMode));
            PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel));
        }));

        leftWidgets.add(sizeButton = new Button(left, 0, 50, 20, new TranslatableComponent(String.valueOf(BaseCard.getChannel(card)), currentChannel), (button) -> {
            currentChannel = BaseCard.nextChannel(card);
            button.setMessage(new TranslatableComponent(String.valueOf(BaseCard.getChannel(card)), currentChannel));
            PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel));
        }));

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            leftWidgets.get(i).y = (top + 20) + (i * 25);
            addRenderableWidget(leftWidgets.get(i));
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        //drawString(matrixStack, Minecraft.getInstance().font, "Energy: " + menu.getEnergy(), 10, 10, 0xffffff);
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
    public void removed() {
        /*PacketHandler.sendToServer(new PacketChangeRange(this.beamRange));
        PacketHandler.sendToServer(new PacketChangeVolume(this.volume));
        PacketHandler.sendToServer(new PacketChangeFreezeDelay(this.freezeDelay));*/

        super.removed();
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

}
