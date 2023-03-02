package com.direwolf20.laserio.client.screens;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;
import java.awt.Color;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.client.renderer.LaserIOItemRenderer;
import com.direwolf20.laserio.client.screens.widgets.ChannelButton;
import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.common.containers.OverclockerChannelContainer;
import com.direwolf20.laserio.common.items.upgrades.OverclockerChannel;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketUpdateOverclockerChannel;
import com.direwolf20.laserio.util.MiscTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class OverclockerChannelScreen extends AbstractContainerScreen<OverclockerChannelContainer>{
	private final ResourceLocation GUI = new ResourceLocation(LaserIO.MODID, "textures/gui/channeledit.png");
	private final int WIDTH = 77;
	private final int HEIGHT = 74;
	private int rel_X = 0;
	private int rel_Y = 0;
	public ItemStack overclocker;
	
	public OverclockerChannelContainer container;
	protected ChannelButton[] buttons = new ChannelButton[16];
	protected boolean[] buttonActive = new boolean[16];
	
	public OverclockerChannelScreen(OverclockerChannelContainer container, Inventory inv, Component component) {
		super(container, inv, component);
		this.container = container;
		this.overclocker = container.overclockerItem;
	}

	@Override
	public void render(PoseStack matrixStack, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, pMouseX, pMouseY, pPartialTick);
		this.renderTooltip(matrixStack, pMouseX, pMouseY);
		renderDeactivation(matrixStack);
		for(int id = 0; id < 16; id++)
		if (MiscTools.inBounds(buttons[id].x, buttons[id].y, buttons[id].getWidth(), buttons[id].getHeight()-1, pMouseX, pMouseY)) {
	       this.renderTooltip(matrixStack, Component.translatable("screen.laserio.channel").append(tooltipMaker("+" + String.valueOf(id*16), LaserNodeBERender.colors[id].getRGB())), pMouseX, pMouseY);
	    }
	}
	
	public void renderDeactivation(PoseStack matrixStack) {
		int overlayColor = new Color(200,200,200,128).getRGB();
		for(int id = 0; id < 16; id++)
		if(!buttonActive[id])
			fill(matrixStack, buttons[id].x, buttons[id].y, buttons[id].x + 16, buttons[id].y + 16, overlayColor);
	}
	
	@Override
	protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
		 RenderSystem.setShaderTexture(0, GUI);
	     this.rel_X = (this.width - WIDTH) / 2;
	     this.rel_Y = (this.height - HEIGHT) / 2;
	     this.blit(matrixStack, rel_X, rel_Y, 0, 0, WIDTH, HEIGHT);
	}

	@Override
	protected void init() {
		super.init();
		BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
		this.itemRenderer = new LaserIOItemRenderer(minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer);
		
		this.rel_X = (this.width - 76) / 2;
	    this.rel_Y = (this.height - 72) / 2;
		
		addChannelButtons();
		
		for(ChannelButton cB : buttons) {
			addRenderableWidget(cB);
		}
		
		if(OverclockerChannel.isChannelVisible(overclocker))
			buttonActive[(int)OverclockerChannel.getChannel(overclocker)] = true;
	}
	
	public void addChannelButtons(){
		int channel = 0;
		for(int y = 0; y < 16*4; y += 16) {
			for(int x = 0; x < 17*4; x += 17) {
				final byte buttonVal = (byte)channel;
				buttons[channel] = new ChannelButton(rel_X + 4 + x, rel_Y + 4 + y, 16, 16, channel, (button) -> {
					OverclockerChannel.setChannel(overclocker, buttonVal);
				});
				buttonActive[channel] = false;
				channel++;
			}
		}
	}
	
	public void setChannel(byte channel, boolean visible){
		for(int id = 0; id < 16; id++) {
			buttonActive[id] = (channel == id);
		}
		ItemStack usedItem = container.playerEntity.getMainHandItem().getItem() instanceof OverclockerChannel ?
				container.playerEntity.getMainHandItem() : container.playerEntity.getOffhandItem();
		OverclockerChannel.setChannel(usedItem, channel);
		OverclockerChannel.setChannelVisible(usedItem, visible);
		PacketHandler.sendToServer(new PacketUpdateOverclockerChannel(channel, visible));
		
		//System.out.println("Client: " + OverclockerChannel.getChannel(container.playerEntity.getItemInHand(InteractionHand.MAIN_HAND)));
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int btn) {
		for(int id = 0; id < 16; id++) {
			ChannelButton button = buttons[id];
			if(MiscTools.inBounds(button.x, button.y, button.getWidth(), button.getHeight(), x, y)) {
				if(buttonActive[id]) {
					setChannel((byte) 0, false);
					buttonActive[0] = false;
				} else {
					setChannel((byte) id, true);
				}
				button.playDownSound(Minecraft.getInstance().getSoundManager());
			}
		}
		return super.mouseClicked(x, y, btn);
	}
	
	 @Override
	 public boolean isPauseScreen() {
	    return false;
	 }
	 
	 @Override
	 protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}
}
