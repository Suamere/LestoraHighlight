package com.lestora.highlight.core;

import com.lestora.config.LestoraConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class PlayerHeldItem {
    public static int getHeldLightLevel(Player player) {
        var mainStack = player.getMainHandItem();
        if (!mainStack.isEmpty()) {
            ResourceLocation mainResource = ForgeRegistries.ITEMS.getKey(mainStack.getItem());
            if (mainResource != null) {
                int mainLight = LestoraConfig.getLightLevel(mainResource, 0);
                if (mainLight > 0) {
                    return mainLight;
                }
            }
        }

        var offStack = player.getOffhandItem();
        if (!offStack.isEmpty()) {
            ResourceLocation offResource = ForgeRegistries.ITEMS.getKey(offStack.getItem());
            if (offResource != null) {
                int offLight = LestoraConfig.getLightLevel(offResource, 0);
                if (offLight > 0) {
                    return offLight;
                }
            }
        }

        return 0;
    }
}