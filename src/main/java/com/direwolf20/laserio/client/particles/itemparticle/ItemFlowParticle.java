package com.direwolf20.laserio.client.particles.itemparticle;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ItemFlowParticle extends BreakingItemParticle {

    private double targetX, targetY, targetZ;
    Random random = new Random();

    public ItemFlowParticle(ClientLevel world, double x, double y, double z, double targetX, double targetY, double targetZ, TextureAtlasSprite sprite, int ticksPerBlock) {
        super(world, x, y, z, sprite);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        Vec3 target = new Vec3(targetX, targetY, targetZ);
        Vec3 source = new Vec3(this.x, this.y, this.z);
        Vec3 path = target.subtract(source).normalize().multiply(1, 1, 1);
        this.gravity = 0.0f;
        double distance = target.distanceTo(source);
        this.hasPhysics = false;
        float minSize = 0.05f;
        float maxSize = 0.15f;
        float partSize = minSize + random.nextFloat() * (maxSize - minSize);
        float speedModifier = (1f - 0.5f) * (partSize - minSize) / (maxSize - minSize) + 0.25f;
        float speedAdjust = ticksPerBlock * (1 / speedModifier);
        this.xd += path.x / speedAdjust;
        this.yd += path.y / speedAdjust;
        this.zd += path.z / speedAdjust;
        this.lifetime = (int) (distance * speedAdjust);
        this.scale(partSize);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
        }
    }

    public static final ParticleProvider<ItemFlowParticleData> FACTORY = new Factory();

    private static final class Factory implements ParticleProvider<ItemFlowParticleData> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        @Override
        public Particle createParticle(ItemFlowParticleData data, ClientLevel world, double x, double y, double z,
                                       double xAux, double yAux, double zAux, RandomSource randomSource) {
            ItemStack stack = data.getItemStack();
            if (stack.isEmpty()) stack = new ItemStack(Items.COBBLESTONE);
            TextureAtlasSprite sprite = resolveSprite(stack, world, randomSource);
            return new ItemFlowParticle(world, x, y, z, data.targetX, data.targetY, data.targetZ, sprite, data.ticksPerBlock);
        }

        private TextureAtlasSprite resolveSprite(ItemStack stack, ClientLevel level, RandomSource random) {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(this.scratchRenderState, stack, ItemDisplayContext.GROUND, level, null, 0);
            Material.Baked material = this.scratchRenderState.pickParticleMaterial(random);
            return material != null ? material.sprite() : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
        }
    }
}
