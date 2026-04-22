package com.direwolf20.laserio.client.color;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public record CardChannelTintSource(boolean useRedstoneChannelOnMode3) implements ItemTintSource {
    public static final MapCodec<CardChannelTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Codec.BOOL.optionalFieldOf("use_redstone_channel_on_mode_3", true).forGetter(CardChannelTintSource::useRedstoneChannelOnMode3)
            ).apply(i, CardChannelTintSource::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        int channel = (useRedstoneChannelOnMode3 && BaseCard.getTransferMode(itemStack) == (byte) 3)
                ? BaseCard.getRedstoneChannel(itemStack)
                : BaseCard.getChannel(itemStack);
        Color color = LaserNodeBERender.colors[channel];
        return color.getRGB();
    }

    @Override
    public MapCodec<CardChannelTintSource> type() {
        return MAP_CODEC;
    }
}
