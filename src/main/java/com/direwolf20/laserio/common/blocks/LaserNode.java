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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LaserNode extends BaseLaserBlock implements EntityBlock {
    //This makes the shape fit the model perfectly, but introduces issues with clicking on specific sides of the block
    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);
    public static final String SCREEN_LASERNODE = "screen.laserio.lasernode";

    public LaserNode(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult hit) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof LaserWrench)
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof LaserNodeBE) {

                if (heldItem.getItem() instanceof BaseCard) {
                    ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, blockPos, hit.getDirection());
                    ItemStack remainingStack = insertItemToNode(itemHandler, heldItem);
                    player.setItemInHand(InteractionHand.MAIN_HAND, remainingStack);
                } else {
                    Direction direction;
                    if (player.isShiftKeyDown())
                        direction = hit.getDirection().getOpposite();
                    else
                        direction = hit.getDirection();
                    LaserNodeItemHandler itemHandler = ((LaserNodeBE) be).nodeSideCaches[direction.ordinal()].itemHandler;
                    ItemStack cardHolder = findCardHolders(player);
                    if (!cardHolder.isEmpty()) CardHolder.getUUID(cardHolder);

                    player.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) -> new LaserNodeContainer((LaserNodeBE) be, windowId, (byte) direction.ordinal(), playerInventory, playerEntity, itemHandler, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), cardHolder), Component.translatable("")), (buf -> {
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
    public static ItemStack insertItemToNode(ResourceHandler<ItemResource> dest, @Nonnull ItemStack stack) {
        if (dest == null || stack.isEmpty())
            return stack;

        ItemResource resource = ItemResource.of(stack);
        int remaining = stack.getCount();
        for (int i = 0; i < LaserNodeContainer.CARDSLOTS && remaining > 0; i++) {
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = dest.insert(i, resource, remaining, tx);
                if (inserted > 0) {
                    tx.commit();
                    remaining -= inserted;
                }
            }
        }

        return remaining <= 0 ? ItemStack.EMPTY : resource.toStack(remaining);
    }

    public static ItemStack findCardHolders(Player player) {
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack.getItem() instanceof CardHolder) return itemStack;
        }
        return ItemStack.EMPTY;
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

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LaserNodeBE laserNodeBE) {
            laserNodeBE.rendersChecked = false;
            laserNodeBE.clearCachedInventories();
            laserNodeBE.redstoneChecked = false;
        }
    }

    @Override
    protected int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
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
    protected int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
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
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return SHAPE;
    }

    @Override
    protected float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState p_48740_) {
        return true;
    }


}
