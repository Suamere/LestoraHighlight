package com.lestora.highlight.core;

import com.lestora.config.LestoraConfig;
import com.lestora.highlight.models.LightPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class LightSourceFinder {
//    public static List<LightPos> findLightSourcesNearby(Level level, BlockPos playerPos, int distance) {
//        List<LightPos> lightPositions;
//        int radiusSq = distance * distance;
//        lightPositions = new ArrayList<>();
//        for (int dx = -distance; dx <= distance; dx++) {
//            for (int dy = -distance; dy <= distance; dy++) {
//                for (int dz = -distance; dz <= distance; dz++) {
//                    if (dx * dx + dy * dy + dz * dz <= radiusSq) {
//                        BlockPos pos = playerPos.offset(dx, dy, dz);
//                        var blockState = level.getBlockState(pos);
//                        for (var rlEntry : LestoraConfig.getLightLevels().entrySet()) {
//                            if (blockSpecialReview(blockState, rlEntry.getKey())) {
//                                lightPositions.add(new LightPos(pos, rlEntry.getValue()));
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return lightPositions;
//    }

    public static List<LightPos> findLightSourcesNearby(Level level, BlockPos playerPos, int distance) {
        List<LightPos> lightPositions = new ArrayList<>();
        int radiusSq = distance * distance;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BFSNode> queue = new LinkedList<>();

        // Start at player's position.
        queue.add(new BFSNode(playerPos));
        visited.add(playerPos);

        while (!queue.isEmpty()) {
            BFSNode node = queue.poll();
            BlockPos currentPos = node.pos;

            // Check if current block is a light source.
            BlockState state = level.getBlockState(currentPos);
            for (var entry : LestoraConfig.getLightLevels().entrySet()) {
                if (entry.getKey().stateMatches(state)) {
                    lightPositions.add(new LightPos(currentPos, entry.getValue()));
                    break;
                }
            }

            // Only expand neighbors if within radius.
            int dx = currentPos.getX() - playerPos.getX();
            int dy = currentPos.getY() - playerPos.getY();
            int dz = currentPos.getZ() - playerPos.getZ();
            if (dx * dx + dy * dy + dz * dz >= radiusSq) {
                continue;
            }

            // Check each of the 6 cardinal directions.
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = currentPos.relative(dir);
                if (visited.contains(neighbor)) {
                    continue;
                }

                // Only consider neighbors within the sphere defined by distance.
                int ndx = neighbor.getX() - playerPos.getX();
                int ndy = neighbor.getY() - playerPos.getY();
                int ndz = neighbor.getZ() - playerPos.getZ();
                if (ndx * ndx + ndy * ndy + ndz * ndz > radiusSq) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighbor);
                // Use the moving direction as the one passed in.
                if (HighlightMemory.isTransparent(level, neighborState, neighbor, dir)) {
                    visited.add(neighbor);
                    queue.add(new BFSNode(neighbor));
                }
            }
        }

        return lightPositions;
    }

    private static class BFSNode {
        public final BlockPos pos;
        public BFSNode(BlockPos pos) {
            this.pos = pos;
        }
    }
}
