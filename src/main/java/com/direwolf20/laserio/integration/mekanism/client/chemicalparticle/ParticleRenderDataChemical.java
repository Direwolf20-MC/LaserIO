package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import mekanism.api.chemical.merged.BoxedChemicalStack;
import net.minecraft.core.BlockPos;

public class ParticleRenderDataChemical {
    public BoxedChemicalStack chemicalStack;
    public BlockPos fromPos;
    public byte direction;
    public BlockPos toPos;
    public byte position;

    public ParticleRenderDataChemical(BoxedChemicalStack chemicalStack, BlockPos fromPos, byte direction, BlockPos toPos, byte position) {
        this.chemicalStack = chemicalStack;
        this.fromPos = fromPos;
        this.direction = direction;
        this.toPos = toPos;
        this.position = position;
    }

}
