package com.entegre.launcher;
import java.nio.file.*;
import com.google.gson.*;
import java.io.StringReader;
import java.util.Arrays;
public final class ProfileMigrationTest {
    public static void main(String[] args) throws Exception {
        String main = Launcher.DEFAULT_PACK_URL;
        String channel = main.replace("/main/", "/minecraft-26.2/");
        check(ProfileMigration.channelUrl(main).equals(channel), "legacy raw URL routes to 26.2");
        check(ProfileMigration.channelUrl("https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml").equals(channel), "legacy public starter URL");
        String other = main.replace("/main/", "/minecraft-26.3/");
        check(ProfileMigration.channelUrl(other).equals(other), "explicit 26.3 retained");
        check(ProfileMigration.targetVersion("https://example.com/minecraft-26.3/pack.toml") == null, "unrelated channels untouched");
        Path instance = Files.createTempDirectory("entegre-profile-test-");
        try {
            Path game = Files.createDirectory(instance.resolve("minecraft"));
            Path profile = instance.resolve("mmc-pack.json");
            Files.writeString(instance.resolve("instance.cfg"), "[General]\nname=Player choice\n");
            String old = "{\"formatVersion\":1,\"custom\":\"preserve me\",\"components\":[{\"uid\":\"net.minecraft\",\"version\":\"1.21.11\",\"important\":true},{\"uid\":\"net.fabricmc.fabric-loader\",\"version\":\"0.18.4\"},{\"uid\":\"org.lwjgl3\",\"version\":\"3.3.3\"},{\"uid\":\"net.fabricmc.intermediary\",\"version\":\"old\"},{\"uid\":\"custom.safe\",\"version\":\"kept\"}]}";
            Files.writeString(profile, old);
            Files.writeString(game.resolve("options.txt"), "player settings");
            check(ProfileMigration.prepare(game, "26.2"), "migration requests restart before installing mods");
            check(Files.readString(instance.resolve("mmc-pack.before-entegre-26.2.json")).equals(old), "exact original rollback snapshot");
            String migrated = Files.readString(profile);
            check(migrated.contains("26.2") && migrated.contains("3.4.1") && migrated.contains("0.19.5"), "complete target runtime");
            check(migrated.contains("intermediary") && migrated.contains("preserve me") && migrated.contains("custom.safe"), "unknown metadata retained; required mapping retained");
            check(!ProfileMigration.prepare(game,"26.2") && migrated.equals(Files.readString(profile)), "retry is byte-identical");
            check(Files.readString(game.resolve("options.txt")).equals("player settings"), "player settings untouched");
            check(ProfileMigration.prepare(game,"26.3") && Files.readString(profile).contains("3.4.3"), "26.3 uses correct LWJGL");
            Files.writeString(profile, "broken");
            try { ProfileMigration.prepare(game,"26.2"); throw new AssertionError("malformed profile accepted"); }
            catch (java.io.IOException expected) { check(Files.readString(profile).equals("broken"),"invalid input untouched"); }
            Files.delete(profile); Files.createSymbolicLink(profile, game.resolve("options.txt"));
            try { ProfileMigration.prepare(game,"26.2"); throw new AssertionError("symlink accepted"); }
            catch (java.io.IOException expected) { check(Files.readString(game.resolve("options.txt")).equals("player settings"),"symlink target untouched"); }
        } finally {
            try (var files=Files.walk(instance)) { for (Path path : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(path); }
        }
        System.out.println("PASS ProfileMigrationTest: legacy routing, explicit channels, restart, exact backup, idempotence, player preservation, invalid input");
    }
    private static void check(boolean result,String message) { if(!result) throw new AssertionError(message); }
}
