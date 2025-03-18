package com.lestora.highlight.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class HasTorchSupport {
    /**
     * Checks if the candidate position has support for a torch.
     * Uses isFaceSturdy: the block below must be sturdy on its UP face,
     * or one of the horizontal neighbors must be sturdy on its facing side.
     *
     * @param level The world Level.
     * @param pos   The candidate BlockPos.
     * @return true if support exists.
     */
    public static boolean hasSupport(Level level, BlockPos pos) {
        if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
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
