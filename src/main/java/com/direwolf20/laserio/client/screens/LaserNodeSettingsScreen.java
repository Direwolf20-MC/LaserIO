package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketChangeColor;
import com.direwolf20.laserio.common.network.packets.PacketOpenNode;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.Vec2i;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaserNodeSettingsScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/laser_node_settings.png");

    protected final LaserNodeContainer container;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int leftPos;
    protected int topPos;
    private int laserRed;
    private int laserGreen;
    private int laserBlue;
    private int laserAlpha;
    private int wrenchAlpha;
    private ForgeSlider sliderRed;
    private ForgeSlider sliderGreen;
    private ForgeSlider sliderBlue;
    private ForgeSlider sliderAlpha;
    private ForgeSlider sliderWrenchAlpha;

    private Map<ForgeSlider, IntConsumer> sliderMap = new HashMap<>();

    private final Vec2i[] tabs = {
            new Vec2i(34, 4), //Down
            new Vec2i(6, 4), //Up
            new Vec2i(62, 4),
            new Vec2i(90, 4),
            new Vec2i(118, 4),
            new Vec2i(146, 4)
    };

    public LaserNodeSettingsScreen(LaserNodeContainer container, Component name) {
        super(name);
        this.container = container;
        this.imageHeight = 181;
        Color color = container.tile.getColor();
        laserRed = color.getRed();
        laserGreen = color.getGreen();
        laserBlue = color.getBlue();
        laserAlpha = color.getAlpha();
        wrenchAlpha = container.tile.getWrenchAlpha();
    }

    @Override
    public void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        List<AbstractWidget> leftWidgets = new ArrayList<>();

        if (container.side != -1) {
            Button returnButton = new ExtendedButton(getGuiLeft() - 25, getGuiTop() + 1, 25, 20, Component.literal("<--"), (button) -> {
                PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) container.side));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            });
            leftWidgets.add(returnButton);
        }

        Button applyButton = new ExtendedButton(getGuiLeft() + 25, getGuiTop() + 150, 50, 20, Component.translatable("screen.laserio.apply"), (button) -> {
            syncColors();
        });
        leftWidgets.add(applyButton);

        Button defaultButton = new ExtendedButton(getGuiLeft() + 100, getGuiTop() + 150, 50, 20, Component.translatable("screen.laserio.default"), (button) -> {
            Color defaultColor = container.tile.getDefaultColor();
            laserRed = defaultColor.getRed();
            sliderRed.setValue(laserRed);
            laserGreen = defaultColor.getGreen();
            sliderGreen.setValue(laserGreen);
            laserBlue = defaultColor.getBlue();
            sliderBlue.setValue(laserBlue);
            laserAlpha = defaultColor.getAlpha();
            sliderAlpha.setValue(laserAlpha);
            wrenchAlpha = 0;
            sliderWrenchAlpha.setValue(0);
            syncColors();
        });
        leftWidgets.add(defaultButton);

        sliderRed = new ForgeSlider(getGuiLeft() + 15, getGuiTop() + 45, 150, 15, Component.translatable("screen.laserio.red").append(": "), Component.empty(), 0, 255, this.laserRed, true) {
            @Override
            protected void applyValue() {
                laserRed = this.getValueInt();
            }
        };
        leftWidgets.add(sliderRed);
        sliderGreen = new ForgeSlider(getGuiLeft() + 15, getGuiTop() + 65, 150, 15, Component.translatable("screen.laserio.green").append(": "), Component.empty(), 0, 255, this.laserGreen, true) {
            @Override
            protected void applyValue() {
                laserGreen = this.getValueInt();
            }
        };
        leftWidgets.add(sliderGreen);
        sliderBlue = new ForgeSlider(getGuiLeft() + 15, getGuiTop() + 85, 150, 15, Component.translatable("screen.laserio.blue").append(": "), Component.empty(), 0, 255, this.laserBlue, true) {
            @Override
            protected void applyValue() {
                laserBlue = this.getValueInt();
            }
        };
        leftWidgets.add(sliderBlue);
        sliderAlpha = new ForgeSlider(getGuiLeft() + 15, getGuiTop() + 105, 150, 15, Component.translatable("screen.laserio.alpha").append(": "), Component.empty(), 0, 255, this.laserAlpha, true) {
            @Override
            protected void applyValue() {
                laserAlpha = this.getValueInt();
            }
        };
        leftWidgets.add(sliderAlpha);
        sliderWrenchAlpha = new ForgeSlider(getGuiLeft() + 15, getGuiTop() + 125, 150, 15, Component.translatable("screen.laserio.wrench").append(": "), Component.empty(), 0, 255, this.wrenchAlpha, true) {
            @Override
            protected void applyValue() {
                wrenchAlpha = this.getValueInt();
            }
        };
        leftWidgets.add(sliderWrenchAlpha);

        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }

        // Used for scroll action
        this.sliderMap = Map.of(
                sliderRed, (a) -> laserRed = a,
                sliderGreen, (a) -> laserGreen = a,
                sliderBlue, (a) -> laserBlue = a,
                sliderAlpha, (a) -> laserAlpha = a,
                sliderWrenchAlpha, (a) -> wrenchAlpha = a
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.sliderMap.forEach((slider, consumer) -> {
            if (slider.isMouseOver(mouseX, mouseY)) {
                slider.setValue(slider.getValueInt() + (delta > 0 ? 1 : -1));
                consumer.accept(slider.getValueInt());
            }
        });

        return false;
    }

    private void syncColors() {
        PacketHandler.sendToServer(new PacketChangeColor(container.tile.getBlockPos(), new Color(laserRed, laserGreen, laserBlue, laserAlpha).getRGB(), wrenchAlpha));
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getGuiLeft(), getGuiTop(), 0);
        guiGraphics.fill(tabs[container.side].x + 2, tabs[container.side].y + 2, tabs[container.side].x + 22, tabs[container.side].y + 14, 0xFFC6C6C6);
        guiGraphics.fill(tabs[container.side].x, tabs[container.side].y + 11, tabs[container.side].x + 2, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.fill(tabs[container.side].x + 22, tabs[container.side].y + 11, tabs[container.side].x + 24, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.drawString(font, Component.translatable("screen.laserio.settings"), imageWidth / 2 - font.width(Component.translatable("screen.laserio.settings")) / 2, 20, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "U", 15, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "D", 43, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "N", 71, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "S", 99, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "W", 128, 7, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(font, "E", 155, 7, Color.DARK_GRAY.getRGB(), false);
        for (Direction direction : Direction.values()) {
            ItemStack itemStack = getAdjacentBlock(direction);
            if (!itemStack.isEmpty()) {
                guiGraphics.renderItem(itemStack, tabs[direction.ordinal()].x + 4, tabs[direction.ordinal()].y - 14, 0);
                if (MiscTools.inBounds(getGuiLeft() + tabs[direction.ordinal()].x + 4, getGuiTop() + tabs[direction.ordinal()].y - 14, 16, 16, mouseX, mouseY)) {
                    guiGraphics.renderTooltip(font, itemStack, mouseX - getGuiLeft(), mouseY - getGuiTop());
                }
            }
        }
        guiGraphics.pose().translate(0, 0, 100);
        int startX = 15;
        int startY = 30;
        guiGraphics.fill(startX, startY, startX + 150, startY + 10, new Color(laserRed, laserGreen, laserBlue, laserAlpha).getRGB());
        guiGraphics.pose().popPose();
    }

    protected ItemStack getAdjacentBlock(Direction direction) {
        BlockState blockState = container.playerEntity.level().getBlockState(this.container.tile.getBlockPos().relative(direction));
        ItemStack itemStack = blockState.getBlock().asItem().getDefaultInstance();
        return itemStack;
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (MiscTools.inBounds(getGuiLeft() + tabs[1].x, getGuiTop() + tabs[1].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 1));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[0].x, getGuiTop() + tabs[0].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 0));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[2].x, getGuiTop() + tabs[2].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 2));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[3].x, getGuiTop() + tabs[3].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 3));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[4].x, getGuiTop() + tabs[4].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(getGuiLeft() + tabs[5].x, getGuiTop() + tabs[5].y, 24, 12, x, y)) {
            PacketHandler.sendToServer(new PacketOpenNode(container.tile.getBlockPos(), (byte) 5));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return super.mouseClicked(x, y, btn);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }
}