package com.direwolf20.laserio.util;

import com.direwolf20.laserio.common.blockentities.LaserNodeBE;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransferResult {
    public List<Result> results = new CopyOnWriteArrayList<>();
    public ItemStack remainingStack = ItemStack.EMPTY;

    public TransferResult() {

    }

    public TransferResult(Result result) {
        results.add(result);
    }

    public TransferResult(Result result, ItemStack itemStack) {
        results.add(result);
        this.remainingStack = itemStack;
    }

    public void addResult(Result result) {
        this.results.add(result);
    }

    public void addRemainingStack(ItemStack stack) {
        this.remainingStack = stack;
    }

    public int getTotalItemCounts() {
        return results.stream().mapToInt(i -> i.itemStack.getCount()).sum();
    }

    public void addResult(TransferResult newResult) {
        results.addAll(newResult.results);
        if (remainingStack.isEmpty())
            remainingStack = newResult.remainingStack;
        else if (ItemStack.isSameItemSameComponents(remainingStack, newResult.remainingStack))
            remainingStack.grow(newResult.remainingStack.getCount());
    }

    public void addOtherCard(ResourceHandler<ItemResource> handler, int slot, BaseCardCache card, LaserNodeBE be) {
        for (Result result : results) {
            if (result.inserterCardCache == null)
                result.addInserter(handler, slot, card, be);
            else if (result.extractorCardCache == null)
                result.addExtractor(handler, slot, card, be);
        }
    }

    public Result splitResult(Result result, int count) {
        if (!results.contains(result))
            return null;
        int position = results.indexOf(result);
        Result newResult = new Result(result.insertHandler, result.extractHandler, result.insertSlot, result.extractSlot, result.inserterCardCache, result.extractorCardCache, result.fromBE, result.toBE, result.itemStack);
        results.get(position).itemStack.shrink(count);
        newResult.itemStack.shrink(result.itemStack.getCount());
        results.add(newResult);
        return result;
    }


    public void doIt() {
        for (Result result : results) {
            result.doIt();
        }

    }

    public static class Result {
        public ResourceHandler<ItemResource> extractHandler; //The inventory being extracted from
        public ResourceHandler<ItemResource> insertHandler; //The inventory being inserted to
        public int insertSlot; //The slot we inserted to
        public int extractSlot; //The slot we extract from
        public BaseCardCache inserterCardCache;
        public BaseCardCache extractorCardCache;
        public ItemStack itemStack;
        public LaserNodeBE fromBE;
        public LaserNodeBE toBE;


        public Result(ResourceHandler<ItemResource> insertHandler, ResourceHandler<ItemResource> extractHandler, int insertSlot, int extractSlot, BaseCardCache inserterCardCache, BaseCardCache extractorCardCache, LaserNodeBE fromBE, LaserNodeBE toBE, ItemStack itemStack) {
            this.insertHandler = insertHandler;
            this.extractHandler = extractHandler;
            this.insertSlot = insertSlot;
            this.extractSlot = extractSlot;
            this.inserterCardCache = inserterCardCache;
            this.extractorCardCache = extractorCardCache;
            this.fromBE = fromBE;
            this.toBE = toBE;
            this.itemStack = itemStack;
        }

        public Result(ResourceHandler<ItemResource> handler, int slot, BaseCardCache cardCache, ItemStack itemStack, LaserNodeBE be, boolean extractor) {
            if (extractor) {
                this.extractHandler = handler;
                this.extractSlot = slot;
                this.extractorCardCache = cardCache;
                this.itemStack = itemStack;
                this.fromBE = be;
            } else {
                this.insertHandler = handler;
                this.insertSlot = slot;
                this.inserterCardCache = cardCache;
                this.itemStack = itemStack;
                this.toBE = be;
            }
        }

        public void addInserter(ResourceHandler<ItemResource> handler, int slot, BaseCardCache inserterCardCache, LaserNodeBE be) {
            this.insertHandler = handler;
            this.insertSlot = slot;
            this.inserterCardCache = inserterCardCache;
            this.toBE = be;
        }

        public void addExtractor(ResourceHandler<ItemResource> handler, int slot, BaseCardCache extractorCardCache, LaserNodeBE be) {
            this.extractHandler = handler;
            this.extractSlot = slot;
            this.extractorCardCache = extractorCardCache;
            this.fromBE = be;
        }

        public void doIt() {
            if (fromBE == null || toBE == null || extractorCardCache == null || inserterCardCache == null) //Happens if we forgot to set this!
                return;

            ItemResource resource = ItemResource.of(itemStack);
            try (Transaction tx = Transaction.openRoot()) {
                //Extract
                if (extractSlot == -1) //We don't know which slot to pull from
                    extractHandler.extract(resource, itemStack.getCount(), tx);
                else
                    extractHandler.extract(extractSlot, resource, itemStack.getCount(), tx);
                //Insert
                if (insertSlot == -1) //We don't know which slot to insert to
                    insertHandler.insert(resource, itemStack.getCount(), tx);
                else
                    insertHandler.insert(insertSlot, resource, itemStack.getCount(), tx);
                tx.commit();
            }
            if (extractorCardCache instanceof StockerCardCache)
                fromBE.drawParticles(itemStack, inserterCardCache.direction, toBE, fromBE, extractorCardCache.direction, inserterCardCache.cardSlot, extractorCardCache.cardSlot);
            else
                fromBE.drawParticles(itemStack, extractorCardCache.direction, fromBE, toBE, inserterCardCache.direction, extractorCardCache.cardSlot, inserterCardCache.cardSlot);
        }

        public int count() {
            return itemStack.getCount();
        }

    }
}
