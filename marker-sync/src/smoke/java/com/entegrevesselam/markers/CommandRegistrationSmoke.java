package com.entegrevesselam.markers;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.network.chat.ClickEvent;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
/** Development-only native API/command probe. All bridge HTTP uses synthetic in-memory responses. */
public final class CommandRegistrationSmoke implements ClientModInitializer {
    private final CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
    private final AtomicInteger starts = new AtomicInteger(), reads = new AtomicInteger();
    private EntegreMarkers bridge;
    private SyncState state;
    private String invitationCommand;
    private int stage, ticks;
    private boolean ready;
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) throw new IllegalStateException("development_only");
            if (client.level != null || client.getConnection() != null) throw new AssertionError("smoke requires disconnected title screen");
            var registry = CommandBuildContext.simple(RegistryAccess.EMPTY,FeatureFlags.DEFAULT_FLAGS);
            ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher,registry);
            var root = dispatcher.getRoot().getChild("entegremarkers");
            if (root == null || root.getCommand() == null) throw new AssertionError("missing entegremarkers command");
            for (String name : new String[]{"pair","sync","status","forget"}) {
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
            for(int color=0;color<16;color++) {
                var colored = new Snapshot.Marker("synthetic-"+color,"Renk","","world","minecraft:overworld",1,64,2,color,"poi",1);
                if(XaeroLayer.waypoint(colored).getColor()!=color) throw new AssertionError("native site color must be preserved: " + color);
            }
            if(waypoint.getX()!=1 || waypoint.getY()!=64 || waypoint.getZ()!=2 || !waypoint.getName().equals(fixture.name())) throw new AssertionError("native canonical coordinates/name unchanged");
            log("PASS native Xaero: all 16 exact colors, initials, world-map-only filters, personal defaults and coordinates");
            invitationCommand = EntegreMarkers.pairingInvitation().getSiblings().stream()
                .map(t -> t.getStyle().getClickEvent()).filter(c -> c instanceof ClickEvent.RunCommand)
                .map(c -> ((ClickEvent.RunCommand)c).command()).findFirst().orElseThrow(() -> new AssertionError("missing native RUN_COMMAND invitation"));
            if(!invitationCommand.equals("/entegremarkers pair")) throw new AssertionError("invitation target must be local pairing command");
            bridge = FabricLoader.getInstance().getEntrypoints("client",ClientModInitializer.class).stream()
                .filter(e -> e instanceof EntegreMarkers).map(e -> (EntegreMarkers)e).findFirst().orElseThrow();
            state = (SyncState)get("state");
            set("api",new MarkerApi(request -> {
                if(!request.uri().getHost().equals("entegrevesselam.com")) throw new AssertionError("fixed host");
                switch(request.uri().getPath()) {
                    case "/api/map/devices/start" -> { starts.incrementAndGet(); return new MarkerApi.Response(503,"{}".getBytes(StandardCharsets.UTF_8)); }
                    case "/api/client/markers" -> { reads.incrementAndGet(); return new MarkerApi.Response(200,"{\"schemaVersion\":1,\"revision\":7,\"updatedAt\":\"2026-10-18T00:00:00Z\",\"markers\":[]}".getBytes(StandardCharsets.UTF_8)); }
                    default -> throw new AssertionError("unexpected HTTP path");
                }
            }));
            ready = true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }
    private void tick(Minecraft client) {
        if(!ready) return;
        if(client.level!=null || client.getConnection()!=null) throw new AssertionError("native probe must remain disconnected");
        if(++ticks>400) throw new AssertionError("native command probe timed out");
        try {
            switch(stage) {
                case 0 -> {
                    run("entegremarkers");run("entegremarkers sync");run("entegremarkers status");
                    if(starts.get()!=0 || reads.get()!=0) throw new AssertionError("outside-target command must not fetch");
                    syntheticContext();run(invitationCommand.substring(1));run("entegremarkers");stage=1;
                }
                case 1 -> {
                    if(starts.get()!=1 || (boolean)get("pairBusy")) return;
                    syntheticContext();run("entegremarkers");stage=2;
                }
                case 2 -> {
                    if(starts.get()!=2 || (boolean)get("pairBusy")) return;
                    syntheticContext();set("device",new DeviceStore.Device("synthetic-token-only",System.currentTimeMillis()+86400000,"00000000-0000-0000-0000-000000000001"));
                    setFocus(client,false);run("entegremarkers sync");
                    if(reads.get()!=0) throw new AssertionError("unfocused manual command must not fetch");
                    setFocus(client,true);run("entegremarkers sync");run("entegremarkers");stage=3;
                }
                case 3 -> {
                    if(reads.get()!=1 || state.syncing()) return;
                    syntheticContext();run("entegremarkers sync");run("entegremarkers");run("entegremarkers status");
                    if(reads.get()!=1 || starts.get()!=2) throw new AssertionError("manual commands must preserve one-in-flight/60-second limits");
                    run("entegremarkers forget");
                    if(get("device")!=null || state.visible()) throw new AssertionError("forget must clear local device and visibility");
                    log("PASS native commands: local clickable pair, bare unpaired pair, paired sync, pending/target/foreground/60-second guards, status and forget");
                    log("DONE synthetic transport only; no browser, account, world, live server or real waypoint data");
                    ready=false;client.stop();
                }
                default -> throw new AssertionError("unknown stage");
            }
        } catch(Exception e) { throw new AssertionError("native command probe",e); }
    }
    // SDL stores focus on Window in 26.3; this probe changes only its isolated fixture.
    private void setFocus(Minecraft client, boolean focused) throws Exception {
        Field field = client.getWindow().getClass().getDeclaredField("focused");
        field.setAccessible(true); field.setBoolean(client.getWindow(),focused);
    }
    private void syntheticContext() {
        var context=new SyncState.Context("entegrevesselam.com","minecraft:overworld","synthetic-native");
        set("context",context);state.context(context);
    }
    private void run(String command) throws Exception { if(dispatcher.execute(command,null)!=1) throw new AssertionError("command failed"); }
    private Field field(String name) { try {Field f=EntegreMarkers.class.getDeclaredField(name);f.setAccessible(true);return f;}catch(Exception e){throw new AssertionError(e);} }
    private Object get(String name) {try{return field(name).get(bridge);}catch(Exception e){throw new AssertionError(e);} }
    private void set(String name,Object value) {try{field(name).set(bridge,value);}catch(Exception e){throw new AssertionError(e);} }
    private static void log(String text) { org.slf4j.LoggerFactory.getLogger("EntegreMarkersSmoke").info(text); }
}
