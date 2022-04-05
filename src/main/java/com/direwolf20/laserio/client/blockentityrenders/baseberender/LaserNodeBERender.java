package com.direwolf20.laserio.client.blockentityrenders.baseberender;

import com.direwolf20.laserio.client.renderer.RenderUtils;
import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;

import java.awt.*;

public class LaserNodeBERender extends BaseLaserBERender<LaserNodeBE> {
    private final Vector3f[] offsets = {
            new Vector3f(0.675f, 0.675f, 0.5f),
            new Vector3f(0.5f, 0.675f, 0.5f),
            new Vector3f(0.325f, 0.675f, 0.5f),
            new Vector3f(0.675f, 0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(0.325f, 0.5f, 0.5f),
            new Vector3f(0.675f, 0.325f, 0.5f),
            new Vector3f(0.5f, 0.325f, 0.5f),
            new Vector3f(0.325f, 0.325f, 0.5f)
    };
    private final Color colors[] = {
            new Color(255, 255, 255),
            new Color(249, 128, 29),
            new Color(198, 79, 189),
            new Color(58, 179, 218),
            new Color(255, 216, 61),
            new Color(128, 199, 31),
            new Color(243, 140, 170),
            new Color(71, 79, 82),
            new Color(156, 157, 151),
            new Color(22, 156, 157),
            new Color(137, 50, 183),
            new Color(60, 68, 169),
            new Color(130, 84, 50),
            new Color(93, 124, 21),
            new Color(176, 46, 38),
            new Color(29, 28, 33)
    };


    public LaserNodeBERender(BlockEntityRendererProvider.Context context) {
        super(context);

    }

    @Override
    public void render(LaserNodeBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        super.render(blockentity, partialTicks, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn);
        for (Direction direction : Direction.values()) {
            blockentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).ifPresent(h -> {
                for (int slot = 0; slot < h.getSlots(); slot++) {
                    ItemStack card = h.getStackInSlot(slot);
                    if (card.getItem() instanceof BaseCard) {
                        if (((BaseCard) card.getItem()).getCardType() == BaseCard.CardType.ITEM) {
                            float[] floatcolors = colors[BaseCard.getChannel(card)].getColorComponents(new float[3]);
                            boolean reverse = direction.equals(Direction.DOWN) ? true : false;
                            if (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT) reverse = !reverse;
                            RenderUtils.drawConnectingLasers(blockentity, BlockPos.ZERO, BlockPos.ZERO.relative(direction), matrixStackIn, bufferIn, findOffset(direction, slot), 0f, 1f, 0f, 0.5f, 0.025f, floatcolors[0], floatcolors[1], floatcolors[2], 1f, 0.0125f, reverse);
                        }
                    }
                }
            });
        }
    }

    public Vector3f findOffset(Direction direction, int slot) {
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
