package com.lestora.highlight.events;

import com.lestora.config.LestoraConfig;
import com.lestora.highlight.core.HighlightEmitter;
import com.lestora.highlight.core.HighlightMemory;
import com.lestora.highlight.core.HighlightSphere;
import com.lestora.highlight.models.LightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class HighlightEvents {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Map<UUID, BlockPos> lastPlayerPositions = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Only process on END phase.
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        // Check every update interval (e.g., LightConfig.updateSeconds * 20 ticks).
        if (tickCounter % (LightConfig.updateSeconds * 20) == 0) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return; // Safety check if no player is present.

            BlockPos currentPos = player.blockPosition();
            BlockPos lastPos = lastPlayerPositions.get(player.getUUID());
            boolean shouldProcess = false;

            if (lastPos == null) {
                shouldProcess = true;
            } else {
                int dx = Math.abs(currentPos.getX() - lastPos.getX());
                int dy = Math.abs(currentPos.getY() - lastPos.getY());
                int dz = Math.abs(currentPos.getZ() - lastPos.getZ());
                // Process if the Manhattan distance is at least half the configured find-light radius.
                if (dx + dy + dz >= LightConfig.findLightRadius / 2) {
                    shouldProcess = true;
                }
            }

            if (shouldProcess) {
                lastPlayerPositions.put(player.getUUID(), currentPos);
                HighlightEmitter.processLights(player);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        handleBlockChange(event.getPlayer(), event.getState(), (level, highlightConfig) -> {
            highlightConfig.remove(event.getPos(), level);
        });
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        handleBlockChange(event.getEntity(), event.getState(), (level, highlightConfig) -> {
            highlightConfig.add(event.getPos(), level);
        });
    }

    private static void handleBlockChange(Entity plyr, BlockState state, BiConsumer<Level, HighlightSphere> action) {
        if (!(plyr instanceof Player player)) return;
        var playerUUID = player.getUUID();

        Block brokenBlock = state.getBlock();
        var processLights =  state.canOcclude() || LestoraConfig.getLightLevels().keySet().stream()
                .anyMatch(rlAmount -> rlAmount.getBlockType().equals(brokenBlock));

        HighlightSphere config = HighlightSphere.getUserHighlightConfig(playerUUID);
        var processHighlights = !(config == null || !HighlightMemory.hasHighlights());

        if (processLights || processHighlights) {
            scheduler.schedule(() -> {
                Minecraft.getInstance().execute(() -> {
                    var innerPlayer = Minecraft.getInstance().level.getPlayerByUUID(playerUUID);
                    if (processLights) HighlightEmitter.processLights(innerPlayer);
                    if (processHighlights) action.accept(Minecraft.getInstance().level, config);
                });
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    public static boolean isPlayerCrouchingKeyDown = false;
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() == Minecraft.getInstance().options.keyShift.getKey().getValue()) {
            var toggle = Minecraft.getInstance().options.toggleCrouch().get();
            var isDown = event.getAction() == GLFW.GLFW_PRESS;
            var isUp = event.getAction() == GLFW.GLFW_RELEASE;
            var isChanged = false;

            if (toggle && isDown) {
                isPlayerCrouchingKeyDown = !isPlayerCrouchingKeyDown;
                isChanged = true;
            }
            else if (!toggle) {
                if (isDown && !isPlayerCrouchingKeyDown) {
                    isPlayerCrouchingKeyDown = true;
                    isChanged = true;
                } else if (isUp && isPlayerCrouchingKeyDown) {
                    isPlayerCrouchingKeyDown = false;
                    isChanged = true;
                }
            }
            if (isChanged) {
                scheduler.schedule(() -> {
                    Minecraft.getInstance().execute(() -> {
                        HighlightEmitter.processLights(Minecraft.getInstance().player, true);
                    });
                }, 100, TimeUnit.MILLISECONDS);
            }
        }
    }
}