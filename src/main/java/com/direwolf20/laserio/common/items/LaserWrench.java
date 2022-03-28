package com.direwolf20.laserio.common.items;

import com.direwolf20.laserio.setup.ModSetup;
import com.direwolf20.laserio.util.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class LaserWrench extends Item {
    public LaserWrench() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP));
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
            return InteractionResultHolder.pass(wrench);

        int range = 10;
        int maxDistance = 8;
        BlockHitResult lookingAt = VectorHelper.getLookingAt((Player) player, ClipContext.Fluid.NONE, range);
        if (lookingAt == null || (level.getBlockState(VectorHelper.getLookingAt((Player) player, wrench, range).getBlockPos()) == Blocks.AIR.defaultBlockState())) {
            if (player.isShiftKeyDown()) {
                storeConnectionPos(wrench, BlockPos.ZERO);
                return InteractionResultHolder.pass(wrench);
            }
        }
        BlockPos pos = lookingAt.getBlockPos();
        /*TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof NodeTileBase))
            return new ActionResult<>(ActionResultType.PASS, wrench);
        */
        if (player.isShiftKeyDown()) {
            //If the wrench doesn't already have a connection, store this position
            if (getConnectionPos(wrench).equals(BlockPos.ZERO))
                storeConnectionPos(wrench, pos);
            else {
                //If the wrench's position equals this one, erase it
                if (pos.equals(getConnectionPos(wrench))) {
                    storeConnectionPos(wrench, BlockPos.ZERO);
                    return InteractionResultHolder.pass(wrench);
                }
                BlockPos sourcePos = getConnectionPos(wrench);
                //TileEntity sourceTE = world.getTileEntity(sourcePos);
                /*if (!(sourceTE instanceof NodeTileBase)) {
                    storeConnectionPos(wrench, BlockPos.ZERO);
                    return new ActionResult<>(ActionResultType.PASS, wrench);
                }*/
                if (!pos.closerThan(sourcePos, maxDistance)) {
                    player.displayClientMessage(new TranslatableComponent("message.logisticslasers.wrenchrange", maxDistance), true);
                    return InteractionResultHolder.pass(wrench);
                }
                /*if (!((NodeTileBase) te).addConnection(sourcePos))
                    ((NodeTileBase) te).removeConnection(sourcePos);*/
            }
        }
        System.out.println(getConnectionPos(wrench));
        return InteractionResultHolder.pass(wrench);
    }
}
