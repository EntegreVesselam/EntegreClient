package com.entegre.launcher;

import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import link.infra.packwiz.installer.Main;
import link.infra.packwiz.installer.ui.data.InstallProgress;
import link.infra.packwiz.installer.ui.gui.GUIHandler;

public final class Launcher {
    public static final String DEFAULT_PACK_URL = "https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/main/pack.toml";
    public record LaunchRequest(String packUrl, boolean settings, boolean preview) {}

    public static LaunchRequest parseArguments(String[] args) {
        boolean settings = false;
        boolean preview = false;
        String packUrl = null;
        for (String arg : args) {
            if (arg.equals("--settings") && !settings) settings = true;
            else if (arg.equals("--preview") && !preview) preview = true;
            else if (!arg.startsWith("-") && packUrl == null) packUrl = arg;
            else throw new IllegalArgumentException("Geçersiz veya yinelenen açılış seçeneği.");
        }
        if (preview && (settings || packUrl != null)) {
            throw new IllegalArgumentException("Önizleme başka seçeneklerle birlikte kullanılamaz.");
        }
        if (packUrl == null) packUrl = DEFAULT_PACK_URL;
        URI uri;
        try { uri = URI.create(packUrl); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Paket adresi geçersiz."); }
        String host = uri.getHost();
        boolean loopback = "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)
                || "[::1]".equals(host);
        if (host == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                || !("https".equalsIgnoreCase(uri.getScheme())
                    || ("http".equalsIgnoreCase(uri.getScheme()) && loopback))) {
            throw new IllegalArgumentException("HTTPS paket adresi gerekiyor; HTTP yalnız yerel denemede kullanılabilir.");
        }
        return new LaunchRequest(ProfileMigration.channelUrl(packUrl), settings, preview);
    }

    public static String[] upstreamArguments(LaunchRequest request) {
        return new String[]{"--timeout", "0", "--title", "Entegre Vesselam", request.packUrl()};
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("Entegre Vesselam güncelleyici\n"
                    + "Kullanım: java -jar entegre-updater.jar [--settings] [HTTPS-paket-adresi]\n"
                    + "Önizleme: java -jar entegre-updater.jar --preview\n"
                    + "Mod ayarları: java -jar entegre-updater.jar --settings");
            return;
        }
        LaunchRequest request;
        try { request = parseArguments(args); }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + " Yardım: --help");
            System.exit(2);
            return;
        }
        // The upstream headless fallback enables optional mods without a choice.
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Entegre güncelleyici için masaüstü oturumu gerekiyor.");
            System.exit(1);
        }
        System.setProperty("entegre.settings", Boolean.toString(request.settings()));
        System.setProperty("entegre.preview", Boolean.toString(request.preview()));
        System.setProperty("apple.awt.application.name", "Entegre Vesselam");
        UIManager.put("OptionPane.okButtonText", "Tamam");
        UIManager.put("OptionPane.cancelButtonText", "İptal");
        UIManager.put("OptionPane.yesButtonText", "Evet");
        UIManager.put("OptionPane.noButtonText", "Hayır");
        if (request.preview()) {
            GUIHandler preview = new GUIHandler();
            preview.setTitle("Entegre Vesselam — Önizleme");
            preview.show();
            preview.submitProgress(new InstallProgress("Downloading Entegre Markers", 7, 12));
            preview.disableOptionsButton(true);
            return;
        }
        String target = ProfileMigration.targetVersion(request.packUrl());
        if (target != null) {
            try {
                if (ProfileMigration.prepare(Path.of("."), target)) {
                    JOptionPane.showMessageDialog(null, "Profil Minecraft " + target + " için hazırlandı.\n"
                            + "Prism’i tamamen kapatıp yeniden açın ve oyunu tekrar başlatın.\n"
                            + "Bu sürüm Java 25 gerektirir; Prism isterse Java 25’i seçin.",
                            "Entegre Vesselam — Sürüm geçişi", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(3);
                    return;
                }
                if (Runtime.version().feature() < 25) {
                    throw new java.io.IOException("Prism profilinin Java ayarından Java 25’i seçip yeniden başlatın.");
                }
            } catch (java.io.IOException | RuntimeException failure) {
                JOptionPane.showMessageDialog(null, failure.getMessage(), "Entegre Vesselam — Geçiş durduruldu", JOptionPane.ERROR_MESSAGE);
                System.exit(2);
                return;
            }
        }
        new Main(upstreamArguments(request));
        System.exit(0);
    }
}
