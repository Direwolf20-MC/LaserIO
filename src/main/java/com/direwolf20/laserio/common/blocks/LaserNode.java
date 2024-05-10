package com.direwolf20.laserio.common.blocks;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.baseblocks.BaseLaserBlock;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.items.CardHolder;
import com.direwolf20.laserio.common.items.LaserWrench;
import com.direwolf20.laserio.common.items.cards.BaseCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LaserNode extends BaseLaserBlock implements EntityBlock {
    //This makes the shape fit the model perfectly, but introduces issues with clicking on specific sides of the block
    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);
    public static final String SCREEN_LASERNODE = "screen.laserio.lasernode";

    public LaserNode() {
        super();
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult hit) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof LaserWrench)
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof LaserNodeBE) {

                if (heldItem.getItem() instanceof BaseCard) {
                    IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, hit.getDirection());
                    ItemStack remainingStack = insertItemToNode(itemHandler, heldItem, false);
                    player.setItemInHand(InteractionHand.MAIN_HAND, remainingStack);
                } else {
                    Direction direction;
                    if (player.isShiftKeyDown())
                        direction = hit.getDirection().getOpposite();
                    else
                        direction = hit.getDirection();
                    IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, direction);
                    ItemStack cardHolder = findCardHolders(player);
                    if (!cardHolder.isEmpty()) CardHolder.getUUID(cardHolder);

                    player.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new LaserNodeContainer((LaserNodeBE) be, windowId, (byte) direction.ordinal(), playerInventory, playerEntity, (LaserNodeItemHandler) itemHandler, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), cardHolder), Component.translatable("")), (buf -> {
                        buf.writeBlockPos(blockPos);
                        buf.writeByte((byte) direction.ordinal());
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, cardHolder);
                    }));
                }
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }

        }
        return InteractionResult.SUCCESS;
    }

    /** Custom Implementation of ItemHandlerHelper.insertItem for right clicking nodes with **/
    public static ItemStack insertItemToNode(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty())
            return stack;

        for (int i = 0; i < LaserNodeContainer.CARDSLOTS; i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public static ItemStack findCardHolders(Player player) {
        ItemStack cardHolder = ItemStack.EMPTY;
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.items.size(); i++) {
            ItemStack itemStack = playerInventory.items.get(i);
            if (itemStack.getItem() instanceof CardHolder) return itemStack;
        }
        return cardHolder;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof LaserNodeBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t) -> {
            if (t instanceof LaserNodeBE tile) {
                tile.tickServer();
            }
        };
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        //System.out.println("Neighbor changed at: " + pos + " from: " + fromPos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LaserNodeBE laserNodeBE) {
            laserNodeBE.rendersChecked = false;
            laserNodeBE.clearCachedInventories();
            laserNodeBE.redstoneChecked = false;
            //laserNodeBE.populateThisRedstoneNetwork(true);
        }
    }

    @Override
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        BlockEntity blockEntity = pBlockAccess.getBlockEntity(pPos);
        if (blockEntity instanceof LaserNodeBE laserNodeBE) {
            return laserNodeBE.getRedstoneSide(pSide.getOpposite());
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LaserNodeBE laserNodeBE) {
            if ((direction == null) || !laserNodeBE.redstoneCardSides.containsKey((byte) direction.getOpposite().ordinal()))
                return false;
            return laserNodeBE.redstoneCardSides.get((byte) direction.getOpposite().ordinal());
        }
        return false;
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        BlockEntity blockEntity = pBlockAccess.getBlockEntity(pPos);
        if (blockEntity instanceof LaserNodeBE laserNodeBE) {
            if (laserNodeBE.getRedstoneSideStrong(pSide.getOpposite()))
                return laserNodeBE.getRedstoneSide(pSide.getOpposite());
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaserNodeBE(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity != null) {
                for (Direction direction : Direction.values()) {
                    //LazyOptional<IItemHandler> cap = tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction);
                    //cap.ifPresent(handler -> {
                    var cap = worldIn.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, tileEntity, direction);
                    if (cap != null) {
                        for (int i = 0; i < cap.getSlots(); ++i) {
                            Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), cap.getStackInSlot(i));
                        }
                    }
                }
            }

        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }


}
