package com.entegre.launcher;

import java.util.Arrays;

public final class LauncherTest {
    public static void main(String[] args) {
        var normal = Launcher.parseArguments(new String[0]);
        check(normal.packUrl().equals(Launcher.DEFAULT_PACK_URL), "normal launch uses existing pack");
        check(!normal.settings() && !normal.preview(), "normal launch mode");
        var command = Arrays.asList(Launcher.upstreamArguments(normal));
        check(command.equals(Arrays.asList("--timeout", "0", "--title", "Entegre Vesselam", Launcher.DEFAULT_PACK_URL)), "no countdown; GUI remains enabled");
        check(Launcher.parseArguments(new String[]{"--settings"}).settings(), "separate settings access");
        check(Launcher.parseArguments(new String[]{"--preview"}).preview(), "preview mode");
        var local = Launcher.parseArguments(new String[]{"--settings", "http://127.0.0.1:8765/pack.toml"});
        check(local.settings() && local.packUrl().contains("8765"), "isolated fixture URL");
        for (String[] invalid : new String[][]{
            {"--no-gui"}, {"--timeout", "10"}, {"--preview", "--settings"},
            {"--preview", "https://example.com/pack.toml"}, {"file:///private/data"},
            {"http://example.com/pack.toml"}, {"https://name:secret@example.com/pack.toml"},
            {"https://example.com/pack.toml#fragment"}, {"https://"},
            {"https://example.com/a", "https://example.com/b"}, {"--settings", "--settings"}
        }) {
            boolean rejected = false;
            try { Launcher.parseArguments(invalid); }
            catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "invalid or ambiguous arguments rejected");
        }
        System.out.println("PASS LauncherTest: launch defaults, settings, preview, 12 invalid cases");
    }

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
}
