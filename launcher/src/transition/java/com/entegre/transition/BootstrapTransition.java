package com.entegre.transition;

import com.entegre.bootstrap.Payloads;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/** Runs in the game JVM, after Prism's previous pre-launch process has exited. */
public final class BootstrapTransition {
    public enum Result { INSTALLED, ALREADY_INSTALLED, SKIPPED, FAILED }
    public static final String SUPPORTED_COMMAND = "\"$INST_JAVA\" -jar packwiz-installer-bootstrap.jar "
            + "https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/main/pack.toml";
    private static final String RELEASE_COMMAND = "\"$INST_JAVA\" -jar \"$INST_DIR/minecraft/packwiz-installer-bootstrap.jar\" "
            + "https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml";

    private BootstrapTransition() {}

    public static Result apply(Path gameDirectory) {
        Path stage = null;
        try {
            Path root = Payloads.root(gameDirectory);
            if (Files.exists(root.resolve(".entegre-updater-disabled"), LinkOption.NOFOLLOW_LINKS)) return Result.SKIPPED;
            if (!supportedCommand(root)) return Result.SKIPPED;
            Path target = Payloads.path(root, "packwiz-installer-bootstrap.jar");
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return Result.SKIPPED;
            byte[] current = Payloads.read(root, "packwiz-installer-bootstrap.jar", Payloads.MAX_JAR_BYTES);
            byte[] replacement = Payloads.verified(root, "bootstrap");
            if (Arrays.equals(current, replacement)) return Result.ALREADY_INSTALLED;
            if (!Payloads.ORIGINAL_SHA256.equals(Payloads.sha256(current))) return Result.SKIPPED;
            Payloads.verified(root, "engine");

            Path backup = Payloads.path(root, Payloads.DIRECTORY + "bootstrap-backup.jar");
            if (!Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) createBackup(root, backup, current);
            byte[] saved = Payloads.read(root, Payloads.DIRECTORY + "bootstrap-backup.jar", Payloads.MAX_JAR_BYTES);
            if (!Payloads.ORIGINAL_SHA256.equals(Payloads.sha256(saved))) return Result.FAILED;

            // Stage beside the target. ATOMIC_MOVE has deliberately no non-atomic fallback.
            stage = Files.createTempFile(root, ".entegre-bootstrap-", ".tmp");
            Files.delete(stage);
            Payloads.writeForced(stage, replacement);
            if (!Payloads.sha256(replacement).equals(Payloads.sha256(
                    Payloads.read(root, stage.getFileName().toString(), Payloads.MAX_JAR_BYTES)))) return Result.FAILED;
            if (!Arrays.equals(current, Payloads.read(root, "packwiz-installer-bootstrap.jar", Payloads.MAX_JAR_BYTES))) {
                return Result.FAILED;
            }
            Payloads.path(root, Payloads.DIRECTORY + "bootstrap-backup.jar");
            atomicReplace(stage, target);
            return Result.INSTALLED;
        } catch (IOException | RuntimeException failure) {
            return Result.FAILED;
        } finally {
            if (stage != null) {
                try { Files.deleteIfExists(stage); } catch (IOException ignored) { /* Retry next launch. */ }
            }
        }
    }

    private static void atomicReplace(Path staged, Path target) throws IOException {
        Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void createBackup(Path root, Path backup, byte[] original) throws IOException {
        Path temporary = Files.createTempFile(backup.getParent(), ".bootstrap-backup-", ".tmp");
        try {
            Files.delete(temporary);
            Payloads.writeForced(temporary, original);
            String relative = Payloads.DIRECTORY + temporary.getFileName();
            if (!Payloads.ORIGINAL_SHA256.equals(Payloads.sha256(Payloads.read(root, relative, Payloads.MAX_JAR_BYTES)))) {
                throw new IOException("Önceki başlatıcı yedeği doğrulanamadı.");
            }
            Payloads.path(root, Payloads.DIRECTORY + "bootstrap-backup.jar");
            try {
                publishBackup(temporary, backup);
            } catch (FileAlreadyExistsException collision) {
                // The caller verifies an existing backup. An unknown file is never replaced.
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void publishBackup(Path staged, Path backup) throws IOException {
        // A hard link publishes the complete forced inode atomically and cannot replace an existing entry.
        // Unsupported filesystems safely retain the old bootstrap; there is no copying fallback.
        Files.createLink(backup, staged);
    }

    private static boolean supportedCommand(Path root) throws IOException {
        Path parent = root.getParent();
        if (parent == null) return false;
        Path cfg = Payloads.path(parent, "instance.cfg");
        if (!Files.exists(cfg, LinkOption.NOFOLLOW_LINKS)) return false;
        String contents = new String(Payloads.read(parent, "instance.cfg", 65536), StandardCharsets.UTF_8);
        boolean general = false;
        String command = null;
        String override = null;
        for (String line : contents.split("\\R")) {
            line = line.strip();
            if (line.startsWith("[")) { general = line.equals("[General]"); continue; }
            if (!general || line.startsWith("#") || line.startsWith(";")) continue;
            if (line.startsWith("PreLaunchCommand=")) {
                if (command != null) return false;
                command = line.substring("PreLaunchCommand=".length());
            } else if (line.startsWith("OverrideCommands=")) {
                if (override != null) return false;
                override = line.substring("OverrideCommands=".length());
            }
        }
        if (!"true".equals(override) || command == null) return false;
        for (String supported : new String[]{SUPPORTED_COMMAND, RELEASE_COMMAND}) {
            // The released command points at this exact sibling game folder, not an arbitrary current directory.
            if (supported.equals(RELEASE_COMMAND) && !root.getFileName().toString().equals("minecraft")) continue;
            String escaped = supported.replace("\"", "\\\"");
            if (command.equals(supported) || command.equals(escaped) || command.equals("\"" + escaped + "\"")) return true;
        }
        return false;
    }
}
