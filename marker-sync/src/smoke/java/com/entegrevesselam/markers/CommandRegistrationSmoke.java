package com.entegrevesselam.markers;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
/** Development-only probe: no account, world, server, command execution or HTTP request. */
public final class CommandRegistrationSmoke implements ClientModInitializer {
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) throw new IllegalStateException("development_only");
            if (client.world != null || client.getNetworkHandler() != null) throw new AssertionError("smoke requires disconnected title screen");
            CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
            var registry = CommandRegistryAccess.of(DynamicRegistryManager.EMPTY,FeatureFlags.DEFAULT_ENABLED_FEATURES);
            ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher,registry);
            var root = dispatcher.getRoot().getChild("entegremarkers");
            if (root == null || root.getCommand() == null) throw new AssertionError("missing entegremarkers command");
            for (String name : new String[]{"pair","status","forget"}) {
                var child = root.getChild(name);
                if (child == null || child.getCommand() == null) throw new AssertionError("missing subcommand " + name);
            }
            org.slf4j.LoggerFactory.getLogger("EntegreMarkersSmoke").info("PASS actual Fabric0.18.4/API0.141.3 event registered /entegremarkers pair/status/forget in Brigadier; disconnected client; no command executed");
        });
    }
}
