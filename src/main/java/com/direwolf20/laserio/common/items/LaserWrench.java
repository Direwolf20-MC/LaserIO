package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.blocks.baseblocks.BaseLaserBlock;
import com.direwolf20.laserio.setup.ModSetup;
import com.direwolf20.laserio.util.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class LaserWrench extends Item {
    public static int maxDistance = 8;

    public LaserWrench() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP)
                .stacksTo(1));
    }

    public static BlockPos storeConnectionPos(ItemStack wrench, BlockPos pos) {
        wrench.getOrCreateTag().put("connectionpos", NbtUtils.writeBlockPos(pos));
        return pos;
    }

    public static BlockPos getConnectionPos(ItemStack wrench) {
        CompoundTag compound = wrench.getOrCreateTag();
        return !compound.contains("connectionpos") ? storeConnectionPos(wrench, BlockPos.ZERO) : NbtUtils.readBlockPos(compound.getCompound("connectionpos"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wrench = player.getItemInHand(hand);
        if (level.isClientSide()) //No client
            return InteractionResultHolder.success(wrench);

        int range = 10; // How far away you can click on blocks from
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, ClipContext.Fluid.NONE, range);
        if (lookingAt == null || !((level.getBlockState(VectorHelper.getLookingAt(player, wrench, range).getBlockPos()).getBlock() instanceof BaseLaserBlock))) {
            if (player.isShiftKeyDown()) {
                storeConnectionPos(wrench, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
        }
        BlockPos targetPos = lookingAt.getBlockPos();
        BlockEntity targetBE = level.getBlockEntity(targetPos);
        if (!(targetBE instanceof BaseLaserBE))
            return InteractionResultHolder.pass(wrench);

        if (player.isShiftKeyDown()) {
            //If the wrench's position equals this one, erase it
            if (targetPos.equals(getConnectionPos(wrench))) {
                storeConnectionPos(wrench, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
            //Store this position
            storeConnectionPos(wrench, targetPos);
            return InteractionResultHolder.pass(wrench);
        } else {
            BlockPos sourcePos = getConnectionPos(wrench);
            BlockEntity sourceBE = level.getBlockEntity(sourcePos);
            //If the Source TE is not one of ours, erase it
            if (!(sourceBE instanceof BaseLaserBE)) {
                storeConnectionPos(wrench, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
            //If we're too far away - send an error to the client
            if (!targetPos.closerThan(sourcePos, maxDistance)) {
                player.displayClientMessage(Component.translatable("message.laserio.wrenchrange", maxDistance), true);
                return InteractionResultHolder.pass(wrench);
            }
            //Connect or disconnect the nodes, depending on current state
            ((BaseLaserBE) targetBE).handleConnection((BaseLaserBE) sourceBE);
        }

        //System.out.println(getConnectionPos(wrench));
        return InteractionResultHolder.success(wrench);
    }
}
