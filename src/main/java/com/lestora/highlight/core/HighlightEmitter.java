package com.lestora.highlight.core;

import com.lestora.highlight.helpers.HighlightEntry;
import com.lestora.highlight.models.HighlightColor;
import com.lestora.highlight.models.LightConfig;
import com.lestora.highlight.models.LightPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HighlightEmitter {
    private static final Set<UUID> lightUUIDs = ConcurrentHashMap.newKeySet();

    public static void processLights(Player player, boolean fromCrouch) {
        if (fromCrouch && player.isCrouching())
            processLights(player);
    }

    public static void processLights(Player player) {
        removeLights();

        if (player.isCrouching()) {
            if (!LightConfig.showWhenCrouching) return;
        } else if (!LightConfig.showWhenStanding) return;

        var level = player.level();
        if (level == null) return;

        var playerPos = player.blockPosition();

        var heldItemLightLevel = PlayerHeldItem.getHeldLightLevel(player);
        if (heldItemLightLevel <= 0) heldItemLightLevel = 14;

        List<LightPos> lightPositions = LightSourceFinder.findLightSourcesNearby(level, playerPos, LightConfig.findLightRadius);
// First, define the priority order in a map.
// Lower numbers = higher priority.
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("minecraft:torch", 1);
        priorityMap.put("minecraft:lantern", 2);
        priorityMap.put("minecraft:sea_lantern", 3);
        priorityMap.put("minecraft:shroomlight", 4);
        priorityMap.put("minecraft:soul_lantern", 5);
        priorityMap.put("minecraft:ochre_froglight", 6);
        priorityMap.put("minecraft:pearlescent_froglight", 7);
        priorityMap.put("minecraft:verdant_froglight", 8);
        priorityMap.put("minecraft:campfire", 9);
        priorityMap.put("minecraft:candle", 10);
        priorityMap.put("minecraft:end_rod", 11);
        priorityMap.put("minecraft:fire", 12);
        priorityMap.put("minecraft:soul_torch", 13);
        priorityMap.put("minecraft:soul_fire", 14);
        priorityMap.put("minecraft:redstone_torch", 15);

// Sort the list based on the resource priority.
// For resources not found in the map, assign a default high value (e.g. 100).
        lightPositions.sort(Comparator.comparingInt(lp -> {
            String resStr = lp.getResource().toString(); // assuming LightPos has getResource()
            return priorityMap.getOrDefault(resStr, 100);
        }));

        var limitCandidates = 0;
        List<BlockPos> candidates = new ArrayList<>();
        for (var lightPos : lightPositions) {
            UUID lightUUID = UUID.nameUUIDFromBytes(
                    ("lightSource:" + lightPos.getBlockPos().getX() + ":" + lightPos.getBlockPos().getY() + ":" + lightPos.getBlockPos().getZ())
                            .getBytes(StandardCharsets.UTF_8)
            );
            lightUUIDs.add(lightUUID);
            var edges = LightEdgesProcessor.findLightEdges(lightPos.getBlockPos(), lightPos.getAmount(), level, LightConfig.showAllOutlines);
            for (var ll0 : edges.LightLevel0) { HighlightMemory.add(lightUUID, ll0); }
            for (var ll1 : edges.LightLevel1) { HighlightMemory.add(lightUUID, ll1); }

            if (limitCandidates < 10) {
                var radius2 = ((lightPos.getAmount() - 1) * 2) - 1;
                var n25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.NORTH);
                var s25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.SOUTH);
                var e25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.EAST);
                var w25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.WEST);
                if (n25 != null) candidates.add(n25);
                if (s25 != null) candidates.add(s25);
                if (e25 != null) candidates.add(e25);
                if (w25 != null) candidates.add(w25);
                limitCandidates++;
            }
        }

        Map<UUID, HighlightEntry> highlightedPositions = new HashMap<>();
        for (BlockPos candidate : candidates) {
            int light = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(candidate);
            if (light != 0) continue;

            if (hasNearbyLight(level, candidate, 2)) continue;

            UUID lightUUID2 = UUID.nameUUIDFromBytes(("radial:" + candidate.getX() + ":" + candidate.getY() + ":" + candidate.getZ()).getBytes(StandardCharsets.UTF_8));
            lightUUIDs.add(lightUUID2);
            highlightedPositions.put(lightUUID2, HighlightEntry.Whole(candidate, HighlightColor.blue(0.5f)));
        }


        if (lightPositions.size() > 10) { lightPositions = lightPositions.subList(0, 10); }
        Set<BlockPos> suggestionSet = new HashSet<>();
        if (lightPositions.size() >= 2) {
            for (int i = 0; i < lightPositions.size(); i++) {
                for (int j = i + 1; j < lightPositions.size(); j++) {
                    BlockPos[] suggestions = Suggestor.suggestThirdTorches(
                            lightPositions.get(i).getBlockPos(),
                            lightPositions.get(j).getBlockPos(),
                            lightPositions.get(i).getAmount(),
                            lightPositions.get(j).getAmount(),
                            heldItemLightLevel,
                            level
                    );
                    if (suggestions[0] != null) {
                        suggestionSet.add(suggestions[0]);
                    }
                    if (suggestions[1] != null) {
                        suggestionSet.add(suggestions[1]);
                    }
                }
            }
        }

        for (BlockPos suggestion : suggestionSet) {
            UUID lightUUID = UUID.nameUUIDFromBytes(
                    ("suggestion:" + suggestion.getX() + ":" + suggestion.getY() + ":" + suggestion.getZ())
                            .getBytes(StandardCharsets.UTF_8)
            );
            lightUUIDs.add(lightUUID);
            highlightedPositions.put(lightUUID, HighlightEntry.Whole(suggestion, HighlightColor.green(0.5f)));
        }

        for (var highlight : highlightedPositions.entrySet()) {
            HighlightMemory.add(highlight.getKey(), highlight.getValue());
        }
    }

    private static boolean hasNearbyLight(Level level, BlockPos pos, int threshold) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= threshold) {
                        BlockPos neighbor = pos.offset(dx, dy, dz);
                        int neighborLight = level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(neighbor);
                        if (neighborLight != 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void removeLights() {
        for (UUID lightUUID : lightUUIDs)
            HighlightMemory.clear(lightUUID);

        lightUUIDs.clear();
    }
}