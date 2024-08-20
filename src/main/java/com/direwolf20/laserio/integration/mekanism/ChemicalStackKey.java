package com.direwolf20.laserio.integration.mekanism;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

import java.util.Objects;

public class ChemicalStackKey {
    public final Chemical<?> chemical;
    private final int hash;


    public ChemicalStackKey(ChemicalStack<?> stack) {
        this.chemical = stack.getType().getChemical();
        this.hash = Objects.hash(chemical);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChemicalStackKey) {
            return (((ChemicalStackKey) obj).chemical == this.chemical);
        }
        return false;
    }
}