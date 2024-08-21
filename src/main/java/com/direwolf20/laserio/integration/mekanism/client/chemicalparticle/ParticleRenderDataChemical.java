package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;

public class ParticleRenderDataChemical {
    public ChemicalStack chemicalStack;
    public BlockPos fromPos;
    public byte direction;
    public BlockPos toPos;
    public byte position;

    public ParticleRenderDataChemical(ChemicalStack chemicalStack, BlockPos fromPos, byte direction, BlockPos toPos, byte position) {
        this.chemicalStack = chemicalStack;
        this.fromPos = fromPos;
        this.direction = direction;
        this.toPos = toPos;
        this.position = position;
    }

}
