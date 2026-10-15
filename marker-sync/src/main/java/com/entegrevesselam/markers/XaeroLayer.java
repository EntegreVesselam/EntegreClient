package com.entegrevesselam.markers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.common.minimap.waypoints.Waypoint;
import java.util.*;
/** Exact Xaero26.6.0 API. Never accesses saved personal sets or saveWorld. */
final class XaeroLayer {
    private final MinecraftClient client;
    private ThirdPartyWaypoints layer;
    private final Ownership<ThirdPartyWaypoints> ownership = new Ownership<>();
    private final Map<String,Waypoint> owned = new HashMap<>();
    private boolean enabled;
    private Object sessionIdentity;
    private long sessionEpoch;
    XaeroLayer(MinecraftClient client) { this.client = client; }
    private void mainThread() { if (!client.isOnThread()) throw new IllegalStateException("main_thread_required"); }
    private MinimapSession session() { return BuiltInHudModules.MINIMAP.getCurrentSession(); }
    SyncState.Context context(String server, String dimension) {
        mainThread(); MinimapSession session = session();
        if (session != sessionIdentity) { hide(); sessionIdentity = session; sessionEpoch++; }
        if (session == null) return null;
        MinimapWorld world = session.getWorldManager().getAutoWorld();
        if (world == null || world.getDimId() == null || !world.getDimId().getValue().toString().equals(dimension)) return null;
        return new SyncState.Context(server,dimension,world.getFullPath().toString() + "#" + sessionEpoch);
    }
    void replace(SyncState.Context context, List<Snapshot.Marker> markers, String ownerId) {
        mainThread(); MinimapSession session = session();
        MinimapWorld world = session == null ? null : session.getWorldManager().getAutoWorld();
        if (world == null || !(world.getFullPath().toString() + "#" + sessionEpoch).equals(context.realm())) throw new IllegalStateException("world_changed");
        Identifier origin = Identifier.of("entegre_markers",ownerId);
        var manager = world.getContainer().getThirdPartyWaypointManager();
        ThirdPartyWaypoints existing = manager.getAll().stream().filter(p -> p.getOriginId().equals(origin)).findFirst().orElse(null);
        ThirdPartyWaypoints target = ownership.open(manager,existing,() -> manager.get(origin));
        Map<String,Waypoint> next = new LinkedHashMap<>();
        for (Snapshot.Marker m : markers) if (m.dimension().equals(context.dimension())) next.put(m.id(),waypoint(m));
        hide(); layer = target;
        if (!layer.isEmpty()) throw new IllegalStateException("ownership_collision");
        final MinimapSession expectedSession = session; final MinimapWorld expectedWorld = world;
        layer.setEnabledStateGetter(() -> enabled && client.world != null
            && expectedSession == session()
            && expectedSession.getWorldManager().getAutoWorld() == expectedWorld
            && expectedSession.getWorldManager().getCurrentWorld() == expectedWorld
            && client.world.getRegistryKey().getValue().toString().equals(context.dimension()));
        for (var entry : next.entrySet()) layer.add(entry.getKey(),entry.getValue());
        owned.putAll(next); enabled = true;
    }
    void hide() {
        mainThread(); enabled = false;
        if (layer != null) {
            Ownership.verifyContents(layer.getWaypoints(),owned);
            // clear() only clears this runtime map; remove(id) would save Xaero root config.
            layer.clear();
        }
        owned.clear(); layer = null;
    }
    static Waypoint waypoint(Snapshot.Marker marker) {
        Waypoint waypoint = new Waypoint(marker.x(),marker.y(),marker.z(),marker.name(),MarkerAppearance.initials(marker.name()),
            xaero.hud.minimap.waypoint.WaypointColor.fromIndex(MarkerAppearance.color(marker.id(),marker.color())));
        waypoint.setVisibility(xaero.hud.minimap.waypoint.WaypointVisibilityType.WORLD_MAP_LOCAL);
        return waypoint;
    }
}
