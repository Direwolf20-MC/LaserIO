package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.common.blockentities.LaserConnectorAdvBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.blocks.baseblocks.BaseLaserBlock;
import com.direwolf20.laserio.setup.LaserIODataComponents;
import com.direwolf20.laserio.util.MiscTools;
import com.direwolf20.laserio.util.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
        super(new Item.Properties()
                .stacksTo(1));
    }

    /*public static ResourceLocation storeDimension(ItemStack wrench, ResourceLocation dim) {
        wrench.getOrCreateTag().putString("dimension", dim.toString());
        return dim;
    }

    public static ResourceLocation getDimension(ItemStack wrench, Level level) {
        CompoundTag compound = wrench.getOrCreateTag();
        String dimName = !compound.contains("dimension") ? level.dimension().location().toString() :compound.getString("dimension");
        ResourceLocation dim = new ResourceLocation(dimName);
        return dim;
    }*/

    public static GlobalPos storeConnectionPos(ItemStack wrench, Level level, BlockPos pos) {
        GlobalPos globalPos = new GlobalPos(level.dimension(), pos);
        wrench.set(LaserIODataComponents.BOUND_GLOBAL_POS, globalPos);
        return globalPos;
    }

    public static GlobalPos getConnectionPos(ItemStack wrench, Level level) {
        if (level == null) return null;
        if (!wrench.has(LaserIODataComponents.BOUND_GLOBAL_POS))
            return storeConnectionPos(wrench, level, BlockPos.ZERO);
        return wrench.get(LaserIODataComponents.BOUND_GLOBAL_POS);
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
                storeConnectionPos(wrench, level, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
        }
        BlockPos targetPos = lookingAt.getBlockPos();
        BlockEntity targetBE = level.getBlockEntity(targetPos);
        if (!(targetBE instanceof BaseLaserBE))
            return InteractionResultHolder.pass(wrench);

        //((ServerLevel) level).server.getLevel(ResourceKey.create(Registries.DIMENSION, getDimension(wrench, level)))

        if (player.isShiftKeyDown()) {
            //If the wrench's position equals this one, erase it
            if (targetPos.equals(getConnectionPos(wrench, level))) {
                storeConnectionPos(wrench, level, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
            //Store this position
            storeConnectionPos(wrench, level, targetPos);
            return InteractionResultHolder.pass(wrench);
        } else {
            GlobalPos sourceDimPos = getConnectionPos(wrench, level);
            BlockEntity sourceBE = MiscTools.getLevel(level.getServer(), sourceDimPos).getBlockEntity(sourceDimPos.pos());
            //If the Source TE is not one of ours, erase it
            if (!(sourceBE instanceof BaseLaserBE)) {
                storeConnectionPos(wrench, level, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
            //If both nodes are Advanced, we can connect them despite distance, so skip that check and connect now
            if (targetBE instanceof LaserConnectorAdvBE targetAdv && sourceBE instanceof LaserConnectorAdvBE sourceAdv) {
                targetAdv.handleAdvancedConnection(sourceAdv);
                return InteractionResultHolder.success(wrench);
            }
            //If we're too far away - send an error to the client
            if (!targetPos.closerThan(sourceDimPos.pos(), maxDistance) || !level.equals(MiscTools.getLevel(level.getServer(), sourceDimPos))) {
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
