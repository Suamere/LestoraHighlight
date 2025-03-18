package com.lestora.highlight.models;

import net.minecraft.core.BlockPos;

public class AxisNode {
    public BlockPos pos;
    public int distance;
    public AxisNode parent;

    public AxisNode(BlockPos pos, int distance, AxisNode parent) {
        this.pos = pos;
        this.distance = distance;
        this.parent = parent;
    }
}
