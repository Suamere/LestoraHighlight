package com.lestora.highlight;

import net.minecraft.core.BlockPos;

public class LightPos {
    private final BlockPos blockPos;
    private final int amount;

    public LightPos(BlockPos blockPos, int amount) {
        this.blockPos = blockPos;
        this.amount = amount;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getAmount() {
        return amount;
    }
}