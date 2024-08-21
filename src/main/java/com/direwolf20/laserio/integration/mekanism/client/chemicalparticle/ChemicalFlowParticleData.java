package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.util.MiscTools;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class ChemicalFlowParticleData implements ParticleOptions {
    public static final MapCodec<ChemicalFlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ChemicalStack.CODEC.fieldOf("chemicalStack").forGetter(ChemicalFlowParticleData::getChemicalStack),
                    Vec3.CODEC.fieldOf("target").forGetter(ChemicalFlowParticleData::getTarget),
                    Codec.INT.fieldOf("ticksPerBlock").forGetter(ChemicalFlowParticleData::getTicksPerBlock)
            ).apply(instance, ChemicalFlowParticleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalFlowParticleData> STREAM_CODEC = StreamCodec.composite(
            ChemicalStack.STREAM_CODEC,
            ChemicalFlowParticleData::getChemicalStack,
            MiscTools.VEC3_STREAM_CODEC,
            ChemicalFlowParticleData::getTarget,
            ByteBufCodecs.INT,
            ChemicalFlowParticleData::getTicksPerBlock,
            ChemicalFlowParticleData::new
    );

    private final ChemicalStack chemicalStack;
    public final Vec3 target;
    public final int ticksPerBlock;

    public ChemicalFlowParticleData(ChemicalStack chemicalStack, double tx, double ty, double tz, int ticks) {
        this(chemicalStack, new Vec3(tx, ty, tz), ticks);
    }

    public ChemicalFlowParticleData(ChemicalStack chemicalStack, Vec3 target, int ticks) {
        this.chemicalStack = chemicalStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        this.target = target;
        ticksPerBlock = ticks;
    }

    @Nonnull
    @Override
    public ParticleType<ChemicalFlowParticleData> getType() {
        return MekanismModParticles.CHEMICALFLOWPARTICLE.get();
    }

    public ChemicalStack getChemicalStack() {
        return chemicalStack;
    }

    public Vec3 getTarget() {
        return target;
    }

    public int getTicksPerBlock() {
        return ticksPerBlock;
    }
}
