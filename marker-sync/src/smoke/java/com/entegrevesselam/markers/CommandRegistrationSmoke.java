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
            var fixture = new Snapshot.Marker("synthetic-appearance","Çınar Kalesi","","world","minecraft:overworld",1,64,2,4,"poi",1);
            var waypoint = XaeroLayer.waypoint(fixture);
            if(!waypoint.getInitials().equals("ÇK")) throw new AssertionError("native name initials");
            if(waypoint.getVisibility()!=xaero.hud.minimap.waypoint.WaypointVisibilityType.WORLD_MAP_LOCAL) throw new AssertionError("native managed marker must hide from world HUD");
            if(waypoint.isDisabled()) throw new AssertionError("map marker must remain enabled");
            if(new xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderProvider().filter.test(waypoint)) throw new AssertionError("native HUD filter must hide managed point");
            if(new xaero.hud.minimap.waypoint.render.WaypointMapRenderProvider().filter.test(waypoint)) throw new AssertionError("world-map-only also intentionally hides minimap");
            var personal = new xaero.common.minimap.waypoints.Waypoint(1,64,2,"Çınar Kalesi","P",xaero.hud.minimap.waypoint.WaypointColor.DARK_RED);
            if(personal.getVisibility()!=xaero.hud.minimap.waypoint.WaypointVisibilityType.LOCAL || personal.getColor()!=4 || !personal.getInitials().equals("P")) throw new AssertionError("personal waypoint defaults must remain unchanged");
            if(!new xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderProvider().filter.test(personal)) throw new AssertionError("personal HUD eligibility preserved");

            if(waypoint.getColor()!=MarkerAppearance.color(fixture.id(),4)) throw new AssertionError("native automatic color");
            if(waypoint.getX()!=1 || waypoint.getY()!=64 || waypoint.getZ()!=2 || !waypoint.getName().equals(fixture.name())) throw new AssertionError("native canonical coordinates/name unchanged");
            org.slf4j.LoggerFactory.getLogger("EntegreMarkersSmoke").info("PASS native Xaero waypoint getters: Unicode initials, color, world-map-only visibility, enabled and canonical data retained");
            org.slf4j.LoggerFactory.getLogger("EntegreMarkersSmoke").info("PASS actual Fabric0.18.4/API0.141.3 event registered /entegremarkers pair/status/forget in Brigadier; disconnected client; no command executed");
        });
    }
}
