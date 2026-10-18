package com.entegrevesselam.markers;
import com.google.gson.*;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;
/** Fixed-origin, redirect-free wire boundary; test constructor accepts an in-memory transport. */
final class MarkerApi {
    static final URI ORIGIN = URI.create("https://entegrevesselam.com");
    record Response(int status, byte[] body) {}
    record Pairing(String deviceCode, String userCode, URI verificationUri, long expiresAt) {}
    record Poll(String status, String token, long expiresAt) {}
    interface Wire { Response send(HttpRequest request) throws Exception; }
    private final Wire wire;
    MarkerApi() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER).build();
        wire = request -> {
            var future = client.sendAsync(request, info -> new LimitedBody(Snapshot.MAX_BYTES));
            try { var r = future.get(10, TimeUnit.SECONDS); return new Response(r.statusCode(),r.body()); }
            catch (Exception e) { future.cancel(true); throw e; }
        };
    }
    MarkerApi(Wire wire) { this.wire = wire; }
    private Response send(String path, String token, JsonObject data) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(ORIGIN.resolve(path)).timeout(Duration.ofSeconds(10)).header("Accept","application/json").header("User-Agent","EntegreMarkers/0.2.1");
        if (token != null) request.header("Authorization","Bearer " + token);
        if (data != null) request.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(data.toString()));
        Response r = wire.send(request.build());
        if (r.body.length > Snapshot.MAX_BYTES) throw new IOException("body_size");
        return r;
    }
    Response markers(String token) throws Exception { return send("/api/client/markers",token,null); }
    Pairing start(long now) throws Exception {
        JsonObject data = new JsonObject(); data.addProperty("label","Entegre Client");
        Response response = send("/api/map/devices/start",null,data);
        if (response.status != 200) throw new IOException("pair_start");
        JsonObject r = Snapshot.strictJson(new String(response.body,StandardCharsets.UTF_8));
        String device = Snapshot.string(r,"deviceCode",32,256), user = Snapshot.string(r,"userCode",4,32);
        URI link = URI.create(Snapshot.string(r,"verificationUri",1,256));
        if (!user.matches("[A-Za-z0-9-]+") || !link.equals(URI.create(ORIGIN + "/map?code=" + user))) throw new IOException("pair_uri");
        long expires = Snapshot.number(r,"expiresIn",1,600); Snapshot.number(r,"interval",5,5);
        return new Pairing(device,user,link,now + expires * 1000);
    }
    Poll poll(Pairing pairing, long now) throws Exception {
        JsonObject data = new JsonObject(); data.addProperty("deviceCode",pairing.deviceCode);
        Response response = send("/api/map/devices/poll",null,data);
        if (response.status != 200) throw new IOException("pair_poll");
        JsonObject r = Snapshot.strictJson(new String(response.body,StandardCharsets.UTF_8));
        String status = Snapshot.string(r,"status",1,16);
        if (!Set.of("pending","approved","expired").contains(status)) throw new IOException("pair_status");
        if (!status.equals("approved")) return new Poll(status,null,0);
        String token = Snapshot.string(r,"accessToken",32,256);
        if (!token.matches("[A-Za-z0-9._~-]+")) throw new IOException("pair_token");
        long seconds = Snapshot.number(r,"expiresIn",1,30L*24*60*60);
        return new Poll(status,token,now+seconds*1000);
    }
    static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final int maximum; private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final CompletableFuture<byte[]> body = new CompletableFuture<>(); private Flow.Subscription subscription;
        LimitedBody(int maximum) { this.maximum = maximum; }
        public CompletionStage<byte[]> getBody() { return body; }
        public void onSubscribe(Flow.Subscription value) { subscription = value; value.request(1); }
        public void onNext(List<ByteBuffer> parts) {
            for (ByteBuffer part : parts) {
                if (part.remaining() > maximum - bytes.size()) { subscription.cancel(); body.completeExceptionally(new IOException("body_size")); return; }
                byte[] data = new byte[part.remaining()]; part.get(data); bytes.writeBytes(data);
            }
            subscription.request(1);
        }
        public void onError(Throwable error) { body.completeExceptionally(error); }
        public void onComplete() { body.complete(bytes.toByteArray()); }
    }
}
