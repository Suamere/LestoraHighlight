package com.lestora.highlight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HasTorchSupport {
    public static boolean hasSupport(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.isFaceSturdy(level, pos.below(), Direction.UP)) {
            return true;
        }
        if (level.getBlockState(pos.north()).isFaceSturdy(level, pos.north(), Direction.SOUTH)) {
            return true;
        }
        if (level.getBlockState(pos.south()).isFaceSturdy(level, pos.south(), Direction.NORTH)) {
            return true;
        }
        if (level.getBlockState(pos.east()).isFaceSturdy(level, pos.east(), Direction.WEST)) {
            return true;
        }
        if (level.getBlockState(pos.west()).isFaceSturdy(level, pos.west(), Direction.EAST)) {
            return true;
        }
        return false;
    }
}
