package com.direwolf20.laserio.util;

import com.mojang.math.Vector3f;

import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

public class MiscTools {
    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    public static boolean inBounds(Button button, double ox, double oy) {
        if (button == null)
            return false;
        return inBounds(button.x, button.y, button.getWidth(), button.getHeight(), ox, oy);
    }

    public static Vector3f findOffset(Direction direction, int slot, Vector3f[] offsets) {
        Vector3f offsetVector = offsets[slot].copy();
        switch (direction) {
            case UP -> {
                offsetVector.transform(Vector3f.XP.rotationDegrees(-270));
                offsetVector.add(0, 1, 0);
            }
            case DOWN -> {
                offsetVector.transform(Vector3f.XP.rotationDegrees(-90));
                offsetVector.add(0, 0, 1);
                //reverse = false;
            }
            //case NORTH -> offsetVector;
            case EAST -> {
                offsetVector.transform(Vector3f.YP.rotationDegrees(-90));
                offsetVector.add(1, 0, 0);
            }
            case SOUTH -> {
                offsetVector.transform(Vector3f.YP.rotationDegrees(-180));
                offsetVector.add(1, 0, 1);
            }
            case WEST -> {
                offsetVector.transform(Vector3f.YP.rotationDegrees(-270));
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
        MutableComponent current = new TranslatableComponent(string);
        current.setStyle(style);
        return current;
    }
}
