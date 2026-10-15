package com.entegrevesselam.markers;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;
import com.google.gson.*;
public final class ContractTests {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SyncState state = new SyncState();
        var overworld = new SyncState.Context("entegrevesselam.com", "minecraft:overworld", "realm/A");
        var nether = new SyncState.Context("entegrevesselam.com", "minecraft:the_nether", "realm/A");
        state.context(overworld);
        var request = state.begin(0, true, true);
        check(request != null, "first request starts");
        check(state.begin(60000, true, true) == null, "one in flight");
        state.context(nether);
        check(!state.accept(request, 12), "stale response must not authorize or apply new realm");
        check(!state.visible(), "preauth layer hidden");
        var fresh = state.begin(60000, true, true);
        check(state.accept(fresh, 12), "current response authorizes");
        check(state.needsApply(), "first dimension applies"); state.applied();
        state.context(overworld);
        check(state.needsApply(), "same global revision applies after dimension switch"); state.applied();
        state.context(new SyncState.Context("entegrevesselam.com", "minecraft:overworld", "realm/B"));
        check(state.needsApply(), "same revision applies after subworld switch"); state.applied();
        check(state.begin(119999, true, true) == null, "60 second minimum");
        var older = state.begin(120000, true, true);
        check(!state.accept(older, 11), "old revision rejected");
        check(state.visible(), "old revision retains last valid layer");
        check(state.begin(180000, false, true) == null, "background does not fetch");
        check(state.begin(180000, true, false) == null, "unpaired does not fetch");
        var denied = state.begin(180000, true, true); state.denied(denied);
        check(!state.visible(), "401/403 hides layer");
        var recover = state.begin(240000, true, true); state.accept(recover, 12);
        check(state.needsApply(), "authorization recovery reapplies same revision");
        var late = state.begin(300000, true, true); state.close();
        check(!state.accept(late, 13), "shutdown drops late response");
        testSnapshot(); testApi(); testStore(); testBody(); testPolling(); testOwnership();
        System.out.println("PASS state: epoch, revision, dimensions/subworld, auth, foreground, in-flight, shutdown");
    }

    static void rejects(Runnable action, String label) { try { action.run(); } catch (RuntimeException expected) { return; } throw new AssertionError(label); }
    static String marker() { return "{\"id\":\"synthetic-1\",\"name\":\"Home <b>\",\"description\":\"plain text\",\"mapId\":\"world\",\"dimension\":\"minecraft:overworld\",\"x\":-29999984,\"y\":-64,\"z\":29999984,\"color\":15,\"icon\":\"home\",\"version\":1}"; }
    static byte[] json(String marker) { return ("{\"schemaVersion\":1,\"revision\":12,\"updatedAt\":\"2026-10-15T12:00:00.000Z\",\"markers\":[" + marker + "]}").getBytes(StandardCharsets.UTF_8); }
    static void testSnapshot() {
        check(Snapshot.parse(json(marker())).markers().getFirst().name().equals("Home <b>"), "plain text retained");
        check(Snapshot.parse(json("")).markers().isEmpty(), "explicit empty list valid");
        rejects(() -> Snapshot.parse("{}".getBytes()), "missing snapshot must not clear");
        rejects(() -> Snapshot.parse(json(marker().replace("-29999984","-29999985"))), "x boundary");
        rejects(() -> Snapshot.parse(json(marker().replace("-64","-65"))), "y boundary");
        rejects(() -> Snapshot.parse(json(marker().replace("29999984","1.5"))), "fraction coordinates");
        rejects(() -> Snapshot.parse(json(marker().replace("\"world\"","\"world_nether\""))), "map dimension mismatch");
        rejects(() -> Snapshot.parse(json(marker().replace("\"color\":15","\"color\":16"))), "palette boundary");
        rejects(() -> Snapshot.parse(json(marker().replace("\"home\"","\"https://evil.example\""))), "icon URL rejected");
        rejects(() -> Snapshot.parse(json(marker()+","+marker())), "duplicate IDs");
        rejects(() -> Snapshot.parse(new byte[Snapshot.MAX_BYTES+1]), "body boundary");
        rejects(() -> Snapshot.parse(new byte[]{(byte)0xff}), "damaged UTF8");
        rejects(() -> Snapshot.parse(new String(json(marker()),StandardCharsets.UTF_8).replace("\"revision\"", "revision").getBytes(StandardCharsets.UTF_8)), "unquoted key invalid JSON");
        String many = java.util.stream.IntStream.range(0,1000).mapToObj(i -> marker().replace("synthetic-1","m"+i)).collect(java.util.stream.Collectors.joining(","));
        // The 1000-item count test uses compact fixtures to remain below the separate byte limit.
        many = many.replace("Home <b>","A").replace("plain text","");
        final String thousand = many;
        check(Snapshot.parse(json(thousand)).markers().size() == 1000, "1000 records accepted within bytes");
        rejects(() -> Snapshot.parse(json(thousand+","+marker())), "1001 rejected");
        byte[] base = json(""); byte[] exact = Arrays.copyOf(base,Snapshot.MAX_BYTES); Arrays.fill(exact,base.length,exact.length,(byte)' ');
        check(Snapshot.parse(exact).markers().isEmpty(), "256KiB accepted");
        System.out.println("PASS snapshot: strict fields, dimensions, coordinates, IDs, icons, UTF8, empty, 1000 and 256KiB boundaries");
    }
    static void testApi() throws Exception {
        List<java.net.http.HttpRequest> sent = new ArrayList<>();
        ArrayDeque<MarkerApi.Response> replies = new ArrayDeque<>();
        MarkerApi api = new MarkerApi(request -> { sent.add(request); return replies.removeFirst(); });
        replies.add(new MarkerApi.Response(200,json(marker())));
        check(api.markers("synthetic-token").status() == 200, "marker response");
        var request = sent.getFirst();
        check(request.uri().equals(java.net.URI.create("https://entegrevesselam.com/api/client/markers")), "fixed origin");
        check(request.headers().firstValue("Authorization").orElseThrow().equals("Bearer synthetic-token"), "scoped authorization header");
        check(request.timeout().orElseThrow().toSeconds() == 10, "10 second timeout");
        String code = "A".repeat(43);
        replies.add(new MarkerApi.Response(200,("{\"deviceCode\":\""+code+"\",\"userCode\":\"12345678\",\"verificationUri\":\"https://entegrevesselam.com/map?code=12345678\",\"expiresIn\":600,\"interval\":5}").getBytes()));
        MarkerApi.Pairing pairing = api.start(1000);
        check(pairing.expiresAt() == 601000, "10 minute expiry");
        replies.add(new MarkerApi.Response(200,"{\"status\":\"pending\",\"accessToken\":\"ignored\"}".getBytes()));
        check(api.poll(pairing,1000).token() == null, "pending never yields token");
        replies.add(new MarkerApi.Response(200,("{\"status\":\"approved\",\"accessToken\":\""+code+"\",\"expiresIn\":2592000}").getBytes()));
        check(api.poll(pairing,1000).expiresAt() == 2592001000L, "30 day token");
        replies.add(new MarkerApi.Response(200,("{\"deviceCode\":\""+code+"\",\"userCode\":\"12345678\",\"verificationUri\":\"https://evil.example/map?code=12345678\",\"expiresIn\":600,\"interval\":5}").getBytes()));
        try { api.start(0); throw new AssertionError("arbitrary verification host"); } catch(java.io.IOException expected) {}
        replies.add(new MarkerApi.Response(200,("{\"status\":\"approved\",\"accessToken\":\""+code+"\",\"expiresIn\":2592001}").getBytes()));
        rejects(() -> { try { api.poll(pairing,0); } catch(RuntimeException e) { throw e; } catch(Exception e) { throw new RuntimeException(e); } }, "token expiry cap");
        System.out.println("PASS API: in-memory wire only, fixed origin, token scope, timeout, pairing URI/expiry, pending");
    }
    static void testStore() throws Exception {
        Path temp = Files.createTempDirectory("ev-markers-store-test");
        try {
            DeviceStore store = new DeviceStore(temp); long now = 100000;
            check(store.load(now) == null, "clean install unpaired");
            var first = store.save("T".repeat(43),now+1000);
            check(store.load(now).equals(first), "atomic credential roundtrip");
            check(store.load(now+1000) == null, "expired credential not loaded");
            if (Files.getFileStore(temp).supportsFileAttributeView("posix")) check(Files.getPosixFilePermissions(temp.resolve("entegre-markers/device.json")).equals(java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")), "owner-only credential");
            store.delete(); check(store.load(now) == null, "forget removes secret");
        } finally { try(var paths = Files.walk(temp)) { for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); } }
        System.out.println("PASS store: temporary fixture only, atomic persistence, owner-only POSIX, expiry, forget");
    }
    static void testBody() {
        var limited = new MarkerApi.LimitedBody(4); boolean[] cancelled = {false};
        limited.onSubscribe(new Flow.Subscription() { public void request(long n) {} public void cancel() { cancelled[0] = true; } });
        limited.onNext(List.of(ByteBuffer.wrap(new byte[4]))); limited.onComplete();
        check(limited.getBody().toCompletableFuture().join().length == 4, "exact streamed limit");
        var over = new MarkerApi.LimitedBody(4);
        over.onSubscribe(new Flow.Subscription() { public void request(long n) {} public void cancel() { cancelled[0] = true; } });
        over.onNext(List.of(ByteBuffer.wrap(new byte[5])));
        check(cancelled[0] && over.getBody().toCompletableFuture().isCompletedExceptionally(), "stream cancelled immediately above limit");
        System.out.println("PASS HTTP body: streaming limit and cancellation");
    }

    static void testPolling() {
        PollSchedule polling = new PollSchedule(0,600000);
        check(!polling.begin(4999,true,true), "no polling below 5 seconds");
        check(polling.begin(5000,true,true), "poll at 5 seconds");
        check(!polling.begin(10000,true,true), "one poll in flight");
        polling.complete();
        check(!polling.begin(9999,true,true), "minimum interval from request start");
        check(!polling.begin(10000,false,true), "pair polling pauses in background");
        check(!polling.begin(10000,true,false), "pair polling pauses off server");
        check(polling.begin(10000,true,true), "next allowed poll"); polling.complete();
        check(!polling.begin(600000,true,true), "10 minute boundary expired");
        System.out.println("PASS pairing scheduler: fake clock 5 second/10 minute, foreground, target, one in flight");
    }

    static void testOwnership() {
        Ownership<Object> ownership = new Ownership<>(); Object container = new Object(), personal = new Object();
        rejects(() -> ownership.open(container,personal,Object::new), "preexisting personal layer must never be claimed");
        Object managed = ownership.open(container,null,Object::new);
        check(ownership.open(container,managed,Object::new) == managed, "reopen own exact runtime layer");
        rejects(() -> ownership.open(new Object(),managed,Object::new), "other realm does not share ownership");
        Map<String,Object> actual = new HashMap<>(), expected = new HashMap<>();
        actual.put("same-name",personal); expected.put("same-name",managed);
        rejects(() -> Ownership.verifyContents(actual,expected), "names and IDs cannot confer ownership");
        actual.put("same-name",managed); Ownership.verifyContents(actual,expected);
        actual.put("unexpected-personal",personal);
        rejects(() -> Ownership.verifyContents(actual,expected), "unexpected record prevents destructive clear");
        check(actual.get("unexpected-personal") == personal, "collision check never mutates personal records");
        System.out.println("PASS ownership: existing layer, realm identity, record identity, collision no mutation");
    }
}
