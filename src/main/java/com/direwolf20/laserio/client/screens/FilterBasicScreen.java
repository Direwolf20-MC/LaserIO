package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterMod;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import com.direwolf20.laserio.common.network.packets.PacketUpdateFilter;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class FilterBasicScreen extends AbstractContainerScreen<FilterBasicContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/basicfilter.png");

    protected final FilterBasicContainer container;
    private ItemStack filter;
    private boolean isAllowList;
    private boolean isCompareNBT;

    public FilterBasicScreen(FilterBasicContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.filter = container.filterItem;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 10, 16, 16, mouseX, mouseY)) {
            if (isAllowList)
                this.renderTooltip(matrixStack, Component.translatable("screen.laserio.allowlist"), mouseX, mouseY);
            else
                this.renderTooltip(matrixStack, Component.translatable("screen.laserio.denylist"), mouseX, mouseY);
        }
        if (!(filter.getItem() instanceof FilterMod)) {
            if (MiscTools.inBounds(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, mouseX, mouseY)) {
                if (isCompareNBT)
                    this.renderTooltip(matrixStack, Component.translatable("screen.laserio.nbttrue"), mouseX, mouseY);
                else
                    this.renderTooltip(matrixStack, Component.translatable("screen.laserio.nbtfalse"), mouseX, mouseY);
            }
        }
    }

    @Override
    public void init() {
        super.init();
        List<AbstractWidget> leftWidgets = new ArrayList<>();

        this.isAllowList = FilterBasic.getAllowList(filter);
        this.isCompareNBT = FilterBasic.getCompareNBT(filter);

        ResourceLocation[] allowListTextures = new ResourceLocation[2];
        allowListTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlistfalse.png");
        allowListTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlisttrue.png");

        leftWidgets.add(new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, allowListTextures, isAllowList ? 1 : 0, (button) -> {
            isAllowList = !isAllowList;
            ((ToggleButton) button).setTexturePosition(isAllowList ? 1 : 0);
        }));

        if (!(filter.getItem() instanceof FilterMod)) {
            ResourceLocation[] nbtTextures = new ResourceLocation[2];
            nbtTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbtfalse.png");
            nbtTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbttrue.png");

            leftWidgets.add(new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, nbtTextures, isCompareNBT ? 1 : 0, (button) -> {
                isCompareNBT = !isCompareNBT;
                ((ToggleButton) button).setTexturePosition(isCompareNBT ? 1 : 0);
            }));
        }

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addRenderableWidget(leftWidgets.get(i));
        }
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        //font.draw(stack, Component.translatable("screen.laserio.allowlist").getString(), 5, 5, Color.DARK_GRAY.getRGB());
        //font.draw(stack, Component.translatable("screen.laserio.comparenbt").getString(), 7, 35, Color.DARK_GRAY.getRGB());
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
        stack = stack.copy().split(hoveredSlot.getMaxStackSize()); // Limit to slot limit
        if (ItemHandlerHelper.canItemStacksStack(stack, container.filterItem)) return true;
        hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
        PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, stack, stack.getCount()));

        return true;
    }

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }

}
