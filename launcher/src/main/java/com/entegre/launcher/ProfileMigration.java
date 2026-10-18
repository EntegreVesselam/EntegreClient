package com.entegre.launcher;

import com.google.gson.*;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/** Prism resolves its runtime before PreLaunchCommand, so changed profiles must restart. */
public final class ProfileMigration {
    private static final String RAW = "https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/";
    public static String channelUrl(String url) {
        if (url.equals(RAW + "main/pack.toml") || url.equals(
                "https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml")) {
            return RAW + "minecraft-26.2/pack.toml";
        }
        return url;
    }
    public static String targetVersion(String url) {
        for (String game : new String[]{"26.2", "26.3"})
            if (url.equals(RAW + "minecraft-" + game + "/pack.toml")) return game;
        return null;
    }
    public static boolean prepare(Path gameRoot, String target) throws IOException {
        Path instance = gameRoot.toRealPath().getParent();
        Path profile = instance.resolve("mmc-pack.json");
        if (!Files.isRegularFile(instance.resolve("instance.cfg")) || Files.isSymbolicLink(profile))
            throw new IOException("Prism profili güvenle bulunamadı; ayrı Prism paketini içe aktarın.");
        byte[] original = Files.readAllBytes(profile);
        if (original.length > 1024 * 1024) throw new IOException("Prism profil boyutu geçersiz.");
        JsonObject data;
        try { data = JsonParser.parseReader(new StringReader(new String(original, StandardCharsets.UTF_8))).getAsJsonObject(); }
        catch (RuntimeException bad) { throw new IOException("Prism profili okunamadı.", bad); }
        JsonArray components = data.get("components") == null ? null : data.get("components").getAsJsonArray();
        if (components == null) throw new IOException("Prism sürüm bileşenleri eksik.");
        boolean minecraft = false, loader = false, lwjgl = false, changed = false;
        JsonArray updated = new JsonArray();
        for (Object rawItem : components) {
            JsonObject component = ((JsonElement) rawItem).getAsJsonObject();
            String uid = component.get("uid").getAsString();
            String desired = switch (uid) {
                case "net.minecraft" -> { minecraft = true; yield target; }
                case "net.fabricmc.fabric-loader" -> { loader = true; yield "0.19.5"; }
                case "org.lwjgl3" -> { lwjgl = true; yield target.equals("26.2") ? "3.4.1" : "3.4.3"; }
                default -> null;
            };
            if (uid.equals("net.fabricmc.intermediary")) { changed = true; continue; }
            if (desired != null && (component.get("version") == null || !desired.equals(component.get("version").getAsString()))) {
                component.add("version", new JsonPrimitive(desired)); changed = true;
            }
            updated.add(component);
        }
        if (!minecraft || !loader || !lwjgl) throw new IOException("Prism Minecraft/Fabric/LWJGL bileşenleri eksik; ayrı paketi içe aktarın.");
        if (!changed) return false;
        data.add("components", updated);
        Path backup = instance.resolve("mmc-pack.before-entegre-" + target + ".json");
        // Never overwrite the original rollback snapshot, including on a retried migration.
        try { Files.write(backup, original, StandardOpenOption.CREATE_NEW); }
        catch (FileAlreadyExistsException exists) {
            if (Files.isSymbolicLink(backup) || !Files.isRegularFile(backup)) throw new IOException("Profil yedeği güvenli değil.");
        }
        Path temporary = Files.createTempFile(instance, "mmc-pack-entegre-", ".tmp");
        try {
            Files.writeString(temporary, new Gson().toJson(data) + "\n");
            Files.move(temporary, profile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
        return true;
    }
    private ProfileMigration() {}
}
