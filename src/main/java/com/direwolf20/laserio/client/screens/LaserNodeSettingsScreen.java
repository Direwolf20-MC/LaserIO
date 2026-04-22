package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.network.data.ChangeColorPayload;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.Vec2i;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaserNodeSettingsScreen extends Screen {
    private final Identifier GUI = Identifier.fromNamespaceAndPath(LaserIO.MODID, "textures/gui/laser_node_settings.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 256;
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
    private ExtendedSlider sliderRed;
    private ExtendedSlider sliderGreen;
    private ExtendedSlider sliderBlue;
    private ExtendedSlider sliderAlpha;
    private ExtendedSlider sliderWrenchAlpha;
    private final MutableComponent[] sides = {
            Component.translatable("screen.laserio.down"),
            Component.translatable("screen.laserio.up"),
            Component.translatable("screen.laserio.north"),
            Component.translatable("screen.laserio.south"),
            Component.translatable("screen.laserio.west"),
            Component.translatable("screen.laserio.east"),
    };

    private Map<ExtendedSlider, IntConsumer> sliderMap = new HashMap<>();

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
            Button returnButton = new ExtendedButton(leftPos - 25, topPos + 1, 25, 20, Component.literal("<--"), (button) -> {
                ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) container.side));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            });
            leftWidgets.add(returnButton);
        }

        Button applyButton = new ExtendedButton(leftPos + 25, topPos + 150, 50, 20, Component.translatable("screen.laserio.apply"), (button) -> {
            syncColors();
        });
        leftWidgets.add(applyButton);

        Button defaultButton = new ExtendedButton(leftPos + 100, topPos + 150, 50, 20, Component.translatable("screen.laserio.default"), (button) -> {
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

        sliderRed = new ExtendedSlider(leftPos + 15, topPos + 45, 150, 15, Component.translatable("screen.laserio.red").append(": "), Component.empty(), 0, 255, this.laserRed, true) {
            @Override
            protected void applyValue() {
                laserRed = this.getValueInt();
            }
        };
        leftWidgets.add(sliderRed);
        sliderGreen = new ExtendedSlider(leftPos + 15, topPos + 65, 150, 15, Component.translatable("screen.laserio.green").append(": "), Component.empty(), 0, 255, this.laserGreen, true) {
            @Override
            protected void applyValue() {
                laserGreen = this.getValueInt();
            }
        };
        leftWidgets.add(sliderGreen);
        sliderBlue = new ExtendedSlider(leftPos + 15, topPos + 85, 150, 15, Component.translatable("screen.laserio.blue").append(": "), Component.empty(), 0, 255, this.laserBlue, true) {
            @Override
            protected void applyValue() {
                laserBlue = this.getValueInt();
            }
        };
        leftWidgets.add(sliderBlue);
        sliderAlpha = new ExtendedSlider(leftPos + 15, topPos + 105, 150, 15, Component.translatable("screen.laserio.alpha").append(": "), Component.empty(), 0, 255, this.laserAlpha, true) {
            @Override
            protected void applyValue() {
                laserAlpha = this.getValueInt();
            }
        };
        leftWidgets.add(sliderAlpha);
        sliderWrenchAlpha = new ExtendedSlider(leftPos + 15, topPos + 125, 150, 15, Component.translatable("screen.laserio.wrench").append(": "), Component.empty(), 0, 255, this.wrenchAlpha, true) {
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double deltaY) {
        this.sliderMap.forEach((slider, consumer) -> {
            if (slider.isMouseOver(mouseX, mouseY)) {
                slider.setValue(slider.getValueInt() + (delta > 0 ? 1 : -1));
                consumer.accept(slider.getValueInt());
            }
        });

        return false;
    }

    private void syncColors() {
        ClientPacketDistributor.sendToServer(new ChangeColorPayload(container.tile.getBlockPos(), new Color(laserRed, laserGreen, laserBlue, laserAlpha).getRGB(), wrenchAlpha));
    }

    protected void renderLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);
        guiGraphics.fill(tabs[container.side].x + 2, tabs[container.side].y + 2, tabs[container.side].x + 22, tabs[container.side].y + 14, 0xFFC6C6C6);
        guiGraphics.fill(tabs[container.side].x, tabs[container.side].y + 11, tabs[container.side].x + 2, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.fill(tabs[container.side].x + 22, tabs[container.side].y + 11, tabs[container.side].x + 24, tabs[container.side].y + 12, 0xFFFFFFFF);
        guiGraphics.text(font, Component.translatable("screen.laserio.settings"), imageWidth / 2 - font.width(Component.translatable("screen.laserio.settings")) / 2, 20, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "U", 15, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "D", 43, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "N", 71, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "S", 99, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "W", 128, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        guiGraphics.text(font, "E", 155, 7, Color.DARK_GRAY.getRGB() | 0xFF000000, false);
        for (Direction direction : Direction.values()) {
            ItemStack itemStack = getAdjacentBlock(direction);
            if (!itemStack.isEmpty()) {
                guiGraphics.item(itemStack, tabs[direction.ordinal()].x + 4, tabs[direction.ordinal()].y - 14, 0);
                if (MiscTools.inBounds(leftPos + tabs[direction.ordinal()].x + 4, topPos + tabs[direction.ordinal()].y - 14, 16, 16, mouseX, mouseY)) {
                    guiGraphics.setTooltipForNextFrame(font, itemStack, mouseX - leftPos, mouseY - topPos);
                }
            }
        }
        guiGraphics.nextStratum();
        int startX = 15;
        int startY = 30;
        guiGraphics.fill(startX, startY, startX + 150, startY + 10, new Color(laserRed, laserGreen, laserBlue, laserAlpha).getRGB());
        guiGraphics.pose().popMatrix();
    }

    protected ItemStack getAdjacentBlock(Direction direction) {
        BlockState blockState = container.playerEntity.level().getBlockState(this.container.tile.getBlockPos().relative(direction));
        ItemStack itemStack = blockState.getBlock().asItem().getDefaultInstance();
        return itemStack;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        if (MiscTools.inBounds(leftPos + tabs[1].x, topPos + tabs[1].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 1));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[0].x, topPos + tabs[0].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 0));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[2].x, topPos + tabs[2].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 2));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[3].x, topPos + tabs[3].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 3));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[4].x, topPos + tabs[4].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (MiscTools.inBounds(leftPos + tabs[5].x, topPos + tabs[5].y, 24, 12, x, y)) {
            ClientPacketDistributor.sendToServer(new OpenNodePayload(container.tile.getBlockPos(), (byte) 5));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return super.mouseClicked(event, doubleClick);
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
