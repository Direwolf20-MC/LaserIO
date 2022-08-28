package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.renderer.LaserIOItemRenderer;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import com.direwolf20.laserio.common.network.packets.PacketUpdateFilter;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class FilterCountScreen extends AbstractContainerScreen<FilterCountContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/filtercount.png");

    protected final FilterCountContainer container;
    private ItemStack filter;
    private boolean isAllowList;
    private boolean isCompareNBT;

    public FilterCountScreen(FilterCountContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.filter = container.filterItem;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        updateItemCounts();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, mouseX, mouseY)) {
            if (isCompareNBT)
                this.renderTooltip(matrixStack, Component.translatable("screen.laserio.nbttrue"), mouseX, mouseY);
            else
                this.renderTooltip(matrixStack, Component.translatable("screen.laserio.nbtfalse"), mouseX, mouseY);
        }
    }

    public void updateItemCounts() {
        IItemHandler handler = container.handler;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            stack.setCount(container.getStackSize(i));
        }
    }

    @Override
    public void init() {
        super.init();
        Minecraft minecraft = Minecraft.getInstance();
        BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        this.itemRenderer = new LaserIOItemRenderer(minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer);
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        this.isAllowList = FilterCount.getAllowList(filter);
        this.isCompareNBT = FilterCount.getCompareNBT(filter);

        ResourceLocation[] nbtTextures = new ResourceLocation[2];
        nbtTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbtfalse.png");
        nbtTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbttrue.png");

        leftWidgets.add(new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, nbtTextures, isCompareNBT ? 1 : 0, (button) -> {
            isCompareNBT = !isCompareNBT;
            ((ToggleButton) button).setTexturePosition(isCompareNBT ? 1 : 0);
        }));

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        //font.draw(stack, Component.translatable("screen.laserio.allowlist").getString(), 5, 5, Color.DARK_GRAY.getRGB());
        //font.draw(stack, Component.translatable("screen.laserio.comparenbt").getString(), 5, 25, Color.DARK_GRAY.getRGB());
        //super.renderLabels(matrixStack, x, y);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        PacketHandler.sendToServer(new PacketUpdateFilter(isAllowList, isCompareNBT));
        super.onClose();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_);
        if (p_keyPressed_1_ == 256 || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();

            return true;
        }

        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (hoveredSlot == null || !(hoveredSlot instanceof FilterBasicSlot))
            return super.mouseClicked(x, y, btn);

        // By splitting the stack we can get air easily :) perfect removal basically
        ItemStack stack = this.menu.getCarried();// getMinecraft().player.inventoryMenu.getCarried();
        if (!stack.isEmpty()) {
            stack = stack.copy();
            hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
            if (ItemHandlerHelper.canItemStacksStack(stack, container.filterItem)) return true;
            PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, stack, stack.getCount()));
            container.handler.setStackInSlotSave(hoveredSlot.index, stack); //We do this for continuity between client/server -- not needed in cardItemScreen
        } else {
            ItemStack slotStack = hoveredSlot.getItem();
            if (slotStack.isEmpty()) return true;
            if (btn == 2) { //Todo IMC Inventory Sorter so this works
                slotStack.setCount(0);
                PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, slotStack, slotStack.getCount()));
                return true;
            }
            int amt = (btn == 0) ? 1 : -1;
            if (Screen.hasShiftDown()) amt *= 10;
            if (Screen.hasControlDown()) amt *= 64;
            if (amt + slotStack.getCount() > 4096) amt = 4096 - slotStack.getCount();
            slotStack.grow(amt);

            PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, slotStack, slotStack.getCount()));
            container.handler.setStackInSlotSave(hoveredSlot.index, slotStack); //We do this for continuity between client/server -- not needed in cardItemScreen
        }


        return true;
    }

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hoveredSlot == null || !(hoveredSlot instanceof FilterBasicSlot))
            return super.mouseScrolled(mouseX, mouseY, delta);

        ItemStack slotStack = hoveredSlot.getItem();
        if (slotStack.isEmpty()) return true;
        int amt = (int) delta;
        if (Screen.hasShiftDown()) amt *= 10;
        if (Screen.hasControlDown()) amt *= 64;
        if (amt + slotStack.getCount() > 4096) amt = 4096 - slotStack.getCount();
        if (slotStack.getCount() + amt <= 0)
            amt = (slotStack.getCount() * -1) + 1;
        slotStack.grow(amt);

        PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, slotStack, slotStack.getCount()));
        return true;
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }

}
