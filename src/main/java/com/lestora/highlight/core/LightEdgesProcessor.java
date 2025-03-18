package com.lestora.highlight.core;

import com.lestora.highlight.helpers.HighlightEntry;
import com.lestora.highlight.models.EdgeNode;
import com.lestora.highlight.models.HighlightColor;
import com.lestora.highlight.models.HighlightFace;
import com.lestora.highlight.models.LightEdges;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class LightEdgesProcessor {

    public static LightEdges findLightEdges(BlockPos lightPos, int lightLevel, Level level, boolean showAllOutlines) {
        Queue<EdgeNode> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        List<HighlightEntry> lightLevel1 = new ArrayList<>();
        List<HighlightEntry> lightLevel0 = new ArrayList<>();

        queue.add(new EdgeNode(lightPos, lightLevel));
        visited.add(lightPos);

        while (!queue.isEmpty()) {
            EdgeNode current = queue.poll();
            BlockPos currentPos = current.pos;
            int currentLight = current.lightLevel;

            if (currentLight > 1) {
                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.relative(direction);
                    if (visited.contains(neighborPos)) {
                        continue;
                    }

                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (!HighlightMemory.isTransparent(level, neighborState, neighborPos, direction)) {
                        continue;
                    }

                    visited.add(neighborPos);
                    queue.add(new EdgeNode(neighborPos, currentLight - 1));
                }
            } else if (currentLight == 1) {
                var actualLight = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(currentPos);
                if (actualLight == 0) {
                    var uuid = UUID.nameUUIDFromBytes(("lightSource:" + currentPos.getX() + ":" + currentPos.getY() + ":" + currentPos.getZ()).getBytes(StandardCharsets.UTF_8));
                    HighlightSphere.setHighlightCenterAndRadius(uuid, currentPos.getX(), currentPos.getY(), currentPos.getZ(), 0, HighlightColor.red(0.5f), level);
                    System.err.println("Light Level 0 where 1 was expected, is your light level configuration right? " + currentPos);
                }
                else if (actualLight > 1 && !showAllOutlines){
                    continue;
                }

                LightEdges thisEdge = processLightEdge(level, currentPos, showAllOutlines);
                lightLevel1.addAll(thisEdge.LightLevel1);
                lightLevel0.addAll(thisEdge.LightLevel0);
            }
        }

        return new LightEdges(lightLevel1, lightLevel0);
    }

    private static LightEdges processLightEdge(Level level, BlockPos l1Edge, boolean showAllOutlines) {
        List<HighlightEntry> lightLevel1 = new ArrayList<>();
        List<HighlightEntry> lightLevel0 = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = l1Edge.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            boolean neighborSolid = !HighlightMemory.isTransparent(level, neighborState, neighborPos, direction);
            if (neighborSolid) {
                HighlightFace neighborFace = HighlightEntry.fromOppositeDirection(direction);
                if (neighborFace == null) { System.err.println("null from direction: " + direction); continue; }
                for (Direction lightAdjDir : getAdjacentSides(direction)) {
                    BlockPos lightAdjPos = l1Edge.relative(lightAdjDir);
                    var adjLightLevel = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(lightAdjPos);
                    if (adjLightLevel == 0) {
                        var lightEdge = HighlightEntry.corner(direction, lightAdjDir);
                        lightLevel1.add(new HighlightEntry(neighborPos, HighlightColor.yellow(), neighborFace, lightEdge));
                    }
                    else if (showAllOutlines) {
                        var expectedEdge = HighlightEntry.corner(direction, lightAdjDir);
                        lightLevel1.add(new HighlightEntry(neighborPos, HighlightColor.yellow(), neighborFace, expectedEdge));
                    }
                }
            }
            else {
                var sideLight = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(neighborPos);
                if (sideLight == 0){
                    for (Direction darkDirection : getAdjacentSides(direction)) {
                        BlockPos darkPos = neighborPos.relative(darkDirection);
                        BlockState darkState = level.getBlockState(darkPos);
                        boolean darkSolid = !HighlightMemory.isTransparent(level, darkState, darkPos, darkDirection);
                        if (darkSolid) {
                            HighlightFace darkFace = HighlightEntry.fromOppositeDirection(darkDirection);
                            if (darkFace == null) { System.err.println("null from direction: " + darkDirection); continue; }
                            var darkEdge = HighlightEntry.corner(darkDirection, oppositeDir(direction));
                            lightLevel0.add(new HighlightEntry(darkPos, HighlightColor.black(0.5f), darkFace, darkEdge));
                        } else {
                            var adjLight = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(darkPos);
                            if (adjLight == 0){
                                BlockPos adjPos = darkPos.relative(oppositeDir(direction));
                                BlockState adjState = level.getBlockState(adjPos);
                                boolean adjSolid = !HighlightMemory.isTransparent(level, adjState, adjPos, direction);
                                if (adjSolid) {
                                    HighlightFace adjFace = HighlightEntry.fromDirection(direction);
                                    if (adjFace == null) { System.err.println("null from direction: " + direction); continue; }
                                    var adjEdge = HighlightEntry.corner(oppositeDir(direction), oppositeDir(darkDirection));
                                    lightLevel0.add(new HighlightEntry(adjPos, HighlightColor.black(0.5f), adjFace, adjEdge));
                                }
                            }
                        }
                    }
                }
            }
        }

        return new LightEdges(lightLevel1, lightLevel0);
    }

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

    private static Direction @NotNull [] getAdjacentSides(Direction direction) {
        Direction[] sideDirs;
        switch (direction) {
            case UP:
            case DOWN: { sideDirs = new Direction[]{ Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST }; break; }
            case NORTH:
            case SOUTH: { sideDirs = new Direction[]{ Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST }; break; }
            case EAST:
            case WEST: { sideDirs = new Direction[]{ Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN }; break; }
            default: sideDirs = new Direction[]{};
        }
        return sideDirs;
    }
}
