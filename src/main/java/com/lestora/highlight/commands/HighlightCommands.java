package com.lestora.highlight.commands;

import com.lestora.highlight.core.HighlightEmitter;
import com.lestora.highlight.events.HighlightEvents;
import com.lestora.highlight.core.HighlightMemory;
import com.lestora.highlight.core.HighlightSphere;
import com.lestora.highlight.models.HighlightColor;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HighlightCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        var root = Commands.literal("lestora");

        registerHighlightRadius(root);
        registerTorchOn(root);
        registerLightRadius(root);
        registerShowAllOutlines(root);
        registerClearHighlights(root);

        event.getDispatcher().register(root);
    }

    private static void registerClearHighlights(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("clearHighlights")
                .executes(context -> {
                    HighlightMemory.clear();
                    return 1;
                })
        );
    }

    private static void registerTorchOn(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("alwaysShowBoundaries")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            HighlightEvents.alwaysOnEnabled = enabled;
                            if (enabled){
                                var player = Minecraft.getInstance().player;
                                HighlightEmitter.processLights(player.level(), player.blockPosition(), HighlightEvents.findLightRadius, HighlightEvents.showAllOutlines);
                            } else {
                                HighlightEmitter.removeLights();
                            }
                            return 1;
                        })
                )
        );
    }

    private static void registerLightRadius(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("lightSourceScanDistance")
                .then(Commands.argument("radius", IntegerArgumentType.integer(5, 100))
                        .executes(context -> {
                            HighlightEvents.findLightRadius = IntegerArgumentType.getInteger(context, "radius");
                            var player = Minecraft.getInstance().player;
                            if (HighlightEvents.alwaysOnEnabled)
                                HighlightEmitter.processLights(player.level(), player.blockPosition(), HighlightEvents.findLightRadius, HighlightEvents.showAllOutlines);
                            return 1;
                        })
                )
        );
    }

    private static void registerShowAllOutlines(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("meldBoundaries")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            HighlightEvents.showAllOutlines = !BoolArgumentType.getBool(context, "enabled");
                            var player = Minecraft.getInstance().player;
                            if (HighlightEvents.alwaysOnEnabled)
                                HighlightEmitter.processLights(player.level(), player.blockPosition(), HighlightEvents.findLightRadius, HighlightEvents.showAllOutlines);
                            return 1;
                        })
                )
        );
    }

    private static void registerHighlightRadius(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("createSphere")
                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0))
                        .executes(context -> {
                            double radius = DoubleArgumentType.getDouble(context, "radius");
                            double x = context.getSource().getPosition().x;
                            double y = context.getSource().getPosition().y;
                            double z = context.getSource().getPosition().z;

                            Level world = Minecraft.getInstance().level;
                            if (world == null) {
                                context.getSource().sendFailure(Component.literal("No client world available."));
                                return 0;
                            }

                            UUID userId = Minecraft.getInstance().player.getUUID();
                            HighlightSphere.setHighlightCenterAndRadius(userId, x, y, z, radius, HighlightColor.red(0.5f), world);
                            return 1;
                        })
                )
        );
    }
}