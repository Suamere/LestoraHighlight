package com.lestora.highlight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Suggestor {


    public static boolean isValidTorchPlacement(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());
        // Adjust the checks as needed – using isAir() and isSolid() as placeholders.
        return state.isAir() && belowState.isSolid();
    }

    // Calculate Manhattan distance between two BlockPos (all three dimensions).
    public static int manhattanDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX())
                + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Returns two suggested torch positions (left and right) given two initial torches,
     * ensuring that only valid placements (air with solid beneath) are considered.
     *
     * Conditions:
     *  - Candidate must be within 25 Manhattan distance from both torches.
     *  - Candidate must be within 13 Manhattan distance from the "dark spot" between torches.
     *  - Among valid candidates, the one that maximizes (d1+d2) is preferred (lighting new area),
     *    with tie-breaker based on closeness to the midpoint.
     *
     * @param t1 first torch position
     * @param t2 second torch position
     * @param level the world level for block state queries
     * @return an array of two BlockPos: [leftCandidate, rightCandidate] (each may be null if not found)
     */
    public static BlockPos[] suggestThirdTorches(BlockPos t1, BlockPos t2, Level level) {
        // Compute midpoint
        int midX = (t1.getX() + t2.getX()) / 2;
        int midY = (t1.getY() + t2.getY()) / 2;
        int midZ = (t1.getZ() + t2.getZ()) / 2;
        BlockPos midpoint = new BlockPos(midX, midY, midZ);

        // Define search bounds: extend around the two torches.
        int margin = 25;
        int minX = Math.min(t1.getX(), t2.getX()) - margin;
        int maxX = Math.max(t1.getX(), t2.getX()) + margin;
        int minY = Math.min(t1.getY(), t2.getY()) - margin;
        int maxY = Math.max(t1.getY(), t2.getY()) + margin;
        int minZ = Math.min(t1.getZ(), t2.getZ()) - margin;
        int maxZ = Math.max(t1.getZ(), t2.getZ()) + margin;

        // Pass 1: Find the "dark spot" between the two torches.
        // Light from a torch at Manhattan distance d is: max(14 - d, 0)
        int bestEffective = Integer.MAX_VALUE;
        BlockPos darkSpot = null;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!isValidTorchPlacement(level, pos)) continue;

                    int d1 = manhattanDistance(pos, t1);
                    int d2 = manhattanDistance(pos, t2);
                    int l1 = Math.max(14 - d1, 0);
                    int l2 = Math.max(14 - d2, 0);
                    int effective = Math.max(l1, l2);

                    if (effective < bestEffective) {
                        bestEffective = effective;
                        darkSpot = pos;
                    } else if (effective == bestEffective && darkSpot != null) {
                        if (manhattanDistance(pos, midpoint) < manhattanDistance(darkSpot, midpoint)) {
                            darkSpot = pos;
                        }
                    }
                }
            }
        }

        if (darkSpot == null) {
            return new BlockPos[] { null, null };
        }

        // Pre-compute the 2D vector for the line from t1 to t2 (x, z only)
        int diffX = t2.getX() - t1.getX();
        int diffZ = t2.getZ() - t1.getZ();

        // Pass 2: Find candidate positions for the third torches.
        BlockPos bestLeft = null;
        int bestLeftCoverage = -1;
        BlockPos bestRight = null;
        int bestRightCoverage = -1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!isValidTorchPlacement(level, pos)) continue;

                    int d1 = manhattanDistance(pos, t1);
                    int d2 = manhattanDistance(pos, t2);
                    if (d1 > 25 || d2 > 25) continue;

                    if (manhattanDistance(pos, darkSpot) > 13) continue;

                    int coverage = d1 + d2;

                    // Determine left/right using cross product (2D, using x and z).
                    int candDX = pos.getX() - darkSpot.getX();
                    int candDZ = pos.getZ() - darkSpot.getZ();
                    int cross = diffX * candDZ - diffZ * candDX;

                    // For left side (cross > 0)
                    if (cross > 0) {
                        if (coverage > bestLeftCoverage) {
                            bestLeftCoverage = coverage;
                            bestLeft = pos;
                        } else if (coverage == bestLeftCoverage && bestLeft != null) {
                            if (manhattanDistance(pos, midpoint) < manhattanDistance(bestLeft, midpoint)) {
                                bestLeft = pos;
                            }
                        }
                    }
                    // For right side (cross < 0)
                    else if (cross < 0) {
                        if (coverage > bestRightCoverage) {
                            bestRightCoverage = coverage;
                            bestRight = pos;
                        } else if (coverage == bestRightCoverage && bestRight != null) {
                            if (manhattanDistance(pos, midpoint) < manhattanDistance(bestRight, midpoint)) {
                                bestRight = pos;
                            }
                        }
                    }
                }
            }
        }

        return new BlockPos[] { bestLeft, bestRight };
    }
}