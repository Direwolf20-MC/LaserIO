package com.direwolf20.laserio.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for the card holder's slots. Mirrors {@link net.minecraft.world.item.component.ItemContainerContents}
 * but stores {@link ItemResource} + count rather than an ItemStackTemplate, so counts above an item's
 * {@code maxStackSize} survive serialization (vanilla {@link ItemStackTemplate#create()} validates strictly
 * and replaces over-stacked entries with EMPTY).
 */
public record CardHolderContents(List<Entry> entries) {
    public static final CardHolderContents EMPTY = new CardHolderContents(List.of());

    public static final Codec<CardHolderContents> CODEC = Entry.CODEC.listOf()
            .xmap(CardHolderContents::new, CardHolderContents::entries);

    public static final StreamCodec<RegistryFriendlyByteBuf, CardHolderContents> STREAM_CODEC =
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(CardHolderContents::new, CardHolderContents::entries);

    public int getCount(int index) {
        for (Entry entry : entries) {
            if (entry.index == index) return entry.count;
        }
        return 0;
    }

    public ItemResource getResource(int index) {
        for (Entry entry : entries) {
            if (entry.index == index) return entry.resource;
        }
        return ItemResource.EMPTY;
    }

    public CardHolderContents with(int index, ItemResource resource, int count) {
        List<Entry> next = new ArrayList<>(entries.size() + 1);
        boolean replaced = false;
        for (Entry entry : entries) {
            if (entry.index == index) {
                if (!resource.isEmpty() && count > 0) {
                    next.add(new Entry(index, resource, count));
                }
                replaced = true;
            } else {
                next.add(entry);
            }
        }
        if (!replaced && !resource.isEmpty() && count > 0) {
            next.add(new Entry(index, resource, count));
        }
        return new CardHolderContents(next);
    }

    public record Entry(int index, ItemResource resource, int count) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(0, 255).fieldOf("slot").forGetter(Entry::index),
                ItemResource.CODEC.fieldOf("item").forGetter(Entry::resource),
                Codec.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE).fieldOf("count").forGetter(Entry::count)
        ).apply(i, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::index,
                ItemResource.STREAM_CODEC, Entry::resource,
                ByteBufCodecs.VAR_INT, Entry::count,
                Entry::new
        );
    }
}
