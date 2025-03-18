package com.lestora.highlight.events;

import com.lestora.highlight.core.HighlightEmitter;
import com.lestora.highlight.core.HighlightMemory;
import com.lestora.highlight.core.HighlightSphere;
import com.lestora.highlight.models.LightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
    // Tick counter to measure 2-second intervals (2 seconds = 40 ticks at 20 ticks per second).
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            // Every 40 ticks (about 2 seconds)
            if (tickCounter % (LightConfig.updateSeconds * 20) == 0) {
                // Iterate over all server players
                for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                    BlockPos currentPos = player.blockPosition();
                    BlockPos lastPos = lastPlayerPositions.get(player.getUUID());
                    // If the player's block position has changed since the last check...
                    if (lastPos == null || !lastPos.equals(currentPos)) {
                        lastPlayerPositions.put(player.getUUID(), currentPos);
                        HighlightEmitter.processLights(player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        handleBlockChange((level, highlightConfig) -> {
            highlightConfig.remove(event.getPos(), level);
        });
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        handleBlockChange((level, highlightConfig) -> {
            highlightConfig.add(event.getPos(), level);
        });
    }

    private static void handleBlockChange(BiConsumer<Level, HighlightSphere> action) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        HighlightEmitter.processLights(player);
        HighlightSphere config = HighlightSphere.getUserHighlightConfig(player.getUUID());
        if (config == null || !HighlightMemory.hasHighlights()) return;

        scheduler.schedule(() -> {
            Minecraft.getInstance().execute(() -> action.accept(level, config));
        }, 200, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() == Minecraft.getInstance().options.keyShift.getKey().getValue()) {
            var toggle = Minecraft.getInstance().options.toggleCrouch().get();
            var isDown = event.getAction() == GLFW.GLFW_PRESS;
            var isUp = event.getAction() == GLFW.GLFW_RELEASE;
            var isChanged = false;

            if (toggle && isDown) {
                LightConfig.isPlayerCrouchingKeyDown = !LightConfig.isPlayerCrouchingKeyDown;
                isChanged = true;
            }
            else if (!toggle) {
                if (isDown && !LightConfig.isPlayerCrouchingKeyDown) {
                    LightConfig.isPlayerCrouchingKeyDown = true;
                    isChanged = true;
                } else if (isUp && LightConfig.isPlayerCrouchingKeyDown) {
                    LightConfig.isPlayerCrouchingKeyDown = false;
                    isChanged = true;
                }
            }
            if (isChanged) {
                scheduler.schedule(() -> {
                    Minecraft.getInstance().execute(() -> {
                        HighlightEmitter.processLights(Minecraft.getInstance().player);
                    });
                }, 100, TimeUnit.MILLISECONDS);
            }
        }
    }
}