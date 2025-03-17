package com.lestora.highlight;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class HighlightEmitterHelper {
    public static Direction oppositeDir(Direction dir) {
        return switch (dir) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    public static Direction @NotNull [] getAdjacentSides(Direction direction) {
        Direction[] sideDirs;
        switch (direction) {
            case UP, DOWN -> sideDirs = new Direction[]{ Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
            case NORTH, SOUTH -> sideDirs = new Direction[]{ Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST };
            case EAST, WEST -> sideDirs = new Direction[]{ Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN };
            default -> sideDirs = new Direction[]{};
        }
        return sideDirs;
    }
}
