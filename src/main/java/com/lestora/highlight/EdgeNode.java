package com.lestora.highlight;

import net.minecraft.core.BlockPos;

class EdgeNode {
    BlockPos pos;
    int lightLevel;

    EdgeNode(BlockPos pos, int lightLevel) {
        this.pos = pos;
        this.lightLevel = lightLevel;
    }
}