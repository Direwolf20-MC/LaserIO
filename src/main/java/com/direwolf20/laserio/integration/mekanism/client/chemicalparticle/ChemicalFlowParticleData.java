package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ChemicalFlowParticleData implements ParticleOptions {
    private final BoxedChemicalStack chemicalStack;
    public final Vec3 target;
    public final int ticksPerBlock;

    public ChemicalFlowParticleData(BoxedChemicalStack chemicalStack, double tx, double ty, double tz, int ticks) {
        this(chemicalStack, new Vec3(tx, ty, tz), ticks);
    }

    public ChemicalFlowParticleData(BoxedChemicalStack chemicalStack, Vec3 target, int ticks) {
        this.chemicalStack = chemicalStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        this.target = target;
        ticksPerBlock = ticks;
    }

    @Nonnull
    @Override
    public ParticleType<ChemicalFlowParticleData> getType() {
        return MekanismModParticles.CHEMICALFLOWPARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        this.chemicalStack.write(buffer);
        buffer.writeVec3(target);
        buffer.writeInt(this.ticksPerBlock);
    }

    @Nonnull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d %s %s %d",
                this.getType(), this.target.x, this.target.y, this.target.z, this.ticksPerBlock, this.chemicalStack.getChemicalType().getSerializedName(),
              this.chemicalStack.getChemicalStack().getType().getRegistryName(), this.chemicalStack.getChemicalStack().getAmount());
    }

    /*public String getParameters() {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.fluidStack.getFluid(), this.itemStack.getTag())).serialize();
    }*/

    public ChemicalStack<?> getChemicalStack() {
        return this.chemicalStack.getChemicalStack();
    }

    public static final Deserializer<ChemicalFlowParticleData> DESERIALIZER = new Deserializer<>() {
        private static final DynamicCommandExceptionType INVALID_TYPE = new DynamicCommandExceptionType(type -> Component.translatable("laserio.error.particle.chemical_type", type));
        private static final Dynamic2CommandExceptionType INVALID_CHEMICAL = new Dynamic2CommandExceptionType((name, type) -> Component.translatable("laserio.error.particle.chemical", name, type));

        @Nonnull
        @Override
        public ChemicalFlowParticleData fromCommand(ParticleType<ChemicalFlowParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double tx = reader.readDouble();
            reader.expect(' ');
            double ty = reader.readDouble();
            reader.expect(' ');
            double tz = reader.readDouble();
            reader.expect(' ');
            int ticks = reader.readInt();
            reader.expect(' ');
            String rawType = reader.readString();
            ChemicalType type = ChemicalType.fromString(rawType);
            if (type == null) {
                throw INVALID_TYPE.create(rawType);
            }
            reader.expect(' ');
            String rawRegistryName = reader.readString();
            ResourceLocation registryName = ResourceLocation.tryParse(rawRegistryName);
            if (registryName == null) {
                throw INVALID_CHEMICAL.create(rawRegistryName, type);
            }
            reader.expect(' ');
            long amount = reader.readLong();
            Registry<? extends Chemical<?>> registry = switch (type) {
                case GAS -> MekanismAPI.GAS_REGISTRY;
                case INFUSION -> MekanismAPI.INFUSE_TYPE_REGISTRY;
                case PIGMENT -> MekanismAPI.PIGMENT_REGISTRY;
                case SLURRY -> MekanismAPI.SLURRY_REGISTRY;
            };
            BoxedChemicalStack boxedStack = registry.getOptional(registryName)
                  .map(chemical -> BoxedChemicalStack.box(chemical.getStack(amount)))
                  .orElseThrow(() -> INVALID_CHEMICAL.create(registryName, type));
            return new ChemicalFlowParticleData(boxedStack, tx, ty, tz, ticks);
        }

        @Override
        public ChemicalFlowParticleData fromNetwork(ParticleType<ChemicalFlowParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            return new ChemicalFlowParticleData(BoxedChemicalStack.read(buffer), buffer.readVec3(), buffer.readInt());
        }
    };
}
