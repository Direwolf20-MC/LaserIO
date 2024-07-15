package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.customhandler.DireItemContainerContents;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LaserIODataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.createDataComponents(LaserIO.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> BOUND_GLOBAL_POS = COMPONENTS.register("bound_global_pos", () -> DataComponentType.<GlobalPos>builder().persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DireItemContainerContents>> ITEMSTACK_HANDLER = COMPONENTS.register("itemstack_handler", () -> DataComponentType.<DireItemContainerContents>builder().persistent(DireItemContainerContents.CODEC).networkSynchronized(DireItemContainerContents.STREAM_CODEC).cacheEncoding().build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_TRANSFER_MODE = COMPONENTS.register("card_transfer_mode", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_CHANNEL = COMPONENTS.register("card_channel", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARD_EXTRACT_SPEED = COMPONENTS.register("card_extract_speed", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_MAX_BACKOFF = COMPONENTS.register("card_max_backoff", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Short>> CARD_PRIORITY = COMPONENTS.register("card_priority", () -> DataComponentType.<Short>builder().persistent(Codec.SHORT).networkSynchronized(ByteBufCodecs.SHORT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_SNEAKY = COMPONENTS.register("card_sneaky", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CARD_REGULATE = COMPONENTS.register("card_regulate", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARD_ROUND_ROBIN = COMPONENTS.register("card_round_robin", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_REDSTONE_MODE = COMPONENTS.register("card_redstone_mode", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CARD_EXACT = COMPONENTS.register("card_exact", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> CARD_REDSTONE_CHANNEL = COMPONENTS.register("card_redstone_channel", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CARD_AND_MODE = COMPONENTS.register("card_and_mode", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CARD_CLONER_ITEM_TYPE = COMPONENTS.register("card_cloner_item_type", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CARD_HOLDER_ACTIVE = COMPONENTS.register("card_holder_active", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> CARD_HOLDER_UUID = COMPONENTS.register("card_holder_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_CARD_EXTRACT_AMT = COMPONENTS.register("energy_card_extract_amt", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_CARD_EXTRACT_SPEED = COMPONENTS.register("energy_card_extract_speed", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_CARD_INSERT_LIMIT = COMPONENTS.register("energy_card_insert_limit", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_CARD_EXTRACT_LIMIT = COMPONENTS.register("energy_card_extract_limit", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FLUID_CARD_EXTRACT_AMT = COMPONENTS.register("fluid_card_extract_amt", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> ITEM_CARD_EXTRACT_AMT = COMPONENTS.register("item_card_extract_amt", () -> DataComponentType.<Byte>builder().persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> REDSTONE_CARD_STRONG = COMPONENTS.register("redstone_card_strong", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHEMICAL_CARD_EXTRACT_AMT = COMPONENTS.register("chemical_card_extract_amt", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_ALLOW = COMPONENTS.register("filter_allow", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_COMPARE = COMPONENTS.register("filter_compare", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> FILTER_COUNT_MBAMT = COMPONENTS.register("filter_amount_mbamt", () -> DataComponentType.<List<Integer>>builder().persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list())).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> FILTER_COUNT_SLOT_COUNTS = COMPONENTS.register("filter_amount_slot_counts", () -> DataComponentType.<List<Integer>>builder().persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list())).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> FILTER_TAG_TAGS = COMPONENTS.register("filter_tag_tags", () -> DataComponentType.<List<String>>builder().persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())).build());

    private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec) {
        return register(name, codec, null);
    }

    private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec, @Nullable final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        if (streamCodec == null) {
            return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
        } else {
            return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
        }
    }
}
