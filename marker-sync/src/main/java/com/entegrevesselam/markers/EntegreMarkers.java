package com.entegrevesselam.markers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.util.*;
import java.util.concurrent.*;
public final class EntegreMarkers implements ClientModInitializer {
    private final SyncState state = new SyncState();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r,"entegre-markers-http"); t.setDaemon(true); return t; });
    private MinecraftClient client;
    private MarkerApi api;
    private DeviceStore store;
    private DeviceStore.Device device;
    private MarkerApi.Pairing pairing;
    private XaeroLayer layer;
    private Snapshot snapshot;
    private SyncState.Context context;
    private String status = "unpaired";
    private long pairEpoch;
    private PollSchedule pollSchedule;
    private boolean pairBusy, stopped, incompatible;
    @Override public void onInitializeClient() {
        client = MinecraftClient.getInstance(); api = new MarkerApi();
        store = new DeviceStore(FabricLoader.getInstance().getConfigDir()); device = store.load(System.currentTimeMillis());
        if (device != null) status = "waiting_server";
        if (!version("xaerominimap","26.6.0") || !version("xaeroworldmap","1.47.0")) { incompatible = true; status = "incompatible"; }
        else { try { layer = new XaeroLayer(client); } catch (LinkageError e) { incompatible = true; status = "incompatible"; } }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(literal("entegremarkers")
            .executes(c -> { message(status); return 1; })
            .then(literal("status").executes(c -> { message(status); return 1; }))
            .then(literal("pair").executes(c -> { pair(); return 1; }))
            .then(literal("forget").executes(c -> { forget(); return 1; }))));
        org.slf4j.LoggerFactory.getLogger("EntegreMarkers").info("Entegre Markers 0.1.1 initialized; client command registration callback installed; Xaero compatible={}", !incompatible);
        ClientTickEvents.END_CLIENT_TICK.register(ignored -> tick());
        ClientLifecycleEvents.CLIENT_STOPPING.register(ignored -> { stopped = true; pairEpoch++; state.close(); safeHide(); worker.shutdownNow(); });
    }
    private boolean version(String mod, String expected) { return FabricLoader.getInstance().getModContainer(mod).map(m -> expected.equals(m.getMetadata().getVersion().getFriendlyString())).orElse(false); }
    private void message(String key) { if (client.player != null) client.player.sendMessage(Text.translatable("entegre_markers."+key),false); }
    static boolean targetServer(String address) { return address != null && Set.of("entegrevesselam.com","entegrevesselam.com:25565").contains(address.toLowerCase(Locale.ROOT)); }
    private void safeHide() {
        if (layer == null) return;
        try { layer.hide(); } catch (RuntimeException | LinkageError e) { incompatible = true; status = "incompatible"; }
    }
    private void tick() {
        if (stopped || incompatible) return;
        long now = System.currentTimeMillis();
        try {
            var server = client.getCurrentServerEntry(); SyncState.Context next = null;
            if (client.world != null && client.getServer() == null && server != null && targetServer(server.address)) next = layer.context("entegrevesselam.com",client.world.getRegistryKey().getValue().toString());
            if (!Objects.equals(context,next)) {
                safeHide(); context = next; state.context(next);
                if (next == null) { snapshot = null; status = device == null ? "unpaired" : "waiting_server"; }
            }
            if (device != null && now >= device.expiresAt()) { device = null; state.resetDevice(); snapshot = null; safeHide(); status = "expired"; }
            if (pairing != null && now >= pairing.expiresAt()) { pairing = null; pairBusy = false; pairEpoch++; status = "expired"; }
            if (pairing != null && !pairBusy && pollSchedule.begin(now,client.isWindowFocused(),context != null)) poll(now);
            if (state.needsApply() && snapshot != null) { layer.replace(context,snapshot.markers(),device.ownerId()); state.applied(); }
            SyncState.Request request = state.begin(now,client.isWindowFocused(),device != null && pairing == null);
            if (request != null) {
                String token = device.token();
                worker.execute(() -> {
                    try {
                        MarkerApi.Response response = api.markers(token);
                        if (response.status() == 401 || response.status() == 403) { client.execute(() -> { state.denied(request); if (!state.visible()) { safeHide(); status = "denied"; } }); return; }
                        if (response.status() != 200) throw new IllegalArgumentException("http_status");
                        Snapshot received = Snapshot.parse(response.body());
                        client.execute(() -> { if (state.accept(request,received.revision())) { snapshot = received; status = "ready"; } });
                    } catch (Exception e) { client.execute(() -> { state.failed(request); if (!stopped) status = "offline"; }); }
                });
            }
        } catch (RuntimeException | LinkageError e) { safeHide(); incompatible = true; status = "incompatible"; }
    }
    private void pair() {
        if (incompatible) { message("incompatible"); return; }
        if (pairBusy || pairing != null) { message("pair_pending"); return; }
        if (context == null) { message("waiting_server"); return; }
        state.resetDevice(); snapshot = null; device = null; safeHide(); pairBusy = true;
        long epoch = ++pairEpoch; status = "pair_pending"; message(status);
        worker.execute(() -> {
            try {
                MarkerApi.Pairing received = api.start(System.currentTimeMillis());
                client.execute(() -> {
                    if (stopped || epoch != pairEpoch) return;
                    pairBusy = false; pairing = received; pollSchedule = new PollSchedule(System.currentTimeMillis(),received.expiresAt());
                    Util.getOperatingSystem().open(received.verificationUri()); message("browser_opened");
                });
            } catch (Exception e) { client.execute(() -> { if (epoch == pairEpoch) { pairBusy = false; status = "pair_failed"; message(status); } }); }
        });
    }
    private void poll(long now) {
        pairBusy = true; long epoch = pairEpoch; MarkerApi.Pairing expected = pairing;
        worker.execute(() -> {
            try {
                MarkerApi.Poll result = api.poll(expected,System.currentTimeMillis());
                client.execute(() -> {
                    if (stopped || epoch != pairEpoch) return;
                    pairBusy = false; pollSchedule.complete();
                    if (result.status().equals("expired")) { pairing = null; status = "expired"; message(status); }
                    else if (result.status().equals("approved")) {
                        if (System.currentTimeMillis() >= expected.expiresAt()) { pairing = null; status = "expired"; return; }
                        try { device = store.save(result.token(),result.expiresAt()); pairing = null; state.resetDevice(); status = "paired"; message(status); }
                        catch (Exception e) { pairing = null; status = "store_failed"; message(status); }
                    }
                });
            } catch (Exception e) { client.execute(() -> { if (epoch == pairEpoch) { pairBusy = false; pollSchedule.complete(); status = "pair_pending"; } }); }
        });
    }
    private void forget() {
        pairEpoch++; pairing = null; pairBusy = false; device = null; snapshot = null; state.resetDevice(); safeHide();
        try { store.delete(); status = "unpaired"; } catch (Exception e) { status = "store_failed"; }
        message(status);
    }
}
