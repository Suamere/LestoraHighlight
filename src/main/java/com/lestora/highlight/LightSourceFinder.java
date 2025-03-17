package com.lestora.highlight;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LightSourceFinder {
    static final Map<RLAmount, Integer> lightLevelsMap = new ConcurrentHashMap<>();

    static {
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "torch"), 0), 14);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "wall_torch"), 0), 14);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "glowstone"), 0), 15);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "redstone_torch"), 0), 7);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "sea_pickle"), 1), 6);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "sea_pickle"), 2), 9);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "sea_pickle"), 3), 12);
        lightLevelsMap.put(new RLAmount(ResourceLocation.fromNamespaceAndPath("minecraft", "sea_pickle"), 4), 15);
    }

    public static List<LightPos> findLightSourcesNearby(Level level, BlockPos center, int radius) {
        List<LightPos> lightPositions;
        int radiusSq = radius * radius;
        lightPositions = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        var blockState = level.getBlockState(pos);
                        for (var rlEntry : lightLevelsMap.entrySet()) {
                            if (blockSpecialReview(blockState, rlEntry.getKey())) {
                                lightPositions.add(new LightPos(pos, rlEntry.getValue()));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return lightPositions;
    }

    private static boolean blockSpecialReview(BlockState blockState, RLAmount rl) {
        var block = blockState.getBlock();
        if (block != rl.getBlockType()) return false;
        if (rl.getAmount() == 0) return true;

        // ToDo: Finish this for all "lit", count, and special cases
        if (block instanceof SeaPickleBlock && rl.getBlockType() == block) {
            if (blockState.getValue(BlockStateProperties.WATERLOGGED))
                return rl.getAmount() == blockState.getValue(SeaPickleBlock.PICKLES);
        }
        return false;
    }
}
