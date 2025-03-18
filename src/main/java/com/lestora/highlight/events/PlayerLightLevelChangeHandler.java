package com.lestora.highlight.events;

import com.lestora.highlight.core.HighlightEmitter;
import com.lestora.highlight.core.PlayerHeldItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerLightLevelChangeHandler {

    private static final Map<UUID, Integer> lastLightLevels = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        int currentLightLevel = PlayerHeldItem.getHeldLightLevel(player);
        UUID playerId = player.getUUID();

        Integer previousLevel = lastLightLevels.get(playerId);
        if (previousLevel == null || previousLevel != currentLightLevel) {
            lastLightLevels.put(playerId, currentLightLevel);
            HighlightEmitter.processLights(player);
        }
    }
}