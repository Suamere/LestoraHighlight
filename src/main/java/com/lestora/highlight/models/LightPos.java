package com.lestora.highlight.models;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class LightPos {
    private final BlockPos blockPos;
    private final int amount;
    private final ResourceLocation resource;

    public LightPos(BlockPos blockPos, int amount, ResourceLocation resource) {
        this.blockPos = blockPos;
        this.amount = amount;
        this.resource = resource;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getAmount() {
        return amount;
    }

    public ResourceLocation getResource() { return resource; }
}