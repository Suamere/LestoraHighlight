package com.lestora.highlight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HighlightMemory {
    // Map each group (UUID) to a thread-safe list of highlighted block entries.
    private final static Map<UUID, CopyOnWriteArrayList<HighlightEntry>> highlightedPositions = new ConcurrentHashMap<>();

    // Returns true if any group has at least one highlighted block.
    public static Boolean hasHighlights() {
        return highlightedPositions.values().stream().anyMatch(list -> !list.isEmpty());
    }

    // Returns a combined list of all highlighted entries across groups.
    public static List<HighlightEntry> getHighlightedPositions() {
        List<HighlightEntry> all = new ArrayList<>();
        for (List<HighlightEntry> list : highlightedPositions.values()) {
            all.addAll(list);
        }
        return all;
    }

    // Returns true if the given group contains an entry for the specified block position.
    public static boolean contains(UUID groupID, BlockPos placedPos) {
        List<HighlightEntry> list = highlightedPositions.get(groupID);
        if (list != null) {
            for (HighlightEntry entry : list) {
                if (entry.pos.equals(placedPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Adds a block position to the highlights for the given group along with its HighlightColor.
    public static void add(UUID groupID, HighlightEntry highlight) {
        highlightedPositions
                .computeIfAbsent(groupID, k -> new CopyOnWriteArrayList<>())
                .add(highlight);
    }

    // Removes a block position from the highlights for the given group.
    public static void remove(UUID groupID, BlockPos placedPos) {
        List<HighlightEntry> list = highlightedPositions.get(groupID);
        if (list != null) {
            list.removeIf(entry -> entry.pos.equals(placedPos));
        }
    }

    public static void clear(UUID groupID) {
        highlightedPositions.remove(groupID);
    }

    public static void clear() {
        highlightedPositions.clear();
        HighlightSphere.userConfigs.clear();
    }

    public static boolean isBlockExposed(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);
        if (!state.canOcclude()) return false;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!neighborState.canOcclude() || neighborState.getFluidState().is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTransparent(Level level, BlockState blockState, BlockPos pos, Direction dir) {
        var block = blockState.getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR) return true;
        if (block == Blocks.WATER) return true;

        if (block instanceof DoorBlock) return true;
        if (block == Blocks.TINTED_GLASS) return false;
        if (block == Blocks.GLASS) return true;
        if (block == Blocks.GLASS_PANE) return true;

        if (dir != null) return !(blockState.isFaceSturdy(level, pos, dir) || blockState.isFaceSturdy(level, pos, HighlightEmitterHelper.oppositeDir(dir)));
        return false;
    }

    public static boolean isBlockSturdy(Level level, BlockPos pos, Direction dir) {
        BlockState standingBlock = level.getBlockState(pos);
        if (!standingBlock.canOcclude()) return false;
        if (!standingBlock.isFaceSturdy(level, pos, dir)) return false;
        return true;
    }

    public static boolean canMobSpawnOn(Level level, BlockPos pos, Direction dir) {
        if (!isBlockSturdy(level, pos, dir)) return false;
        BlockState above = level.getBlockState(pos.above());
        BlockState above2 = level.getBlockState(pos.above(2));
        return above.isAir() && above2.isAir();
    }
}
