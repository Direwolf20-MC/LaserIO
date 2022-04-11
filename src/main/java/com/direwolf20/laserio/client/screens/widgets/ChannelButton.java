package com.direwolf20.laserio.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

import java.awt.*;

public class ChannelButton extends Button {
    private int channel;
    private final Color colors[] = {
            new Color(0xf9ffff),
            new Color(0xf9801d),
            new Color(0xc64fbd),
            new Color(0x3ab3da),
            new Color(0xffd83d),
            new Color(0x80c71f),
            new Color(0xf38caa),
            new Color(0x474f52),
            new Color(0x9c9d97),
            new Color(0x169c9d),
            new Color(0x8932b7),
            new Color(0x3c44a9),
            new Color(0x825432),
            new Color(0x5d7c15),
            new Color(0xb02e26),
            new Color(0x1d1c21),
    };

    public ChannelButton(int widthIn, int heightIn, int width, int height, int channel, OnPress onPress) {
        super(widthIn, heightIn, width, height, TextComponent.EMPTY, onPress);
        this.channel = channel;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        fill(stack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFFa8a8a8);
        fill(stack, this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2, colors[channel].getRGB());
        Font font = Minecraft.getInstance().font;
        int j = this.channel == 0 ? colors[15].getRGB() : getFGColor();
        font.draw(stack, String.valueOf(channel), this.x + this.width / 4, this.y + (this.height - 8) / 2, j);
        //drawCenteredString(stack, font, String.valueOf(channel), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
