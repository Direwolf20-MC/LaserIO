package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeSideCache {
    public LaserNodeItemHandler itemHandler;
    public LazyOptional<LaserNodeItemHandler> handlerLazyOptional;
    public int overClocker;
    public final List<ExtractorCardCache> extractorCardCaches = new CopyOnWriteArrayList<>();
    public LazyOptional<LaserNodeBE.LaserEnergyStorage> laserEnergyStorage;
    public LaserNodeBE.LaserEnergyStorage energyStorage;
    public Byte2ByteMap myRedstoneFromSensors = new Byte2ByteOpenHashMap();  //Channel,Strength

    public NodeSideCache() {

    }

    public NodeSideCache(LaserNodeItemHandler itemHandler, LazyOptional<LaserNodeItemHandler> handlerLazyOptional, int overClocker, LaserNodeBE.LaserEnergyStorage energyStorage) {
        this.itemHandler = itemHandler;
        this.handlerLazyOptional = handlerLazyOptional;
        this.overClocker = overClocker;
        this.energyStorage = energyStorage;
        this.laserEnergyStorage = LazyOptional.of(() -> energyStorage);
    }

    public void invalidateEnergy() {
        laserEnergyStorage.invalidate();
        laserEnergyStorage = LazyOptional.of(() -> energyStorage);
    }


}
