package com.direwolf20.laserio.common.network.handler;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blockentities.basebe.BaseLaserBE;
import com.direwolf20.laserio.common.containers.CardEnergyContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.CardRedstoneContainer;
import com.direwolf20.laserio.common.containers.LaserNodeContainer;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import com.direwolf20.laserio.common.network.data.OpenNodePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Optional;

import static com.direwolf20.laserio.common.blocks.LaserNode.SCREEN_LASERNODE;
import static com.direwolf20.laserio.common.blocks.LaserNode.findCardHolders;

public class PacketOpenNode {
    public static final PacketOpenNode INSTANCE = new PacketOpenNode();

    public static PacketOpenNode get() {
        return INSTANCE;
    }

    public void handle(final OpenNodePayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> senderOptional = context.player();
            if (senderOptional.isEmpty())
                return;
            ServerPlayer sender = (ServerPlayer) senderOptional.get();

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null)
                return;

            BlockPos pos;
            if (container instanceof LaserNodeContainer)
                pos = payload.sourcePos();
            else if (container instanceof CardItemContainer cardItemContainer)
                pos = cardItemContainer.sourceContainer;
            else if (container instanceof CardEnergyContainer cardEnergyContainer)
                pos = cardEnergyContainer.sourceContainer;
            else if (container instanceof CardRedstoneContainer cardRedstoneContainer)
                pos = cardRedstoneContainer.sourceContainer;
            else return;

            final BlockPos sourcePos = pos;
            BlockEntity be = sender.level().getBlockEntity(sourcePos);
            if (be == null || !(be instanceof BaseLaserBE))
                return;

            ItemStack heldStack = sender.containerMenu.getCarried();
            if (!heldStack.isEmpty()) {
                // set it to empty, so it's doesn't get dropped
                sender.containerMenu.setCarried(ItemStack.EMPTY);
            }
            IItemHandler h = sender.level().getCapability(Capabilities.ItemHandler.BLOCK, sourcePos, Direction.values()[payload.side()]);
            ItemStack cardHolder = findCardHolders(sender);

            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(SCREEN_LASERNODE);
                }

                @Override
                public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                    return false;
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new LaserNodeContainer((LaserNodeBE) be, windowId, payload.side(), playerInventory, playerEntity, (LaserNodeItemHandler) h, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), cardHolder);
                }
            };

            sender.openMenu(containerProvider, (buf -> {
                buf.writeBlockPos(pos);
                buf.writeByte(payload.side());
                buf.writeItem(cardHolder);
            }));

            if (!heldStack.isEmpty()) {
                sender.containerMenu.setCarried(heldStack);
                sender.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack));
            }
        });
    }
}
