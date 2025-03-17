package com.lestora.highlight;

import net.minecraft.core.BlockPos;

class AxisNode {
    BlockPos pos;
    int distance;
    AxisNode parent;

    AxisNode(BlockPos pos, int distance, AxisNode parent) {
        this.pos = pos;
        this.distance = distance;
        this.parent = parent;
    }
}
