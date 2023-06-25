package com.direwolf20.laserio.common.network.packets;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.blocks.LaserNode;
import com.direwolf20.laserio.common.containers.*;
import com.direwolf20.laserio.common.containers.customhandler.CardItemHandler;
import com.direwolf20.laserio.common.items.cards.CardEnergy;
import com.direwolf20.laserio.common.items.cards.CardFluid;
import com.direwolf20.laserio.common.items.cards.CardItem;
import com.direwolf20.laserio.common.items.cards.CardRedstone;
import com.direwolf20.laserio.common.items.filters.BaseFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.awt.*;
import java.util.function.Supplier;

import static com.direwolf20.laserio.common.items.cards.BaseCard.getInventory;


public class PacketChangeColor {
    private BlockPos sourcePos;
    private int color;

    public PacketChangeColor(BlockPos pos, int color) {
        this.sourcePos = pos;
        this.color = color;
    }

    public static void encode(PacketChangeColor msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeInt(msg.color);
    }

    public static PacketChangeColor decode(FriendlyByteBuf buffer) {
        return new PacketChangeColor(buffer.readBlockPos(), buffer.readInt());

    }

    public static class Handler {
        public static void handle(PacketChangeColor msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender == null)
                    return;

                BlockEntity blockEntity = sender.level().getBlockEntity(msg.sourcePos);
                if (blockEntity instanceof LaserNodeBE laserNodeBE) {
                    laserNodeBE.setColor(new Color(msg.color, true));
                    laserNodeBE.discoverAllNodes();
                }
            });


            ctx.get().setPacketHandled(true);
        }
    }
}
