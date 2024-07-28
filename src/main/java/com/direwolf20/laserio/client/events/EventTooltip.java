package com.direwolf20.laserio.client.events;

import com.direwolf20.laserio.client.screens.LaserGuiGraphics;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.items.filters.FilterTag;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

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
        public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
            if (this.tooltipData.stack == null)
                return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || !Screen.hasShiftDown())
                return;
            if (tooltipData.filterData == null && tooltipData.tags == null)
                return;

            int bx = x - 3;
            int by = y - 6;
            int j = 0;

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            if (tooltipData.stack.getItem() instanceof FilterTag) {
                for (int i = 0; i < tooltipData.tags.size(); i++) {
                    int xx = bx + (j % STACKS_PER_LINE) * 9;
                    int yy = by + (j / STACKS_PER_LINE) * 10;
                    String tag = tooltipData.tags.get(i);
                    if (!tag.isEmpty()) renderTagStack(guiGraphics, tag, xx, yy);
                    j++;
                }
            } else {
                for (int i = 0; i < tooltipData.filterData.getSlots(); i++) {
                    int xx = bx + (j % STACKS_PER_LINE) * 9;
                    int yy = by + (j / STACKS_PER_LINE) * 10;
                    ItemStack filterStack = tooltipData.filterData.getStackInSlot(i);
                    if (!filterStack.isEmpty()) renderFilterStack(guiGraphics, filterStack, xx, yy);
                    j++;
                }
            }
        }

        public static class Data implements TooltipComponent {
            public ItemStack stack;
            public ComponentItemHandler filterData;
            public List<String> tags;
            public int rows = 0;

            public Data(ItemStack stack) {
                this.stack = stack;

                if (stack.getItem() instanceof FilterBasic)
                    this.filterData = FilterBasic.getInventory(stack);
                else if (stack.getItem() instanceof FilterCount)
                    this.filterData = FilterCount.getInventory(stack);
                else if (stack.getItem() instanceof FilterTag)
                    this.tags = FilterTag.getTags(stack);
                else return;

                if (stack.getItem() instanceof FilterTag) {
                    int itemStackMin = 0;
                    int itemStackMax = Math.min(15, tags.size());
                    tags = tags.subList(itemStackMin, itemStackMax);
                    tags.sort(Comparator.naturalOrder());
                    rows = (int) Math.ceil((double) tags.size() / 5);
                } else {
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

    }

    private static void renderFilterStack(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        LaserGuiGraphics laserGuiGraphics = new LaserGuiGraphics(mc, guiGraphics.bufferSource());
        laserGuiGraphics.renderItemScale(8f, itemStack, x, y);
        laserGuiGraphics.renderItemDecorations(mc.font, itemStack, x, y, null);
    }

    private static void renderTagStack(GuiGraphics guiGraphics, String tag, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        List<Holder<Item>> tagItems = BuiltInRegistries.ITEM.getTag(ItemTags.create(ResourceLocation.parse(tag))).stream().flatMap(HolderSet.ListBacked::stream).toList();
        if (tagItems.size() > 0) {
            ItemStack drawStack = new ItemStack(tagItems.get((int) (mc.level.getGameTime() / 20) % tagItems.size()));
            renderFilterStack(guiGraphics, drawStack, x, y);
        }

        List<Holder<Fluid>> tagFluids = BuiltInRegistries.FLUID.getTag(FluidTags.create(ResourceLocation.parse(tag))).stream().flatMap(HolderSet.ListBacked::stream).toList();
        if (tagFluids.size() > 0) {
            FluidStack drawFluidStack = new FluidStack(tagFluids.get((int) (mc.level.getGameTime() / 20) % tagFluids.size()), 1000);
            if (!drawFluidStack.isEmpty()) {
                ItemStack bucketStack = new ItemStack(drawFluidStack.getFluid().getBucket(), 1);
                if (!bucketStack.isEmpty())
                    renderFilterStack(guiGraphics, bucketStack, x, y);
            }
        }
    }
}

