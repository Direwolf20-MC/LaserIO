package com.direwolf20.laserio.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransferResult {
    public record Result(IItemHandler handler, int count, int slot, InserterCardCache inserterCardCache) {

    }

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
        this.remainingStack = stack; //TODO Copy?
    }

    public int getTotalItemCounts() {
        return results.stream().mapToInt(i -> i.count).sum();
    }

    public void addResult(TransferResult newResult) {
        results.addAll(newResult.results);
        if (remainingStack.isEmpty())
            remainingStack = newResult.remainingStack;
        else if (ItemHandlerHelper.canItemStacksStack(remainingStack, newResult.remainingStack))
            remainingStack.grow(newResult.remainingStack.getCount());
    }
}
