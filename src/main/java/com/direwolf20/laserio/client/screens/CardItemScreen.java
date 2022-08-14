package com.direwolf20.laserio.client.screens;

import com.direwolf20.laserio.client.renderer.LaserIOItemRenderer;
import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.client.screens.widgets.NumberButton;
import com.direwolf20.laserio.client.screens.widgets.TextButton;
import com.direwolf20.laserio.client.screens.widgets.ToggleButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.customslot.CardItemSlot;
import com.direwolf20.laserio.common.containers.customslot.CardOverclockSlot;
import com.direwolf20.laserio.common.containers.customslot.FilterBasicSlot;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.items.filters.*;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.*;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.Map;

import java.awt.*;

public class CardItemScreen extends AbstractCardScreen<CardItemContainer> {
    private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/itemcard.png");

    protected final CardItemContainer container;
    protected byte currentMode;
    protected byte currentChannel;
    protected byte currentRedstoneChannel;
    protected byte currentItemExtractAmt;
    protected short currentPriority;
    protected byte currentSneaky;
    protected int currentTicks;
    protected boolean currentExact;
    protected int currentRoundRobin;
    protected boolean currentRegulate;
    protected boolean currentAndMode;
    protected int isAllowList = -1;
    protected int isCompareNBT = -1;
    protected boolean showFilter;
    protected boolean showAllow;
    protected boolean showNBT;
    public ItemStack filter;
    protected Map<String, Button> buttons = new HashMap<>();
    protected byte currentRedstoneMode;

    protected final String[] sneakyNames = {
            "screen.laserio.default",
            "screen.laserio.down",
            "screen.laserio.up",
            "screen.laserio.north",
            "screen.laserio.south",
            "screen.laserio.west",
            "screen.laserio.east",
    };

