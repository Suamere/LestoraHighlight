package com.lestora.highlight.core;

import com.lestora.highlight.models.HighlightColor;
import com.lestora.highlight.models.LightPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HighlightEmitter {
    private static final Set<UUID> lightUUIDs = ConcurrentHashMap.newKeySet();

    public static void processLights(Level level, BlockPos playerPos, int radius, boolean showAllOutlines) {
        if (level == null) return;
        var heldItemLightLevel = 14; // ToDo: When calling processLights, pass in the held item light level.  If 0, assume 14 for Torch
        removeLights();
        List<LightPos> lightPositions = LightSourceFinder.findLightSourcesNearby2(level, playerPos, radius);
        for (var lightPos : lightPositions) {
            UUID lightUUID = UUID.nameUUIDFromBytes(
                    ("lightSource:" + lightPos.getBlockPos().getX() + ":" + lightPos.getBlockPos().getY() + ":" + lightPos.getBlockPos().getZ())
                            .getBytes(StandardCharsets.UTF_8)
            );
            lightUUIDs.add(lightUUID);
            var edges = LightEdgesProcessor.findLightEdges(lightPos.getBlockPos(), lightPos.getAmount(), level, showAllOutlines);
            for (var ll0 : edges.LightLevel0) {
                HighlightMemory.add(lightUUID, ll0);
            }
            for (var ll1 : edges.LightLevel1) {
                HighlightMemory.add(lightUUID, ll1);
            }

            var radius2 = ((lightPos.getAmount() - 1) * 2) - 1;
            var n25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.NORTH);
            var s25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.SOUTH);
            var e25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.EAST);
            var w25 = TorchBFS.bfsBestCandidateSingleDirection(lightPos.getBlockPos(), level, radius2, Direction.WEST);
            List<BlockPos> candidates = new ArrayList<>();
            if (n25 != null) candidates.add(n25);
            if (s25 != null) candidates.add(s25);
            if (e25 != null) candidates.add(e25);
            if (w25 != null) candidates.add(w25);
            for (BlockPos candidate : candidates) {
                UUID uuid = UUID.nameUUIDFromBytes(("suggestion:" + candidate.getX() + ":" + candidate.getY() + ":" + candidate.getZ()).getBytes(StandardCharsets.UTF_8));
                HighlightSphere.setHighlightCenterAndRadius(uuid, candidate.getX(), candidate.getY(), candidate.getZ(), 0, HighlightColor.blue(0.5f), level);
            }
        }

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
            UUID uuid = UUID.nameUUIDFromBytes(
                    ("suggestion:" + suggestion.getX() + ":" + suggestion.getY() + ":" + suggestion.getZ())
                            .getBytes(StandardCharsets.UTF_8)
            );
            HighlightSphere.setHighlightCenterAndRadius(uuid, suggestion.getX(), suggestion.getY(), suggestion.getZ(), 0, HighlightColor.green(0.5f), level);
        }
    }

    public static void removeLights() {
        for (UUID lightUUID : lightUUIDs)
            HighlightMemory.clear(lightUUID);

        lightUUIDs.clear();
    }
}