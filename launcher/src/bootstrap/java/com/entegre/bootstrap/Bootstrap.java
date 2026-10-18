package com.entegre.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Stable Prism entry point; the managed engine is never executed in place. */
public final class Bootstrap {
    private Bootstrap() {}

    public static void main(String[] args) {
        int result;
        try {
            result = run(Path.of("."), args);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            System.err.println("Güncelleme durduruldu.");
            result = 1;
        } catch (IOException | RuntimeException failure) {
            System.err.println("Güncelleyici açılamadı. Dosyaları geri yükleyip yeniden deneyin.");
            result = 1;
        }
        System.exit(result);
    }

    public static int run(Path gameDirectory, String[] args) throws IOException, InterruptedException {
        Path root = Payloads.root(gameDirectory);
        byte[] jar;
        try {
            jar = Payloads.verified(root, "engine");
        } catch (IOException invalidEngine) {
            jar = Payloads.read(root, Payloads.DIRECTORY + "bootstrap-backup.jar", Payloads.MAX_JAR_BYTES);
            if (!Payloads.ORIGINAL_SHA256.equals(Payloads.sha256(jar))) {
                throw new IOException("Önceki başlatıcı doğrulanamadı.");
            }
            System.err.println("Güncelleyici doğrulanamadı; doğrulanmış önceki başlatıcı kullanılıyor.");
        }
        // No fallback after this point: cancellation and engine errors must reach Prism unchanged.
        return runPrivateCopy(root, jar, args);
    }

    private static int runPrivateCopy(Path root, byte[] jar, String[] args) throws IOException, InterruptedException {
        Path directory = Files.createTempDirectory("entegre-updater-");
        Path temporary = directory.resolve("engine.jar");
        try {
            Payloads.writeForced(temporary, jar);
            if (!Payloads.sha256(jar).equals(Payloads.sha256(Payloads.read(directory, "engine.jar", Payloads.MAX_JAR_BYTES)))) {
                throw new IOException("Geçici güncelleyici doğrulanamadı.");
            }
            String executable = System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java";
            List<String> command = new ArrayList<>(List.of(
                    Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                    "-jar", temporary.toString()));
            command.addAll(Arrays.asList(args));
            Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
            try {
                return process.waitFor();
            } catch (InterruptedException interrupted) {
                process.destroyForcibly();
                process.waitFor();
                throw interrupted;
            }
        } finally {
            try {
                Files.deleteIfExists(temporary);
                Files.deleteIfExists(directory);
            } catch (IOException cleanup) {
                directory.toFile().deleteOnExit();
                temporary.toFile().deleteOnExit();
            }
        }
    }
}