    public CardItemScreen(CardItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        filter = container.slots.get(0).getItem();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        toggleFilterSlots();
        if (showFilter)
            updateItemCounts();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        Button modeButton = buttons.get("mode");
        if (MiscTools.inBounds(modeButton.x, modeButton.y, modeButton.getWidth(), modeButton.getHeight(), mouseX, mouseY)) {
            TranslatableComponent translatableComponents[] = new TranslatableComponent[4];
            translatableComponents[0] = new TranslatableComponent("screen.laserio.insert");
            translatableComponents[1] = new TranslatableComponent("screen.laserio.extract");
            translatableComponents[2] = new TranslatableComponent("screen.laserio.stock");
            translatableComponents[3] = new TranslatableComponent("screen.laserio.sensor");
            this.renderTooltip(matrixStack, translatableComponents[currentMode], mouseX, mouseY);
        }
        Button channelButton = buttons.get("channel");
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.channel").append(String.valueOf(currentChannel)), mouseX, mouseY);
        }
        Button redstoneChannelButton = buttons.get("redstoneChannel");
        if (MiscTools.inBounds(redstoneChannelButton.x, redstoneChannelButton.y, redstoneChannelButton.getWidth(), redstoneChannelButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.redstonechannel").append(String.valueOf(currentRedstoneChannel)), mouseX, mouseY);
        }
        Button sneakyButton = buttons.get("sneaky");
        if (MiscTools.inBounds(sneakyButton.x, sneakyButton.y, sneakyButton.getWidth(), sneakyButton.getHeight(), mouseX, mouseY)) {
            this.renderTooltip(matrixStack, new TranslatableComponent(String.valueOf(sneakyNames[currentSneaky + 1])), mouseX, mouseY);
        }
        Button amountButton = buttons.get("amount");
        if (MiscTools.inBounds(amountButton.x, amountButton.y, amountButton.getWidth(), amountButton.getHeight(), mouseX, mouseY)) {
            if (showExtractAmt()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.extractamt"), mouseX, mouseY);
            }
            if (showPriority()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.priority"), mouseX, mouseY);
            }
        }
        Button regulate = buttons.get("regulate");
        if (MiscTools.inBounds(regulate.x, regulate.y, regulate.getWidth(), regulate.getHeight(), mouseX, mouseY)) {
            if (showRegulate()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.regulate"), mouseX, mouseY);
            }
        }
        Button roundrobin = buttons.get("roundrobin");
        if (MiscTools.inBounds(roundrobin.x, roundrobin.y, roundrobin.getWidth(), roundrobin.getHeight(), mouseX, mouseY)) {
            if (showRoundRobin()) {
                TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
                translatableComponents[0] = new TranslatableComponent("screen.laserio.false");
                translatableComponents[1] = new TranslatableComponent("screen.laserio.true");
                translatableComponents[2] = new TranslatableComponent("screen.laserio.enforced");
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.roundrobin").append(translatableComponents[currentRoundRobin]), mouseX, mouseY);
            }
        }
        Button redstoneMode = buttons.get("redstoneMode");
        if (MiscTools.inBounds(redstoneMode.x, redstoneMode.y, redstoneMode.getWidth(), redstoneMode.getHeight(), mouseX, mouseY)) {
            TranslatableComponent translatableComponents[] = new TranslatableComponent[3];
            translatableComponents[0] = new TranslatableComponent("screen.laserio.ignored");
            translatableComponents[1] = new TranslatableComponent("screen.laserio.low");
            translatableComponents[2] = new TranslatableComponent("screen.laserio.high");
            this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.redstoneMode").append(translatableComponents[currentRedstoneMode]), mouseX, mouseY);
        }
        Button exact = buttons.get("exact");
        if (MiscTools.inBounds(exact.x, exact.y, exact.getWidth(), exact.getHeight(), mouseX, mouseY)) {
            if (showExactAmt()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.exact"), mouseX, mouseY);
            }
        }
        Button speedButton = buttons.get("speed");
        if (MiscTools.inBounds(speedButton.x, speedButton.y, speedButton.getWidth(), speedButton.getHeight(), mouseX, mouseY)) {
            if (!showPriority()) {
                this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.tickSpeed"), mouseX, mouseY);
            }
        }
        if (showAllow) {
            Button allowList = buttons.get("allowList");
            if (MiscTools.inBounds(allowList.x, allowList.y, allowList.getWidth(), allowList.getHeight(), mouseX, mouseY)) {
                if (isAllowList == 1)
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.allowlist"), mouseX, mouseY);
                else
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.denylist"), mouseX, mouseY);
            }
        }
        if (showNBT) {
            Button nbtButton = buttons.get("nbt");
            if (MiscTools.inBounds(nbtButton.x, nbtButton.y, nbtButton.getWidth(), nbtButton.getHeight(), mouseX, mouseY)) {
                if (isCompareNBT == 1)
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.nbttrue"), mouseX, mouseY);
                else
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.nbtfalse"), mouseX, mouseY);
            }
        }
        if (BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.SENSOR) {
            Button andButton = buttons.get("and");
            if (MiscTools.inBounds(andButton.x, andButton.y, andButton.getWidth(), andButton.getHeight(), mouseX, mouseY)) {
                if (currentAndMode)
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.and"), mouseX, mouseY);
                else
                    this.renderTooltip(matrixStack, new TranslatableComponent("screen.laserio.or"), mouseX, mouseY);
            }
        }
    }

