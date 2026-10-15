package com.entegrevesselam.markers;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
final class DeviceStore {
    record Device(String token, long expiresAt, String ownerId) {}
    private final Path directory, file;
    DeviceStore(Path config) { directory = config.resolve("entegre-markers"); file = directory.resolve("device.json"); }
    Device load(long now) {
        try {
            if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(file) || Files.size(file) > 4096) return null;
            JsonObject value = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            String token = Snapshot.string(value,"token",32,256), owner = Snapshot.string(value,"ownerId",36,36);
            UUID.fromString(owner);
            long expiry = Snapshot.number(value,"expiresAt",now+1,now+30L*86400000);
            if (!token.matches("[A-Za-z0-9._~-]+")) return null;
            return new Device(token,expiry,owner);
        } catch (Exception ignored) { return null; }
    }
    Device save(String token, long expiry) throws IOException {
        if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(file)) throw new IOException("unsafe_store");
        Files.createDirectories(directory); secure(directory,true);
        Device device = new Device(token,expiry,UUID.randomUUID().toString());
        JsonObject value = new JsonObject(); value.addProperty("token",token); value.addProperty("expiresAt",expiry); value.addProperty("ownerId",device.ownerId);
        Path temporary = Files.createTempFile(directory,"device-",".tmp");
        try {
            secure(temporary,false); Files.writeString(temporary,value.toString());
            Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); secure(file,false);
        } finally { Files.deleteIfExists(temporary); }
        return device;
    }
    void delete() throws IOException { Files.deleteIfExists(file); }
    private static void secure(Path path, boolean directory) throws IOException {
        if (Files.getFileStore(path).supportsFileAttributeView("posix")) Files.setPosixFilePermissions(path,PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"));
    }
}
