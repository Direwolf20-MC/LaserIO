package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ChemicalFlowParticle extends BreakingItemParticle {

    public ChemicalFlowParticle(ClientLevel world, double x, double y, double z, Vec3 target, ChemicalStack<?> chemicalStack, int ticksPerBlock) {
        super(world, x, y, z, ItemStack.EMPTY);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        Vec3 source = new Vec3(this.x, this.y, this.z);
        Vec3 path = target.subtract(source).normalize().multiply(1, 1, 1);
        this.gravity = 0.0f;
        double distance = target.distanceTo(source);
        //System.out.println(source +":"+target);
        this.hasPhysics = false;
        float minSize = 0.15f;
        float maxSize = 0.25f;
        float partSize = minSize + world.random.nextFloat() * (maxSize - minSize);
        float speedModifier = (1f - 0.5f) * (partSize - minSize) / (maxSize - minSize) + 0.25f;
        //float speedModifier = (0.5f - 1f) * (partSize - maxSize) / (minSize - maxSize) + 1f;
        float speedAdjust = ticksPerBlock * (1 / speedModifier);
        this.xd += path.x / speedAdjust;
        this.yd += path.y / speedAdjust;
        this.zd += path.z / speedAdjust;
        this.lifetime = (int) (distance * speedAdjust);
        this.scale(partSize);
        this.setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(chemicalStack.getChemical().getIcon()));
        int i = chemicalStack.getChemicalColorRepresentation();
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

    public static ParticleProvider<ChemicalFlowParticleData> FACTORY = (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
            new ChemicalFlowParticle(world, x, y, z, data.target, data.getChemicalStack().getChemicalStack(), data.ticksPerBlock);
}
