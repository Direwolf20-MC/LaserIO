package com.direwolf20.laserio.common.items.upgrades;

import static com.direwolf20.laserio.util.MiscTools.tooltipMaker;

import java.util.List;

import javax.annotation.Nullable;

import com.direwolf20.laserio.client.blockentityrenders.LaserNodeBERender;
import com.direwolf20.laserio.common.containers.OverclockerChannelContainer;
import com.direwolf20.laserio.setup.ModSetup;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class OverclockerChannel extends Item{
	public OverclockerChannel() {
		super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
	}
	
    public static byte setChannel(ItemStack item, byte channel) {
        if (channel == 0)
            item.removeTagKey("channel");
        else
            item.getOrCreateTag().putByte("channel", channel);
        return channel;
    }

    public static byte getChannel(ItemStack item) {
        CompoundTag compound = item.getTag();
        if (compound == null || !compound.contains("channel")) return (byte) 0;
        return compound.getByte("channel");
    }
    
    public static boolean isChannelVisible(ItemStack item){
    	CompoundTag compound = item.getTag();
        if (compound == null || !compound.contains("show_channel")) return false;
        return compound.getBoolean("show_channel");
    }
    
    public static boolean setChannelVisible(ItemStack item, boolean visible) {
        if (!visible)
            item.removeTagKey("show_channel");
        else
            item.getOrCreateTag().putBoolean("show_channel", visible);
        return visible;
    }
    
    
    public static int getChannelOffset(ItemStack item){
    	return getChannel(item) * 16;
    }

    public static byte nextChannel(ItemStack item) {
        byte k = getChannel(item);
        return setChannel(item, (byte) (k == 15 ? 0 : k + 1));
    }

    public static byte previousChannel(ItemStack item) {
        byte k = getChannel(item);
        return setChannel(item, (byte) (k == 0 ? 15 : k - 1));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    	ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide()) return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);

        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                (windowId, playerInventory, playerEntity) -> new OverclockerChannelContainer(windowId, playerInventory, player, itemstack), Component.translatable("")), (buf -> {
            buf.writeItem(itemstack);
        }));
        
        //System.out.println(itemstack.getItem().getRegistryName()+""+itemstack.getTag());
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        Minecraft mc = Minecraft.getInstance();

        if (world == null || mc.player == null) {
            return;
        }
         
        if(!isChannelVisible(stack)) return;
       
        boolean sneakPressed = Screen.hasShiftDown();

        if (!sneakPressed) {
            tooltip.add(Component.translatable("laserio.tooltip.item.show_settings")
                    .withStyle(ChatFormatting.GRAY));
        } else {

            MutableComponent toWrite = tooltipMaker("laserio.tooltip.item.card.channel", ChatFormatting.GRAY.getColor());
            
            toWrite.append(tooltipMaker("+" + String.valueOf(getChannelOffset(stack)), LaserNodeBERender.colors[getChannel(stack)].getRGB()));
            tooltip.add(toWrite);

        }
        
    }
}
