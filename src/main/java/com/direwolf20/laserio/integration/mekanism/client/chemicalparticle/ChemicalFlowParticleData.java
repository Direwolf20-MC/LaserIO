package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ChemicalFlowParticleData implements ParticleOptions {
    private final ChemicalStack<?> chemicalStack;
    public final String type;
    public final double targetX;
    public final double targetY;
    public final double targetZ;
    public final int ticksPerBlock;

    public ChemicalFlowParticleData(ChemicalStack<?> chemicalStack, double tx, double ty, double tz, int ticks, String type) {
        this.chemicalStack = chemicalStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        ticksPerBlock = ticks;
        this.type = type;
    }

    @Nonnull
    @Override
    public ParticleType<ChemicalFlowParticleData> getType() {
        return MekanismModParticles.CHEMICALFLOWPARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.type);
        ChemicalUtils.writeChemicalStack(buffer, this.chemicalStack);
        buffer.writeDouble(this.targetX);
        buffer.writeDouble(this.targetY);
        buffer.writeDouble(this.targetZ);
        buffer.writeInt(this.ticksPerBlock);
    }

    @Nonnull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d %s",
                this.getType(), this.targetX, this.targetY, this.targetZ, this.ticksPerBlock, this.type);
    }

    /*public String getParameters() {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.fluidStack.getFluid(), this.itemStack.getTag())).serialize();
    }*/

    @OnlyIn(Dist.CLIENT)
    public ChemicalStack<?> getChemicalStack() {
        return this.chemicalStack;
    }

    public static final Deserializer<ChemicalFlowParticleData> DESERIALIZER = new Deserializer<ChemicalFlowParticleData>() {
        @Nonnull
        @Override
        public ChemicalFlowParticleData fromCommand(ParticleType<ChemicalFlowParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            String type = reader.readString();
            reader.expect(' ');


            reader.expect(' ');
            double tx = reader.readDouble();
            reader.expect(' ');
            double ty = reader.readDouble();
            reader.expect(' ');
            double tz = reader.readDouble();
            reader.expect(' ');
            int ticks = reader.readInt();
            return new ChemicalFlowParticleData(GasStack.EMPTY, tx, ty, tz, ticks, type); //TODO Check this
        }

        @Override
        public ChemicalFlowParticleData fromNetwork(ParticleType<ChemicalFlowParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            String type = buffer.readUtf();
            ChemicalType chemicalType = ChemicalType.fromString(type);
            if (chemicalType == ChemicalType.GAS)
                return new ChemicalFlowParticleData(ChemicalUtils.readGasStack(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), type);
            else if (chemicalType == ChemicalType.INFUSION)
                return new ChemicalFlowParticleData(ChemicalUtils.readInfusionStack(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), type);
            else if (chemicalType == ChemicalType.PIGMENT)
                return new ChemicalFlowParticleData(ChemicalUtils.readPigmentStack(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), type);
            else if (chemicalType == ChemicalType.SLURRY)
                return new ChemicalFlowParticleData(ChemicalUtils.readSlurryStack(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), type);
            else
                return null; //Shouldn't happen?
        }
    };
}
