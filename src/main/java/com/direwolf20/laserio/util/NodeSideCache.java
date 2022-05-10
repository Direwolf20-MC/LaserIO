package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.containers.customhandler.LaserNodeItemHandler;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeSideCache {
    public LaserNodeItemHandler itemHandler;
    public LazyOptional<LaserNodeItemHandler> handlerLazyOptional;
    public int overClocker;
    public final List<ExtractorCardCache> extractorCardCaches = new CopyOnWriteArrayList<>();
    public final List<StockerCardCache> stockerCardCaches = new CopyOnWriteArrayList<>();

    public NodeSideCache() {

    }

    public NodeSideCache(LaserNodeItemHandler itemHandler, LazyOptional<LaserNodeItemHandler> handlerLazyOptional, int overClocker) {
        this.itemHandler = itemHandler;
        this.handlerLazyOptional = handlerLazyOptional;
        this.overClocker = overClocker;
    }


}
