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

public class LaserNodeBERender extends BaseLaserBERender<LaserNodeBE> {
    Vector3f[] offsets = new Vector3f[9];

    public LaserNodeBERender(BlockEntityRendererProvider.Context context) {
        super(context);
        offsets[0] = new Vector3f(0.75f, 0.75f, 0.5f);
        offsets[1] = new Vector3f(0.5f, 0.75f, 0.5f);
        offsets[2] = new Vector3f(0.25f, 0.75f, 0.5f);
        offsets[3] = new Vector3f(0.75f, 0.5f, 0.5f);
        offsets[4] = new Vector3f(0.5f, 0.5f, 0.5f);
        offsets[5] = new Vector3f(0.25f, 0.5f, 0.5f);
        offsets[6] = new Vector3f(0.75f, 0.25f, 0.5f);
        offsets[7] = new Vector3f(0.5f, 0.25f, 0.5f);
        offsets[8] = new Vector3f(0.25f, 0.25f, 0.5f);
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
                            /*float offsetX = direction.getNormal().getX() != 0 ? direction.getNormal().getX()*(0f)+0.5f : direction.getNormal().getX()*(0.25f)+0.5f;
                            float offsetY = direction.getNormal().getY() != 0 ? direction.getNormal().getY()*(0f)+0.5f : direction.getNormal().getY()*(0.25f)+0.5f;
                            float offsetZ = direction.getNormal().getZ() != 0 ? direction.getNormal().getZ()*(0f)+0.5f : direction.getNormal().getZ()*(0.25f)+0.5f;*/
                            boolean reverse = false;
                            offsets[0] = new Vector3f(0.675f, 0.675f, 0.5f);
                            offsets[1] = new Vector3f(0.5f, 0.675f, 0.5f);
                            offsets[2] = new Vector3f(0.325f, 0.675f, 0.5f);
                            offsets[3] = new Vector3f(0.675f, 0.5f, 0.5f);
                            offsets[4] = new Vector3f(0.5f, 0.5f, 0.5f);
                            offsets[5] = new Vector3f(0.325f, 0.5f, 0.5f);
                            offsets[6] = new Vector3f(0.675f, 0.325f, 0.5f);
                            offsets[7] = new Vector3f(0.5f, 0.325f, 0.5f);
                            offsets[8] = new Vector3f(0.325f, 0.325f, 0.5f);
                            Vector3f offsetVector = offsets[slot];
                            switch (direction) {
                                case UP -> {
                                    offsetVector.transform(Vector3f.XP.rotationDegrees(-270));
                                    offsetVector.add(0, 1, 0);
                                }
                                case DOWN -> {
                                    offsetVector.transform(Vector3f.XP.rotationDegrees(-90));
                                    offsetVector.add(0, 0, 1);
                                    reverse = false;
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
                            float offsetX = offsetVector.x();
                            float offsetY = offsetVector.y();
                            float offsetZ = offsetVector.z();
                            RenderUtils.drawConnectingLasers(blockentity, BlockPos.ZERO, BlockPos.ZERO.relative(direction), matrixStackIn, bufferIn, offsetX, offsetY, offsetZ, 0f, 1f, 0f, 0.5f, 0.0125f, reverse);
                        }
                    }
                }
            });
        }
    }

}
