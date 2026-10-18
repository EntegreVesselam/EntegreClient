package com.entegre.launcher;

/** Text emitted by the pinned Packwiz engine; unknown details remain visible. */
public final class ProgressText {
    private ProgressText() {}

    public record Text(String title, String detail) {}

    public static Text from(String message) {
        String title = switch (message) {
            case "Loading manifest file..." -> "Kurulum bilgileri okunuyor";
            case "Loading pack file..." -> "Güncellemeler kontrol ediliyor";
            case "Loading MultiMC pack file..." -> "Oyun sürümü kontrol ediliyor";
            case "Checking local files..." -> "Yerel dosyalar kontrol ediliyor";
            case "Comparing new files..." -> "Dosya değişiklikleri karşılaştırılıyor";
            case "Validating existing files..." -> "Dosyalar doğrulanıyor";
            case "Resolving CurseForge metadata..." -> "Mod bilgileri alınıyor";
            case "Modpack is already up to date!", "Modpack files are already up to date!" -> "Her şey güncel";
            case "Reconfigure optional mods?" -> "Mod ayarları hazır";
            default -> null;
        };
        if (title != null) return new Text(title, "");
        if (message.startsWith("Failed to download ")) {
            return new Text("Dosya indirilemedi", message.substring("Failed to download ".length()));
        }
        if (message.startsWith("Downloaded ")) {
            return new Text("Dosya indirildi", message.substring("Downloaded ".length()));
        }
        if (message.startsWith("Downloading ")) {
            return new Text("Dosya indiriliyor", message.substring("Downloading ".length()));
        }
        String[][] endings = {
            {" already exists (cached)", "Dosya güncel"},
            {" already exists (validated)", "Dosya doğrulandı"},
            {" pending (you should never see this...)", "Dosya bekleniyor"}
        };
        for (String[] ending : endings) {
            if (message.endsWith(ending[0])) {
                return new Text(ending[1], message.substring(0, message.length() - ending[0].length()));
            }
        }
        for (String prefix : new String[] {"Skipped ", "Deleted "}) {
            if (!message.startsWith(prefix)) continue;
            boolean skipped = prefix.equals("Skipped ");
            String suffix;
            if (message.endsWith(" (disabled)")) {
                suffix = " (disabled)";
                title = skipped ? "Devre dışı dosya atlandı" : "Devre dışı dosya kaldırıldı";
            } else if (message.endsWith(" (wrong side)")) {
                suffix = " (wrong side)";
                title = skipped ? "Bu kurulum için gerekmeyen dosya atlandı"
                    : "Bu kurulum için gerekmeyen dosya kaldırıldı";
            } else {
                continue;
            }
            return new Text(title, message.substring(prefix.length(), message.length() - suffix.length()));
        }
        return new Text("Güncelleme sürüyor", message);
    }
}
