package com.direwolf20.laserio.client.particles.fluidparticle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class FluidFlowParticle extends BreakingItemParticle {

    private double targetX, targetY, targetZ;
    Random random = new Random();

    public FluidFlowParticle(ClientLevel world, double x, double y, double z, double targetX, double targetY, double targetZ, FluidStack fluidStack, int ticksPerBlock) {
        super(world, x, y, z, ItemStack.EMPTY);
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
        //System.out.println(source +":"+target);
        this.hasPhysics = false;
        float minSize = 0.15f;
        float maxSize = 0.25f;
        float partSize = minSize + random.nextFloat() * (maxSize - minSize);
        float speedModifier = (1f - 0.5f) * (partSize - minSize) / (maxSize - minSize) + 0.25f;
        //float speedModifier = (0.5f - 1f) * (partSize - maxSize) / (minSize - maxSize) + 1f;
        float speedAdjust = ticksPerBlock * (1 / speedModifier);
        this.xd += path.x / speedAdjust;
        this.yd += path.y / speedAdjust;
        this.zd += path.z / speedAdjust;
        this.lifetime = (int) (distance * speedAdjust);
        this.scale(partSize);
        this.setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack)));
        //this.setSprite(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStack.getFluid().getAttributes().getStillTexture(fluidStack)));
        int i = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
        this.rCol *= (float) (i >> 16 & 255) / 255.0F;
        this.gCol *= (float) (i >> 8 & 255) / 255.0F;
        this.bCol *= (float) (i & 255) / 255.0F;
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

    public static ParticleProvider<FluidFlowParticleData> FACTORY =
            (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
                    new FluidFlowParticle(world, x, y, z, data.targetX, data.targetY, data.targetZ, data.getFluidStack(), data.ticksPerBlock);
}
