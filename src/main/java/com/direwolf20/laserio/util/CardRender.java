package com.direwolf20.laserio.util;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import static com.direwolf20.laserio.util.MiscTools.findOffset;

public class CardRender {
    public Direction direction;
    public int cardSlot;
    public float r;
    public float g;
    public float b;
    public BlockPos startBlock;
    public BlockPos endBlock;
    public float diffX;
    public float diffY;
    public float diffZ;
    public Vector3f startLaser;
    public Vector3f endLaser;
    public float[] floatcolors;


    public CardRender(Direction direction, int cardSlot, ItemStack card, BlockPos start) {
        this.direction = direction;
        this.cardSlot = cardSlot;
        this.startBlock = start;
        endBlock = startBlock.relative(direction);

        if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
            r = 0f;
            g = 1f;
            b = 0f;
        } else if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.FLUID) {
            r = 0f;
            g = 0f;
            b = 1f;
        }
        Vector3f offset = findOffset(direction, cardSlot, LaserNodeBERender.offsets);
        diffX = endBlock.getX() + offset.x() - startBlock.getX();
        diffY = endBlock.getY() + offset.y() - startBlock.getY();
        diffZ = endBlock.getZ() + offset.z() - startBlock.getZ();

        boolean reverse = !direction.equals(Direction.DOWN);
        if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.EXTRACT)
            reverse = !reverse;
        floatcolors = LaserNodeBERender.colors[BaseCard.getChannel(card)].getColorComponents(new float[3]);
        if (reverse) {
            endLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            startLaser = new Vector3f(diffX, diffY, diffZ);
        } else {
            startLaser = new Vector3f(offset.x(), offset.y(), offset.z());
            endLaser = new Vector3f(diffX, diffY, diffZ);
        }
    }

}
