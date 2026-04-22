package com.direwolf20.laserio.client.color;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public record CardRedstoneTintSource() implements ItemTintSource {
    public static final MapCodec<CardRedstoneTintSource> MAP_CODEC = MapCodec.unit(new CardRedstoneTintSource());

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        Color color = LaserNodeBERender.colors[CardRedstone.getRedstoneChannel(itemStack)];
        return color.getRGB();
    }

    @Override
    public MapCodec<CardRedstoneTintSource> type() {
        return MAP_CODEC;
    }
}
