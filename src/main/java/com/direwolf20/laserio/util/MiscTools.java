package com.direwolf20.laserio.util;

import com.mojang.math.Axis;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MiscTools {
    public static StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            Vec3::x,
            ByteBufCodecs.DOUBLE,
            Vec3::y,
            ByteBufCodecs.DOUBLE,
            Vec3::z,
            Vec3::new
    );

    public static Level getLevel(MinecraftServer server, GlobalPos globalPos) {
        if (server == null)
            return null;//level = Minecraft.getInstance().level;
        else
            return server.getLevel(globalPos.dimension());
    }

    public static CompoundTag globalPosToNBT(GlobalPos globalPos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", globalPos.dimension().location().toString());
        tag.put("blockpos", NbtUtils.writeBlockPos(globalPos.pos()));
        return tag;
    }

    public static GlobalPos nbtToGlobalPos(CompoundTag tag) {
        ResourceKey<Level> levelKey;
        if (tag.contains("dimension"))
            levelKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")));
        else
            return null;
        BlockPos blockPos = NbtUtils.readBlockPos(tag, "blockpos").orElse(BlockPos.ZERO);
        return blockPos.equals(BlockPos.ZERO) ? null : GlobalPos.of(levelKey, blockPos);
    }

    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    public static Vector3f findOffset(Direction direction, int slot, Vector3f[] offsets) {
        Vector3f offsetVector = new Vector3f(offsets[slot]);
        switch (direction) {
            case UP -> {
                Quaternionf quaternionf = Axis.XP.rotationDegrees(-270);
                offsetVector = quaternionf.transform(offsetVector);
                //offsetVector.transform(Vector3f.XP.rotationDegrees(-270));
                offsetVector.add(0, 1, 0);
            }
            case DOWN -> {
                Quaternionf quaternionf = Axis.XP.rotationDegrees(-90);
                offsetVector = quaternionf.transform(offsetVector);
                //offsetVector.transform(Vector3f.XP.rotationDegrees(-90));
                offsetVector.add(0, 0, 1);
                //reverse = false;
            }
            //case NORTH -> offsetVector;
            case EAST -> {
                Quaternionf quaternionf = Axis.YP.rotationDegrees(-90);
                offsetVector = quaternionf.transform(offsetVector);
                //offsetVector.transform(Vector3f.YP.rotationDegrees(-90));
                offsetVector.add(1, 0, 0);
            }
            case SOUTH -> {
                Quaternionf quaternionf = Axis.YP.rotationDegrees(-180);
                offsetVector = quaternionf.transform(offsetVector);
                //offsetVector.transform(Vector3f.YP.rotationDegrees(-180));
                offsetVector.add(1, 0, 1);
            }
            case WEST -> {
                Quaternionf quaternionf = Axis.YP.rotationDegrees(-270);
                offsetVector = quaternionf.transform(offsetVector);
                //offsetVector.transform(Vector3f.YP.rotationDegrees(-270));
                offsetVector.add(0, 0, 1);
            }
        }
        return offsetVector;
    }

    public static ListTag stringListToNBT(List<String> list) {
        ListTag nbtList = new ListTag();
        for (String string : list) {
            CompoundTag tag = new CompoundTag();
            tag.putString("list", string);
            nbtList.add(tag);
        }
        return nbtList;
    }

    public static List<String> NBTToStringList(ListTag nbtList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag tag = nbtList.getCompound(i);
            list.add(tag.getString("list"));
        }
        return list;
    }

    public static MutableComponent tooltipMaker(String string, int color) {
        Style style = Style.EMPTY;
        style = style.withColor(color);
        MutableComponent current = Component.translatable(string);
        current.setStyle(style);
        return current;
    }
}
