package com.direwolf20.laserio.util;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;

public class MiscTools {
    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
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
}
