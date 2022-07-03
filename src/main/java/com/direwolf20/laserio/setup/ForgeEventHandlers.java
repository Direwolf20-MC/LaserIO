package com.direwolf20.laserio.setup;

import com.direwolf20.laserio.common.blocks.baseblocks.BaseLaserBlock;
import com.direwolf20.laserio.common.items.LaserWrench;
import com.direwolf20.laserio.util.VectorHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.direwolf20.laserio.util.VectorHelper.RANGE;
import static net.minecraft.world.InteractionHand.OFF_HAND;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getWorld();
        Player player = event.getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        if (event.getHand().equals(OFF_HAND)) {
            boolean hasItemInOffhand = !player.getOffhandItem().isEmpty();
            boolean isWrenchHeld = !heldItem.isEmpty() && heldItem.getItem() instanceof LaserWrench;
            boolean isLookingAtBaseLaserBlock = world.getBlockState(VectorHelper.getLookingAt(player, RANGE).getBlockPos()).getBlock() instanceof BaseLaserBlock;

            if (hasItemInOffhand && isWrenchHeld && isLookingAtBaseLaserBlock) {
                event.setCanceled(true);
            }
        }
    }

}
