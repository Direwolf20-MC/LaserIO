package com.direwolf20.laserio.util;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class DimBlockPos {
    public BlockPos blockPos;
    public ResourceKey<Level> levelKey;

    public DimBlockPos(Level level, BlockPos blockPos) {
        this.blockPos = blockPos;
        this.levelKey = level.dimension();
    }

    public DimBlockPos(ResourceKey<Level> levelKey, BlockPos blockPos) {
        this.blockPos = blockPos;
        this.levelKey = levelKey;
    }

    public DimBlockPos(CompoundTag tag) {
        this.levelKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("level")));
        this.blockPos = NbtUtils.readBlockPos(tag.getCompound("blockpos"));
    }

    public Level getLevel(MinecraftServer server) {
        if (server == null)
            return null;//level = Minecraft.getInstance().level;
        else
            return server.getLevel(this.levelKey);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("level", levelKey.location().toString());
        tag.put("blockpos", NbtUtils.writeBlockPos(blockPos));
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DimBlockPos) {
            return (((DimBlockPos) obj).levelKey == this.levelKey) && Objects.equals(((DimBlockPos) obj).blockPos, this.blockPos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        // Include blockPos in hash code calculation
        result = prime * result + Objects.hashCode(blockPos);

        // Include level in hash code calculation
        result = prime * result + Objects.hashCode(levelKey);

        return result;
    }

}