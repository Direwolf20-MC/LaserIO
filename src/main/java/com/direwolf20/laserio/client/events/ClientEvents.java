package com.direwolf20.laserio.client.events;

import static com.direwolf20.laserio.client.events.RenderGUIOverlay.renderLocation;

import com.direwolf20.laserio.client.renderer.BlockOverlayRender;
import com.direwolf20.laserio.client.renderer.DelayedRenderer;
import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.blocks.LaserConnectorAdv;
import com.direwolf20.laserio.common.items.LaserWrench;
import com.direwolf20.laserio.util.DimBlockPos;
import com.direwolf20.laserio.util.VectorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    static void renderWorldLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Player myplayer = Minecraft.getInstance().player;

        ItemStack myItem = getWrench(myplayer);
        if (myItem.getItem() instanceof LaserWrench) {
            DimBlockPos selectedDimPos = LaserWrench.getConnectionPos(myItem, myplayer.level());
            if (selectedDimPos != null && myplayer.level().dimension().equals(selectedDimPos.levelKey)) {
                BlockEntity be = myplayer.level().getBlockEntity(selectedDimPos.blockPos);
                if (!selectedDimPos.blockPos.equals(BlockPos.ZERO) && (be instanceof BaseLaserBE))
                    BlockOverlayRender.renderSelectedBlock(evt, selectedDimPos.blockPos, (BaseLaserBE) be);
            }
        }

        //DelayedRenderer Renders
        DelayedRenderer.render(evt.getPoseStack());
        DelayedRenderer.renderConnections(evt.getPoseStack());
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

    @SubscribeEvent
    static void renderGUIOverlay(CustomizeGuiOverlayEvent.DebugText evt) {
        Player player = Minecraft.getInstance().player;
        Level level = player.level();
        ItemStack wrench = getWrench(player);
        if (!(wrench.getItem() instanceof LaserWrench)) {
            return;
        }
        int range = 10; // How far away you can look at blocks from
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, ClipContext.Fluid.NONE, range);
        if (lookingAt == null || !((level.getBlockState(VectorHelper.getLookingAt(player, wrench, range).getBlockPos()).getBlock() instanceof LaserConnectorAdv))) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(lookingAt.getBlockPos());
        if (blockEntity instanceof LaserConnectorAdvBE laserConnectorAdvBE) {
            GuiGraphics guiGraphics = evt.getGuiGraphics();
            Font font = Minecraft.getInstance().font;
            renderLocation(font, guiGraphics, laserConnectorAdvBE);
        }
    }

}