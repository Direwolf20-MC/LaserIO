package com.direwolf20.laserio.client.events;

import com.direwolf20.laserio.client.renderer.LaserIOItemRenderer;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.opengl.GL11;

public class EventTooltip {
    private static final int STACKS_PER_LINE = 5;

    public static class CopyPasteTooltipComponent implements ClientTooltipComponent {
        Data tooltipData;

        public CopyPasteTooltipComponent(Data tooltipComponent) {
            tooltipData = tooltipComponent;
        }

        @Override
        public int getHeight() {
            return Screen.hasShiftDown() ? 10 * tooltipData.rows : 0;
        }

        @Override
        public int getWidth(Font font) {
            return Screen.hasShiftDown() && tooltipData.filterData != null ? STACKS_PER_LINE * 9 : 0;
        }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int p_194053_) {
            if (this.tooltipData.stack == null || (this.tooltipData.stack.getItem() instanceof FilterTag))
                return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || !Screen.hasShiftDown() || tooltipData.filterData == null)
                return;

            int bx = x - 3;
            int by = y - 6;
            int j = 0;

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            for (int i = 0; i < tooltipData.filterData.getSlots(); i++) {
                ItemStack filterStack = tooltipData.filterData.getStackInSlot(i);
                int xx = bx + (j % STACKS_PER_LINE) * 9;
                int yy = by + (j / STACKS_PER_LINE) * 10;
                if (!filterStack.isEmpty()) renderFilterStack(poseStack, itemRenderer, filterStack, xx, yy);
                j++;
            }
        }

        public static class Data implements TooltipComponent {
            public ItemStack stack;
            public ItemStackHandler filterData;
            public int rows = 0;

            public Data(ItemStack stack) {
                this.stack = stack;

                if (stack.getItem() instanceof FilterBasic)
                    this.filterData = FilterBasic.getInventory(stack);
                else if (stack.getItem() instanceof FilterCount)
                    this.filterData = FilterCount.getInventory(stack);

                //Figure out how many rows to render - since we want to match the card UI we have to go row by row checking for all empty
                for (int slot = 0; slot < 5; slot++) {
                    if (!filterData.getStackInSlot(slot).isEmpty()) {
                        rows = 1;
                        break;
                    }
                }
                for (int slot = 5; slot < 10; slot++) {
                    if (!filterData.getStackInSlot(slot).isEmpty()) {
                        rows = 2;
                        break;
                    }
                }
                for (int slot = 10; slot < 15; slot++) {
                    if (!filterData.getStackInSlot(slot).isEmpty()) {
                        rows = 3;
                        break;
                    }
                }
            }
        }

    }

    private static void renderFilterStack(PoseStack matrices, ItemRenderer itemRenderer, ItemStack itemStack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        LaserIOItemRenderer tooltipItemRenderer = new LaserIOItemRenderer(mc.getTextureManager(), mc.getModelManager(), mc.getItemColors(), blockentitywithoutlevelrenderer);

        String s1 = Integer.toString(itemStack.getCount());
        int w1 = mc.font.width(s1);

        matrices.pushPose();
        matrices.scale(.5f, .5f, 0);
        tooltipItemRenderer.renderGuiItem(8f, itemStack, x, y, itemRenderer.getModel(itemStack, null, null, 0));
        //itemRenderer.renderGuiItemDecorations(mc.font, itemStack, x, y);
        tooltipItemRenderer.renderGuiItemDecorations(mc.font, itemStack, x, y, null, 0.5f);
        matrices.popPose();

        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();

        matrices.pushPose();
        matrices.translate(x + 8 - w1 / 4f, y + 12, itemRenderer.blitOffset + 250);
        matrices.scale(.5f, .5f, 0);
        //mc.font.draw(matrices, s1, 0, 0, 0xFFFFFF);
        matrices.popPose();

        /*if (hasReq) {

            if (count < req) {
                String fs = Integer.toString(req - count);
                String s2 = "(" + fs + ")";
                int w2 = mc.font.width(s2);

                matrices.pushPose();
                matrices.translate(x + 8 - w2 / 4f, y + 17, itemRenderer.blitOffset + 250);
                matrices.scale(.5f, .5f, 0);
                mc.font.drawInBatch(s2, 0, 0, 0xFF0000, true, matrices.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
                matrices.popPose();

                missingCount = (req - count);
            }
        }*/

        irendertypebuffer$impl.endBatch();
    }
}

