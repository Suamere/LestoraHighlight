package com.lestora.highlight.core;

import com.lestora.config.LestoraConfig;
import com.lestora.highlight.models.LightConfig;
import com.lestora.highlight.models.LightPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.Collectors;

public class LightSourceFinder {

    public static List<LightPos> findLightSourcesNearby(Level level, BlockPos playerPos, int distance) {
        List<LightPos> lightPositions = new ArrayList<>();
        int radiusSq = distance * distance;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BFSNode> queue = new LinkedList<>();

        var configLights = LestoraConfig.getLightLevels().entrySet();
        if (LightConfig.torchesOnly) {
            configLights = configLights.stream()
                    .filter(entry -> {
                        var rl = entry.getKey().getResource();
                        String id = rl.toString();  // e.g. "minecraft:torch"
                        return id.equals("minecraft:torch") || id.equals("minecraft:lantern");
                    })
                    .collect(Collectors.toSet());
        }

        // Start at player's position.
        queue.add(new BFSNode(playerPos));
        visited.add(playerPos);

        while (!queue.isEmpty()) {
            BFSNode node = queue.poll();
            BlockPos currentPos = node.pos;

            // Check if current block is a light source.
            BlockState state = level.getBlockState(currentPos);
            for (var entry : configLights) {
                if (entry.getKey().stateMatches(state)) {
                    // Here we pass the resource identifier along with the light level.
                    // Adjust the LightPos constructor accordingly.
                    lightPositions.add(new LightPos(currentPos, entry.getValue(), entry.getKey().getResource()));
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

                int ndx = neighbor.getX() - playerPos.getX();
                int ndy = neighbor.getY() - playerPos.getY();
                int ndz = neighbor.getZ() - playerPos.getZ();
                if (ndx * ndx + ndy * ndy + ndz * ndz > radiusSq) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighbor);
                // Use the moving direction as passed in.
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
