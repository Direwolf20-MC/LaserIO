package com.direwolf20.laserio.integration.mekanism.client.chemicalparticle;


import com.direwolf20.laserio.util.DimBlockPos;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;

public class ParticleDataChemical {
    public record PositionData(DimBlockPos node, byte direction, byte position) {
    }

    public String chemicalType;
    public ChemicalStack<?> chemicalStack;
    public PositionData fromData;
    public PositionData toData;

    public ParticleDataChemical(ChemicalStack<?> chemicalStack, DimBlockPos fromNode, byte fromDirection, DimBlockPos toNode, byte toDirection, byte extractPosition, byte insertPosition) {
        if (chemicalStack instanceof GasStack) {
            chemicalType = ChemicalType.GAS.getSerializedName();
        } else if (chemicalStack instanceof SlurryStack) {
            chemicalType = ChemicalType.SLURRY.getSerializedName();
        } else if (chemicalStack instanceof PigmentStack) {
            chemicalType = ChemicalType.PIGMENT.getSerializedName();
        } else if (chemicalStack instanceof InfusionStack) {
            chemicalType = ChemicalType.INFUSION.getSerializedName();
        }
        this.chemicalStack = chemicalStack;
        this.fromData = new PositionData(fromNode, fromDirection, extractPosition);
        this.toData = new PositionData(toNode, toDirection, insertPosition);
    }

}
