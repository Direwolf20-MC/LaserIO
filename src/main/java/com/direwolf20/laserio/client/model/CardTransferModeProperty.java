package com.direwolf20.laserio.client.model;

import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CardTransferModeProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<CardTransferModeProperty> MAP_CODEC = MapCodec.unit(new CardTransferModeProperty());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return BaseCard.getTransferMode(itemStack);
    }

    @Override
    public MapCodec<CardTransferModeProperty> type() {
        return MAP_CODEC;
    }
}
