package com.direwolf20.laserio.util;

import net.minecraft.core.Direction;

public class ExtractorCard {
    public final byte extractAmt;
    public final Direction direction;
    public final byte channel;

    public ExtractorCard(byte extractAmt, Direction direction, byte channel) {
        this.extractAmt = extractAmt;
        this.direction = direction;
        this.channel = channel;
    }
}
