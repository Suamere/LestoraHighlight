package com.lestora.highlight.core;

import com.lestora.highlight.helpers.HasTorchSupport;
import com.lestora.highlight.models.AxisNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.*;

public class TorchBFS {
    /**
     * Runs a BFS for a single horizontal direction (plus vertical moves) from the start.
     * Allowed moves: UP, DOWN, and the given horizontalDir.
     * For each branch (determined by the very first move from start), we record any valid node
     * (with support) that reaches at least fallbackMin = ceil(targetDistance/2). Then we group
     * candidates by branch and finally choose the candidate with the highest Manhattan distance.
     * In case of ties, the candidate whose Y coordinate is closest to the start's Y level is preferred.
     *
     * @param start          Starting BlockPos.
     * @param level          The world Level.
     * @param targetDistance The desired Manhattan distance (e.g. 25).
     * @param horizontalDir  The single horizontal Direction (e.g. NORTH or SOUTH).
     * @return A single candidate BlockPos, or null if none found.
     */
    public static BlockPos bfsBestCandidateSingleDirection(BlockPos start, Level level, int targetDistance, Direction horizontalDir) {
        // fallbackMin is ceil(targetDistance/2)
        int fallbackMin = (targetDistance + 1) / 2;
        // Map to hold the best candidate for each branch (branch key = first move from start).
        Map<BlockPos, AxisNode> branchCandidates = new HashMap<>();

        Set<BlockPos> visited = new HashSet<>();
        Queue<AxisNode> queue = new LinkedList<>();

        AxisNode startNode = new AxisNode(start, 0, null);
        queue.add(startNode);
        visited.add(start);

        // Allowed moves: UP, DOWN, and the specified horizontalDir.
        List<Direction> moves = new ArrayList<>();
        moves.add(Direction.UP);
        moves.add(Direction.DOWN);
        moves.add(horizontalDir);

        while (!queue.isEmpty()) {
            AxisNode node = queue.poll();
            int dist = node.distance;
            if (dist > targetDistance) continue; // Do not expand further if past targetDistance.

            // Record any node that has support and is at least fallbackMin distance.
            if (dist >= fallbackMin && HasTorchSupport.hasSupport(level, node.pos)) {
                // Determine branch key: the first move from start.
                BlockPos branchKey = getBranchKey(node, start);
                // For each branch, keep the candidate with the highest distance.
                AxisNode existing = branchCandidates.get(branchKey);
                if (existing == null || node.distance > existing.distance) {
                    branchCandidates.put(branchKey, node);
                } else if (node.distance == existing.distance) {
                    // Tie-breaker: choose the candidate with Y level closest to the start.
                    int diffNew = Math.abs(node.pos.getY() - start.getY());
                    int diffExisting = Math.abs(existing.pos.getY() - start.getY());
                    if (diffNew < diffExisting) {
                        branchCandidates.put(branchKey, node);
                    }
                }
            }

            // Expand neighbors if we haven't reached targetDistance yet.
            if (dist < targetDistance) {
                for (Direction move : moves) {
                    BlockPos next = node.pos.relative(move);
                    if (!visited.contains(next)) {
                        // Traverse only if the block is transparent.
                        if (HighlightMemory.isTransparent(level, level.getBlockState(next), next, move)) {
                            AxisNode newNode = new AxisNode(next, dist + 1, node);
                            visited.add(next);
                            queue.add(newNode);
                        }
                    }
                }
            }
        }

        // Now choose the best candidate among all branch candidates:
        // The one with the highest Manhattan distance, tie-broken by Y difference from start.
        AxisNode bestCandidate = null;
        for (AxisNode candidate : branchCandidates.values()) {
            if (bestCandidate == null) {
                bestCandidate = candidate;
            } else if (candidate.distance > bestCandidate.distance) {
                bestCandidate = candidate;
            } else if (candidate.distance == bestCandidate.distance) {
                int diffCandidate = Math.abs(candidate.pos.getY() - start.getY());
                int diffBest = Math.abs(bestCandidate.pos.getY() - start.getY());
                if (diffCandidate < diffBest) {
                    bestCandidate = candidate;
                }
            }
        }
        return bestCandidate == null ? null : bestCandidate.pos;
    }

    /**
     * Traverses the parent chain to find the branch key: the immediate child of start.
     *
     * @param node  The current node.
     * @param start The starting BlockPos.
     * @return The branch key BlockPos.
     */
    private static BlockPos getBranchKey(AxisNode node, BlockPos start) {
        AxisNode current = node;
        while (current.parent != null && !current.parent.pos.equals(start)) {
            current = current.parent;
        }
        return current.pos; // This is the immediate child of start.
    }
}
