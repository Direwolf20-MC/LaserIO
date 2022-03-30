package com.direwolf20.laserio.client.events;

import com.direwolf20.laserio.client.renderer.BlockOverlayRender;
import com.direwolf20.laserio.common.items.LaserWrench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ClientEvents {
    @SubscribeEvent
    static void renderWorldLastEvent(RenderLevelLastEvent evt) {
        Player myplayer = Minecraft.getInstance().player;

        ItemStack myItem = getWrench(myplayer);
        if (myItem.getItem() instanceof LaserWrench) {
            BlockPos selectedPos = LaserWrench.getConnectionPos(myItem);
            if (!selectedPos.equals(BlockPos.ZERO))
                BlockOverlayRender.renderSelectedBlock(evt, selectedPos);
        }
    }

    public static ItemStack getWrench(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof LaserWrench)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof LaserWrench)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }
}
