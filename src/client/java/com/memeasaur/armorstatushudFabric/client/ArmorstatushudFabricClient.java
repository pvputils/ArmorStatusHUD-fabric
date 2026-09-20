package com.memeasaur.armorstatushudFabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import org.slf4j.LoggerFactory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ArmorstatushudFabricClient implements ClientModInitializer {
    static HudConfig config = new HudConfig();
    private static boolean openConfig;
    @Override public void onInitializeClient() {
        try { config = HudConfig.load(); }
        catch (Exception e) { LoggerFactory.getLogger("armorstatushud").error("Cannot load armorstatushud.json; using defaults without overwriting the file", e); }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("armorstatushud-fabric", "equipment"), (graphics, delta) -> ArmorHud.render(graphics));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfig) { openConfig = false; client.gui.setScreen(new HudConfigScreen()); }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(literal("armorstatus")
            .executes(context -> { openConfig = true; return 1; })
            .then(literal("config").executes(context -> { openConfig = true; return 1; }))
            .then(literal("toggle").executes(context -> {
                HudConfig updated = config.copy(); updated.enabled = !updated.enabled;
                try { updated.save(); config = updated; context.getSource().sendFeedback(Component.literal("ArmorStatusHUD " + (config.enabled ? "enabled" : "disabled"))); return 1; }
                catch (Exception e) { context.getSource().sendError(Component.literal("Could not save ArmorStatusHUD config: " + e.getMessage())); return 0; }
            }))
            .then(literal("reload").executes(context -> {
                try { config = HudConfig.load(); context.getSource().sendFeedback(Component.literal("ArmorStatusHUD config reloaded")); return 1; }
                catch (Exception e) { context.getSource().sendError(Component.literal("Could not reload config: " + e.getMessage())); return 0; }
            }))));
    }
}

