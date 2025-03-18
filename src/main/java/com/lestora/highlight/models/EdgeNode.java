package com.lestora.highlight.models;

import net.minecraft.core.BlockPos;

public class EdgeNode {
    public BlockPos pos;
    public int lightLevel;

    public EdgeNode(BlockPos pos, int lightLevel) {
        this.pos = pos;
        this.lightLevel = lightLevel;
    }
}