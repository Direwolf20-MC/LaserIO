package com.direwolf20.laserio.common.items.upgrades;

import com.direwolf20.laserio.setup.ModSetup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
    	System.out.println(getChannel(pPlayer.getItemInHand(pUsedHand)));
    	return super.use(pLevel, pPlayer, pUsedHand);
    }
}
