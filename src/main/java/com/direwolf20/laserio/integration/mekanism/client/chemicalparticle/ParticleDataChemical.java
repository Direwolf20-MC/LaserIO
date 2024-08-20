package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;

import com.direwolf20.laserio.util.DimBlockPos;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;

public class ParticleDataChemical {
    public record PositionData(DimBlockPos node, byte direction, byte position) {
    }

    public String chemicalType;
    public ChemicalStack<?> chemicalStack;
    public PositionData fromData;
    public PositionData toData;

    public ParticleDataChemical(ChemicalStack<?> chemicalStack, DimBlockPos fromNode, byte fromDirection, DimBlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        this.chemicalType = ChemicalType.getTypeFor(chemicalStack).getSerializedName();
        this.chemicalStack = chemicalStack;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }

}