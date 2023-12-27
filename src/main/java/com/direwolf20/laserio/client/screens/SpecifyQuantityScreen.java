package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.renderer.LaserIOItemRenderer;
import com.direwolf20.laserio.client.renderer.LaserIOItemRendererFluid;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.SpecifyQuantityContainer;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class SpecifyQuantityScreen extends AbstractContainerScreen<SpecifyQuantityContainer> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/specify_quantity.png");

    private final AbstractContainerScreen<?> parent;

    private EditBox quantityBox;
    protected Button okButton;
    protected Button cancelButton;

    LaserIOItemRenderer itemRenderer;
    LaserIOItemRendererFluid fluidRenderer;

    private final Slot filterSlot;
    private final ItemStack slotStack;

    private final int initialQuantity;
    protected int[] increments;
    protected int maxAmount;
    protected boolean isFluid;

    public SpecifyQuantityScreen(AbstractContainerScreen<?> parent, Player player, Slot slot, ItemStack slotStack, int quantity, boolean isFluid) {
        super(new SpecifyQuantityContainer(0, player, slot.getItem(), quantity), player.getInventory(), Component.translatable("specify_quantity"));
        this.parent = parent;
        this.initialQuantity = quantity;
        this.filterSlot = slot;
        this.slotStack = slotStack;

        this.imageWidth = 172;
        this.imageHeight = 100;
        Minecraft minecraft = Minecraft.getInstance();
        BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        itemRenderer = new LaserIOItemRenderer(Minecraft.getInstance(), minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer);
        fluidRenderer = new LaserIOItemRendererFluid(minecraft, minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer, this);

        this.isFluid = isFluid;

        if (this.isFluid) {
            this.increments = new int[] { 10, 100, 1000 };
            this.maxAmount = 4096000;
        }
        else {
            this.increments = new int[]{1, 10, 64};
            this.maxAmount = 4096;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        guiGraphics = isFluid ? new LaserGuiGraphicsFluid(Minecraft.getInstance(), guiGraphics.bufferSource(), this) :
                new LaserGuiGraphics(Minecraft.getInstance(), guiGraphics.bufferSource());

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public Button addButton(int x, int y, int w, int h, Component text, boolean enabled, boolean visible, Button.OnPress onPress) {
        Button button = Button.builder(text, onPress).pos(x, y).size(w, h).build();
        button.active = enabled;
        button.visible = visible;
        addRenderableWidget(button);
        return button;
    }

    public void postInit(int x, int y) {

        int okCancelPosX = 114;
        int okCancelPosY = 33;
        int okCancelWidth = 50;

        okButton = addButton(x + okCancelPosX, y + okCancelPosY, okCancelWidth, 20, Component.translatable("gui.ok"), true, true, btn -> onOkButtonPressed());
        cancelButton = addButton(x + okCancelPosX, y + okCancelPosY + 24, okCancelWidth, 20, Component.translatable("gui.cancel"), true, true, btn -> close());

        int quantityPosX = 9;
        int quantityPosY = 51;

        quantityBox = new EditBox(font, x + quantityPosX, y + quantityPosY, 69 - 6, font.lineHeight, Component.literal(""));
        quantityBox.setBordered(false);
        quantityBox.setVisible(true);
        quantityBox.setValue(String.valueOf(this.initialQuantity));
        quantityBox.setCanLoseFocus(false);
        quantityBox.setFocused(true);
        quantityBox.setResponder(text -> {
            int amount = 0;
            try {
                amount = Integer.parseInt(quantityBox.getValue());
            } catch (NumberFormatException e) {
                // NO OP
            }

            if (amount > maxAmount) {
                quantityBox.setValue(String.valueOf(maxAmount));
            }
        });

        addRenderableWidget(quantityBox);

        setFocused(quantityBox);

        int[] increments = this.increments;

        int xx = 7;
        int width = 30;

        for (int i = 0; i < 3; i++) {
            int increment = increments[i];

            Component text = Component.literal("+" + increment);
            if (text.getString().equals("+1000")) {
                text = Component.literal("+1B");
            }

            addButton(x + xx, y + 20, width, 20, text, true, true, btn -> onIncrementButtonClicked(increment));

            xx += width + 3;
        }

        xx = 7;
        for (int i = 0; i < 3; i++) {
            int increment = increments[i];

            Component text = Component.literal("-" + increment);
            if (text.getString().equals("-1000")) {
                text = Component.literal("-1B");
            }

            addButton(x + xx, y + this.imageHeight - 20 - 7, width, 20, text, true, true, btn -> onIncrementButtonClicked(-increment));

            xx += width + 3;
        }
    }

    @Override
    public void init() {
        super.init();

        this.clearWidgets();

        postInit(leftPos, topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void close() {
        if (minecraft != null)
            minecraft.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        if (quantityBox.isFocused() && (p_keyPressed_1_ == GLFW.GLFW_KEY_ENTER || p_keyPressed_1_ == GLFW.GLFW_KEY_KP_ENTER)) {
            onOkButtonPressed();
        }

        if (quantityBox.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }

        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    private void onIncrementButtonClicked(int increment) {
        int oldAmount = 0;

        try {
            oldAmount = Integer.parseInt(quantityBox.getValue());
        } catch (NumberFormatException e) {
            // NO OP
        }

        int newAmount = increment;
        newAmount = Math.max(1, ((oldAmount == 1 && newAmount != 1) ? 0 : oldAmount) + newAmount);

        if (newAmount > maxAmount) {
            newAmount = maxAmount;
        }

        quantityBox.setValue(String.valueOf(newAmount));
    }

    private void onOkButtonPressed() {
        try {
            int quantity = Integer.parseInt(quantityBox.getValue());

            LOGGER.info("Setting quantity to: {}", quantity);

            if (!this.isFluid)
                PacketHandler.sendToServer(new PacketGhostSlot(this.filterSlot.index, this.slotStack, quantity));
            else
                PacketHandler.sendToServer(new PacketGhostSlot(this.filterSlot.index, this.slotStack, this.slotStack.getCount(), quantity));

            close();
        } catch (NumberFormatException e) {
            // NO OP
        }
    }

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            onIncrementButtonClicked(1);
        } else {
            onIncrementButtonClicked(-1);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static MutableComponent getTrans(String key, Object... args) {
        return Component.translatable(LaserIO.MODID + "." + key, args);
    }
}