    public void updateItemCounts() {
        IItemHandler handler = container.filterHandler;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            stack.setCount(container.getStackSize(i + container.SLOTS));
        }
    }

    public void addAmtButton() {
        buttons.put("amount", new NumberButton(getGuiLeft() + 147, getGuiTop() + 25, 24, 12, currentMode == 0 ? currentPriority : currentItemExtractAmt, (button) -> {
            changeAmount(-1);
        }));
    }

    public void addModeButton() {
        ResourceLocation[] modeTextures = new ResourceLocation[4];
        modeTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeinserter.png");
        modeTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modeextractor.png");
        modeTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modestocker.png");
        modeTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/modesensor.png");
        buttons.put("mode", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 5, 16, 16, modeTextures, currentMode, (button) -> {
            currentMode = BaseCard.nextTransferMode(card);
            ((ToggleButton) button).setTexturePosition(currentMode);
            ((NumberButton) buttons.get("amount")).setValue(currentMode == 0 ? currentPriority : currentItemExtractAmt);
            modeChange();
        }));
    }

    public void addRedstoneButton() {
        ResourceLocation[] redstoneTextures = new ResourceLocation[3];
        redstoneTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstoneignore.png");
        redstoneTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonelow.png");
        redstoneTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/redstonehigh.png");
        buttons.put("redstoneMode", new ToggleButton(getGuiLeft() + 105, getGuiTop() + 5, 16, 16, redstoneTextures, currentRedstoneMode, (button) -> {
            currentRedstoneMode = (byte) (currentRedstoneMode == 2 ? 0 : currentRedstoneMode + 1);
            ((ToggleButton) button).setTexturePosition(currentRedstoneMode);
        }));
    }

    public void addRedstoneChannelButton() {
        buttons.put("redstoneChannel", new ChannelButton(getGuiLeft() + 125, getGuiTop() + 5, 16, 16, currentRedstoneChannel, (button) -> {
            currentRedstoneChannel = CardRedstone.nextRedstoneChannel(card);
            ((ChannelButton) button).setChannel(currentRedstoneChannel);
        }));
    }

    @Override
    public void init() {
        super.init();
        Minecraft minecraft = Minecraft.getInstance();
        BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        this.itemRenderer = new LaserIOItemRenderer(minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer);
        currentMode = BaseCard.getTransferMode(card);
        currentChannel = BaseCard.getChannel(card);
        currentItemExtractAmt = CardItem.getItemExtractAmt(card);
        currentPriority = BaseCard.getPriority(card);
        currentSneaky = BaseCard.getSneaky(card);
        currentTicks = BaseCard.getExtractSpeed(card);
        currentExact = BaseCard.getExact(card);
        currentRoundRobin = BaseCard.getRoundRobin(card);
        currentRegulate = BaseCard.getRegulate(card);
        currentRedstoneMode = BaseCard.getRedstoneMode(card);
        currentRedstoneChannel = BaseCard.getRedstoneChannel(card);
        currentAndMode = BaseCard.getAnd(card);

        showFilter = !(filter == null) && !filter.isEmpty() && !(filter.getItem() instanceof FilterTag);
        if (showFilter) {
            isAllowList = BaseFilter.getAllowList(filter) ? 1 : 0;
            isCompareNBT = BaseFilter.getCompareNBT(filter) ? 1 : 0;
            if (filter.getItem() instanceof FilterMod) {
                showAllow = true;
                showNBT = false;
            } else if (filter.getItem() instanceof FilterBasic) {
                showAllow = true;
                showNBT = true;
            } else if (filter.getItem() instanceof FilterCount) {
                showAllow = false;
                showNBT = true;
            }
            if (BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.SENSOR)
                showAllow = false;
        } else {
            isAllowList = -1;
            isCompareNBT = -1;
        }

        ResourceLocation[] allowListTextures = new ResourceLocation[2];
        allowListTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlistfalse.png");
        allowListTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/allowlisttrue.png");

        buttons.put("allowList", new ToggleButton(getGuiLeft() + 135, getGuiTop() + 61, 16, 16, allowListTextures, isAllowList == 1 ? 1 : 0, (button) -> {
            isAllowList = isAllowList == 1 ? 0 : 1;
            ((ToggleButton) button).setTexturePosition(isAllowList == 1 ? 1 : 0);
        }));

        ResourceLocation[] nbtTextures = new ResourceLocation[2];
        nbtTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbtfalse.png");
        nbtTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/matchnbttrue.png");

        buttons.put("nbt", new ToggleButton(getGuiLeft() + 153, getGuiTop() + 61, 16, 16, nbtTextures, isCompareNBT == 1 ? 1 : 0, (button) -> {
            isCompareNBT = isCompareNBT == 1 ? 0 : 1;
            ((ToggleButton) button).setTexturePosition(isCompareNBT == 1 ? 1 : 0);
        }));

        addAmtButton();

        buttons.put("speed", new NumberButton(getGuiLeft() + 147, getGuiTop() + 39, 24, 12, currentTicks, (button) -> {
            changeTick(-1);
        }));

        ResourceLocation[] exactTextures = new ResourceLocation[2];
        exactTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exactfalse.png");
        exactTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/exacttrue.png");
        buttons.put("exact", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 25, 16, 16, exactTextures, currentExact ? 1 : 0, (button) -> {
            currentExact = !currentExact;
            ((ToggleButton) button).setTexturePosition(currentExact ? 1 : 0);
        }));

        ResourceLocation[] roundRobinTextures = new ResourceLocation[3];
        roundRobinTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinfalse.png");
        roundRobinTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobintrue.png");
        roundRobinTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/roundrobinenforced.png");
        buttons.put("roundrobin", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, roundRobinTextures, currentRoundRobin, (button) -> {
            currentRoundRobin = currentRoundRobin == 2 ? 0 : currentRoundRobin + 1;
            ((ToggleButton) button).setTexturePosition(currentRoundRobin);
        }));

        ResourceLocation[] regulateTextures = new ResourceLocation[2];
        regulateTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatefalse.png");
        regulateTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/regulatetrue.png");
        buttons.put("regulate", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, regulateTextures, currentRegulate ? 1 : 0, (button) -> {
            currentRegulate = !currentRegulate;
            ((ToggleButton) button).setTexturePosition(currentRegulate ? 1 : 0);
        }));

        ResourceLocation[] andTextures = new ResourceLocation[2];
        andTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/or.png");
        andTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/and.png");
        buttons.put("and", new ToggleButton(getGuiLeft() + 5, getGuiTop() + 25, 16, 16, andTextures, currentAndMode ? 1 : 0, (button) -> {
            currentAndMode = !currentAndMode;
            ((ToggleButton) button).setTexturePosition(currentAndMode ? 1 : 0);
        }));

        addModeButton();
        addRedstoneButton();
        addRedstoneChannelButton();

        buttons.put("channel", new ChannelButton(getGuiLeft() + 5, getGuiTop() + 65, 16, 16, currentChannel, (button) -> {
            currentChannel = BaseCard.nextChannel(card);
            ((ChannelButton) button).setChannel(currentChannel);
        }));

        ResourceLocation[] sneakyTextures = new ResourceLocation[7];
        sneakyTextures[0] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky.png");
        sneakyTextures[1] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-down.png");
        sneakyTextures[2] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-up.png");
        sneakyTextures[3] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-north.png");
        sneakyTextures[4] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-south.png");
        sneakyTextures[5] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-west.png");
        sneakyTextures[6] = new ResourceLocation(LaserIO.MODID, "textures/gui/buttons/sneaky-east.png");
        buttons.put("sneaky", new ToggleButton(getGuiLeft() + 25, getGuiTop() + 5, 16, 16, sneakyTextures, currentSneaky + 1, (button) -> {
            currentSneaky = BaseCard.nextSneaky(card);
            ((ToggleButton) button).setTexturePosition(currentSneaky + 1);
        }));

        if (container.direction != -1) {
            buttons.put("return", new Button(getGuiLeft() - 25, getGuiTop() + 1, 25, 20, new TextComponent("<--"), (button) -> {
                openNode();
            }));
        }

        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            addRenderableWidget(button.getValue());
        }
        if (!showNBT) removeWidget(buttons.get("nbt"));
        if (!showAllow) removeWidget(buttons.get("allowList"));


        if (card.getCount() > 1) {
            for (int i = 0; i < container.SLOTS; i++) {
                if (i >= container.slots.size()) continue;
                Slot slot = container.getSlot(i);
                if (slot instanceof CardItemSlot cardItemSlot)
                    cardItemSlot.setEnabled(false);
                if (slot instanceof CardOverclockSlot cardOverclockSlot)
                    cardOverclockSlot.setEnabled(false);
            }
        }

        modeChange();
        /*if (currentMode == 0) removeWidget(buttons.get("speed"));
        if (currentMode == 0) removeWidget();
        if (currentMode == 0 || currentMode == 1) removeWidget(buttons.get("roundrobin"));
        if (currentMode == 0 || currentMode == 2) removeWidget(buttons.get("regulate"));*/
    }

    public void modeChange() {
        Button speedButton = buttons.get("speed");
        Button exactButton = buttons.get("exact");
        Button rrButton = buttons.get("roundrobin");
        Button regulateButton = buttons.get("regulate");
        Button channelButton = buttons.get("channel");
        Button amountButton = buttons.get("amount");
        Button andButton = buttons.get("and");
        Button redstoneModeButton = buttons.get("redstoneMode");
        if (currentMode == 0) { //insert
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            if (!renderables.contains(redstoneModeButton))
                addRenderableWidget(redstoneModeButton);
            removeWidget(speedButton);
            removeWidget(exactButton);
            removeWidget(rrButton);
            removeWidget(regulateButton);
            removeWidget(andButton);
        } else if (currentMode == 1) { //extract
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(rrButton))
                addRenderableWidget(rrButton);
            if (!renderables.contains(redstoneModeButton))
                addRenderableWidget(redstoneModeButton);
            removeWidget(regulateButton);
            removeWidget(andButton);
        } else if (currentMode == 2) { //stock
            if (!renderables.contains(channelButton))
                addRenderableWidget(channelButton);
            if (!renderables.contains(amountButton))
                addRenderableWidget(amountButton);
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(exactButton))
                addRenderableWidget(exactButton);
            if (!renderables.contains(regulateButton))
                addRenderableWidget(regulateButton);
            if (!renderables.contains(redstoneModeButton))
                addRenderableWidget(redstoneModeButton);
            removeWidget(rrButton);
            removeWidget(andButton);
        } else if (currentMode == 3) { //sensor
            if (!renderables.contains(speedButton))
                addRenderableWidget(speedButton);
            if (!renderables.contains(andButton))
                addRenderableWidget(andButton);
            removeWidget(rrButton);
            removeWidget(regulateButton);
            removeWidget(channelButton);
            removeWidget(amountButton);
            removeWidget(redstoneModeButton);
            if (filter.getItem() instanceof FilterCount) {
                if (!renderables.contains(exactButton))
                    addRenderableWidget(exactButton);
            } else {
                removeWidget(exactButton);
            }
        }
    }

    public void changeAmount(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 64;
        if (change < 0) {
            if (currentMode == 0) {
                currentPriority = (short) (Math.max(currentPriority + change, -4096));
            } else {
                currentItemExtractAmt = (byte) (Math.max(currentItemExtractAmt + change, 1));
            }
        } else {
            if (currentMode == 0) {
                currentPriority = (short) (Math.min(currentPriority + change, 4096));
            } else {
                currentItemExtractAmt = (byte) (Math.min(currentItemExtractAmt + change, Math.max(container.getSlot(1).getItem().getCount() * 16, 8)));
            }
        }
    }

    public void changeTick(int change) {
        if (Screen.hasShiftDown()) change *= 10;
        if (Screen.hasControlDown()) change *= 64;
        if (change < 0) {
            currentTicks = (Math.max(currentTicks + change, Math.max(20 - container.getSlot(1).getItem().getCount() * 5, 1)));
        } else {
            currentTicks = (Math.min(currentTicks + change, 1200));//Math.max(container.getSlot(1).getItem().getCount() * 16, 1)));
        }
    }

    public void toggleFilterSlots() {
        filter = container.slots.get(0).getItem();
        showFilter = !filter.isEmpty() && !(filter.getItem() instanceof FilterTag);
        Button exactButton = buttons.get("exact");
        if (showFilter) { //If the filter isn't empty, and the allowList is set to -1, it means we don't have a real value for allow list yet so get it
            if (filter.getItem() instanceof FilterMod) {
                showNBT = false;
                if (currentMode == 2) {
                    showAllow = true;
                    //removeWidget(buttons.get("allowList"));
                } else {
                    showAllow = true;
                    if (!renderables.contains(buttons.get("allowList"))) addRenderableWidget(buttons.get("allowList"));
                }
            } else if (filter.getItem() instanceof FilterBasic) {
                showNBT = true;
                if (currentMode == 2) {
                    showAllow = true;
                    //removeWidget(buttons.get("allowList"));
                } else {
                    showAllow = true;
                    if (!renderables.contains(buttons.get("allowList"))) addRenderableWidget(buttons.get("allowList"));
                }
            } else if (filter.getItem() instanceof FilterCount) {
                showAllow = false;
                showNBT = true;
                removeWidget(buttons.get("allowList"));
            }
            if (BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.SENSOR) {
                showAllow = false;
                removeWidget(buttons.get("allowList"));
                if (filter.getItem() instanceof FilterCount) {
                    if (!renderables.contains(exactButton))
                        addRenderableWidget(exactButton);
                } else {
                    removeWidget(exactButton);
                }
            }
            if (isAllowList == -1) {
                isAllowList = BaseFilter.getAllowList(filter) ? 1 : 0;
                ((ToggleButton) buttons.get("allowList")).setTexturePosition(isAllowList == 1 ? 1 : 0);
                isCompareNBT = BaseFilter.getCompareNBT(filter) ? 1 : 0;
                ((ToggleButton) buttons.get("nbt")).setTexturePosition(isCompareNBT == 1 ? 1 : 0);
                if (showAllow) addRenderableWidget(buttons.get("allowList"));
                if (showNBT) addRenderableWidget(buttons.get("nbt"));
            }
        } else {
            isAllowList = -1;
            isCompareNBT = -1;
            removeWidget(buttons.get("allowList"));
            removeWidget(buttons.get("nbt"));
            showAllow = false;
            showNBT = false;
            if (BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.SENSOR) {
                removeWidget(exactButton);
            }
        }
        for (int i = container.SLOTS; i < container.SLOTS + container.FILTERSLOTS; i++) {
            if (i >= container.slots.size()) continue;
            Slot slot = container.getSlot(i);
            if (!(slot instanceof FilterBasicSlot)) continue;
            ((FilterBasicSlot) slot).setEnabled(showFilter);
        }
    }

    private boolean showExtractAmt() {
        return card.getItem() instanceof BaseCard && ((BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT) && (BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.SENSOR));
    }

    private boolean showExactAmt() {
        if (BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.SENSOR)
            return filter.getItem() instanceof FilterCount;
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) != BaseCard.TransferMode.INSERT;
    }

    private boolean showPriority() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.INSERT;
    }

    private boolean showRegulate() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.STOCK;
    }

    private boolean showRoundRobin() {
        return card.getItem() instanceof BaseCard && BaseCard.getNamedTransferMode(card) == BaseCard.TransferMode.EXTRACT;
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        /*stack.pushPose();
        stack.scale(0.5f, 0.5f, 0.5f);
        if (showExtractAmt()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.extractamt").getString() + ":", 5*2, 45*2, Color.DARK_GRAY.getRGB());
        }
        if (showPriority()) {
            font.draw(stack, new TranslatableComponent("screen.laserio.priority").getString() + ":", 5*2, 50*2, Color.DARK_GRAY.getRGB());
        }
        stack.popPose();*/
        //super.renderLabels(matrixStack, x, y);
    }

    @Override
    public Color cardColor() {
        return ItemCardColor;
    }

    @Override
    public Component cardTypeName(){
        return new TranslatableComponent("item.laserio.card_item");
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        filter = container.slots.get(0).getItem();
        if (showFilter) {
            int slotsWidth = 90;
            int slotsHeight = 54;
            relX = relX + 43;
            relY = relY + 24;
            blit(matrixStack, relX, relY, 0, 167, slotsWidth, slotsHeight);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (showFilter)
            PacketHandler.sendToServer(new PacketUpdateFilter(isAllowList == 1, isCompareNBT == 1));
        saveSettings();
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


    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static TranslatableComponent getTrans(String key, Object... args) {
        return new TranslatableComponent(LaserIO.MODID + "." + key, args);
    }

    public void setExtract(NumberButton amountButton, int btn) {
        if (btn == 0)
            changeAmount(1);
        else if (btn == 1)
            changeAmount(-1);
        amountButton.setValue(currentMode == 0 ? currentPriority : currentItemExtractAmt);
        amountButton.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void saveSettings() {
        PacketHandler.sendToServer(new PacketUpdateCard(currentMode, currentChannel, currentItemExtractAmt, currentPriority, currentSneaky, (short) currentTicks, currentExact, currentRegulate, (byte) currentRoundRobin, 0, 0, currentRedstoneMode, currentRedstoneChannel, currentAndMode));
    }

    public boolean filterSlot(int btn) {
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


        PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, slotStack, slotStack.getCount() + amt));
        return true;
    }

    public void openNode() {
        saveSettings();
        PacketHandler.sendToServer(new PacketOpenNode(container.sourceContainer, container.direction));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        ChannelButton channelButton = ((ChannelButton) buttons.get("channel"));
        if (MiscTools.inBounds(channelButton.x, channelButton.y, channelButton.getWidth(), channelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentChannel = BaseCard.nextChannel(card);
            else if (btn == 1)
                currentChannel = BaseCard.previousChannel(card);
            channelButton.setChannel(currentChannel);
            channelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        ChannelButton redstoneChannelButton = ((ChannelButton) buttons.get("redstoneChannel"));
        if (MiscTools.inBounds(redstoneChannelButton.x, redstoneChannelButton.y, redstoneChannelButton.getWidth(), redstoneChannelButton.getHeight(), x, y)) {
            if (btn == 0)
                currentRedstoneChannel = BaseCard.nextRedstoneChannel(card);
            else if (btn == 1)
                currentRedstoneChannel = BaseCard.previousRedstoneChannel(card);
            redstoneChannelButton.setChannel(currentRedstoneChannel);
            redstoneChannelButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        ToggleButton sneakyButton = ((ToggleButton) buttons.get("sneaky"));
        if (MiscTools.inBounds(sneakyButton.x, sneakyButton.y, sneakyButton.getWidth(), sneakyButton.getHeight(), x, y)) {
            if (btn == 0) {
                currentSneaky = BaseCard.nextSneaky(card);
                sneakyButton.setTexturePosition(currentSneaky + 1);
            } else if (btn == 1) {
                currentSneaky = BaseCard.previousSneaky(card);
                sneakyButton.setTexturePosition(currentSneaky + 1);
            }
            sneakyButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        NumberButton amountButton = ((NumberButton) buttons.get("amount"));
        if (MiscTools.inBounds(amountButton.x, amountButton.y, amountButton.getWidth(), amountButton.getHeight(), x, y)) {
            setExtract(amountButton, btn);
            return true;
        }

        NumberButton speedButton = ((NumberButton) buttons.get("speed"));
        if (MiscTools.inBounds(speedButton.x, speedButton.y, speedButton.getWidth(), speedButton.getHeight(), x, y)) {
            if (btn == 0)
                changeTick(1);
            else if (btn == 1)
                changeTick(-1);
            speedButton.setValue(currentTicks);
            speedButton.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        if (hoveredSlot == null)
            return super.mouseClicked(x, y, btn);

        if (hoveredSlot instanceof FilterBasicSlot) {
            if (filter.getItem() instanceof FilterBasic) {
                // By splitting the stack we can get air easily :) perfect removal basically
                ItemStack stack = this.menu.getCarried();// getMinecraft().player.inventoryMenu.getCarried();
                stack = stack.copy().split(hoveredSlot.getMaxStackSize()); // Limit to slot limit
                if (ItemHandlerHelper.canItemStacksStack(stack, container.cardItem)) return true;
                hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
                PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, stack, stack.getCount()));
            } else if (filter.getItem() instanceof FilterCount) {
                ItemStack stack = this.menu.getCarried();// getMinecraft().player.inventoryMenu.getCarried();
                if (!stack.isEmpty()) {
                    stack = stack.copy();
                    if (ItemHandlerHelper.canItemStacksStack(stack, container.cardItem)) return true;
                    hoveredSlot.set(stack); // Temporarily update the client for continuity purposes
                    PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, stack, stack.getCount()));
                } else {
                    filterSlot(btn);
                    /*ItemStack slotStack = hoveredSlot.getItem();
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
                    //slotStack.grow(amt);

                    PacketHandler.sendToServer(new PacketGhostSlot(hoveredSlot.index, slotStack, slotStack.getCount() + amt));*/
                }
            }
            return true;
        }
        if (hoveredSlot instanceof CardItemSlot) { //Right click
            if (btn == 0) {
                if (filter.getItem() instanceof BaseFilter && !(filter.getItem() instanceof FilterTag)) //Save the filter before removing it from the slot
                    PacketHandler.sendToServer(new PacketUpdateFilter(isAllowList == 1, isCompareNBT == 1));
            } else if (btn == 1) {
                int slot = hoveredSlot.getSlotIndex();
                saveSettings();
                PacketHandler.sendToServer(new PacketOpenFilter(slot));
                return true;
            }
        }
        return super.mouseClicked(x, y, btn);
    }
}
