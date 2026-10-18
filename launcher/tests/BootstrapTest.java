import com.entegre.bootstrap.Bootstrap;
import com.entegre.bootstrap.Payloads;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class BootstrapTest {
    public static final class FakeEngine {
        public static void main(String[] args) throws Exception {
            Path copy = Path.of(FakeEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Files.write(Path.of("observed.txt"), List.of(Path.of(".").toRealPath().toString(), copy.toString(),
                    String.join("|", args)), StandardCharsets.UTF_8);
            Files.writeString(Path.of("ready"), "ready");
            if (args.length > 0 && args[0].equals("hold")) {
                long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
                while (!Files.exists(Path.of("release")) && System.nanoTime() < end) Thread.sleep(15);
            }
            System.exit(7);
        }
    }

    public static void main(String[] args) throws Exception {
        Path original = Path.of(args.length == 0 ? "vendor/packwiz-installer-bootstrap.jar" : args[0]);
        byte[] official = Files.readAllBytes(original);
        require(Payloads.sha256(official).equals(Payloads.ORIGINAL_SHA256), "Unrecognized official fixture");
        Path base = Files.createTempDirectory("entegre-bootstrap-tests-").toRealPath();
        try {
            byte[] engine = fakeJar();
            Path game = instance(base, "copy-and-exit", engine);
            byte[] personal = "synthetic personal data".getBytes(StandardCharsets.UTF_8);
            Files.write(game.resolve("options.txt"), personal);
            // A valid rollback copy exists: exit 7 must still propagate, never invoke fallback.
            Files.write(game.resolve("entegre-updater/bootstrap-backup.jar"), official);
            CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> {
                try { return Bootstrap.run(game, new String[]{"hold", "space argument", ";touch injected", "$(no-shell)"}); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!Files.exists(game.resolve("ready")) && System.nanoTime() < deadline) Thread.sleep(15);
            require(Files.exists(game.resolve("ready")), "Fake child did not start");
            List<String> observed = Files.readAllLines(game.resolve("observed.txt"));
            require(observed.get(0).equals(game.toString()), "Child working directory changed");
            require(observed.get(2).equals("hold|space argument|;touch injected|$(no-shell)"), "Arguments were changed or interpreted");
            Path temporary = Path.of(observed.get(1));
            require(!temporary.startsWith(game), "Engine executed in the managed game directory");
            require(Arrays.equals(Files.readAllBytes(temporary), engine), "Private copy differs");
            if (Files.getFileStore(temporary).supportsFileAttributeView("posix")) {
                require(Files.getPosixFilePermissions(temporary.getParent()).stream()
                        .allMatch(permission -> permission.name().startsWith("OWNER_")), "Temporary directory is not private");
            }
            Files.write(game.resolve("entegre-updater/engine.jar"), "replacement while child runs".getBytes(StandardCharsets.UTF_8));
            require(Arrays.equals(Files.readAllBytes(temporary), engine), "Running copy changed with managed engine");
            Files.writeString(game.resolve("release"), "release");
            require(result.get(10, TimeUnit.SECONDS) == 7, "Engine nonzero exit was hidden");
            require(!Files.exists(temporary) && !Files.exists(temporary.getParent()), "Private execution copy was not cleaned up");
            require(Arrays.equals(Files.readAllBytes(game.resolve("options.txt")), personal), "Personal file changed");
            require(!Files.exists(game.resolve("injected")), "Shell syntax executed");
            System.out.println("PASS shim: actual child exit 7; cwd/argv preserved; managed file replaceable; private copy cleaned");

            Path missing = instance(base, "missing-engine", engine);
            Files.delete(missing.resolve("entegre-updater/engine.jar"));
            rejected(missing, "Missing engine without verified backup");
            Process failedMain = launchMain(missing);
            String failureOutput = new String(failedMain.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            require(failedMain.waitFor() == 1, "Shim main did not exit 1 for a missing engine");
            require(failureOutput.contains("Güncelleyici açılamadı") && !failureOutput.contains(missing.toString()),
                    "Failure message is not generic Turkish");
            Path realMain = instance(base, "main-exit", engine);
            Process mainProcess = launchMain(realMain);
            mainProcess.getInputStream().readAllBytes();
            require(mainProcess.waitFor() == 7, "Shim main did not propagate child exit 7");
            Path corrupt = instance(base, "wrong-hash", engine);
            Files.writeString(corrupt.resolve("entegre-updater/engine.sha256"), "0".repeat(64));
            rejected(corrupt, "Wrong engine hash");
            Path truncated = instance(base, "truncated", engine);
            Files.write(truncated.resolve("entegre-updater/engine.jar"), Arrays.copyOf(engine, engine.length / 2));
            rejected(truncated, "Truncated engine");
            Path hashSyntax = instance(base, "hash-syntax", engine);
            Files.writeString(hashSyntax.resolve("entegre-updater/engine.sha256"), Payloads.sha256(engine) + "  engine.jar\n");
            rejected(hashSyntax, "Non-plain SHA file");
            Path tooLarge = instance(base, "oversize", engine);
            try (var file = new java.io.RandomAccessFile(tooLarge.resolve("entegre-updater/engine.jar").toFile(), "rw")) {
                file.setLength(Payloads.MAX_JAR_BYTES + 1L);
            }
            rejected(tooLarge, "Oversized engine");
            System.out.println("PASS shim: missing, corrupt, truncated, malformed SHA and oversized engines rejected");

            for (String leaf : List.of("engine.jar", "engine.sha256")) {
                Path linked = instance(base, "symlink-" + leaf, engine);
                Path managed = linked.resolve("entegre-updater/" + leaf);
                Path outside = base.resolve("outside-" + leaf);
                Files.move(managed, outside);
                Files.createSymbolicLink(managed, outside);
                rejected(linked, "Symbolic payload path");
            }
            Path linkedDirectory = instance(base, "symlink-directory", engine);
            Path outside = base.resolve("outside-directory");
            Files.move(linkedDirectory.resolve("entegre-updater"), outside);
            Files.createSymbolicLink(linkedDirectory.resolve("entegre-updater"), outside);
            rejected(linkedDirectory, "Symbolic managed directory");
            System.out.println("PASS shim: symbolic engine, checksum and managed directory rejected");

            Path fallback = instance(base, "verified-fallback", engine);
            Files.delete(fallback.resolve("entegre-updater/engine.jar"));
            Files.write(fallback.resolve("entegre-updater/bootstrap-backup.jar"), official);
            require(Bootstrap.run(fallback, new String[]{"--help"}) == 0, "Verified original backup could not show its offline help");
            Files.writeString(fallback.resolve("entegre-updater/bootstrap-backup.jar"), "unknown backup");
            rejected(fallback, "Unknown backup");
            Files.delete(fallback.resolve("entegre-updater/bootstrap-backup.jar"));
            Files.createSymbolicLink(fallback.resolve("entegre-updater/bootstrap-backup.jar"), original.toAbsolutePath());
            rejected(fallback, "Symbolic backup");
            System.out.println("PASS shim: only pinned original backup can run after pre-launch validation failure");
        } finally {
            remove(base);
        }
    }

    static Path instance(Path base, String name, byte[] engine) throws IOException {
        Path game = Files.createDirectory(base.resolve(name));
        Files.createDirectory(game.resolve("entegre-updater"));
        Files.write(game.resolve("entegre-updater/engine.jar"), engine);
        Files.writeString(game.resolve("entegre-updater/engine.sha256"), Payloads.sha256(engine) + "\n");
        return game;
    }

    static byte[] fakeJar() throws IOException {
        var bytes = new java.io.ByteArrayOutputStream();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "BootstrapTest$FakeEngine");
        try (JarOutputStream jar = new JarOutputStream(bytes, manifest)) {
            jar.putNextEntry(new JarEntry("BootstrapTest$FakeEngine.class"));
            try (var source = BootstrapTest.class.getResourceAsStream("/BootstrapTest$FakeEngine.class")) {
                if (source == null) throw new IOException("Missing fake engine class");
                source.transferTo(jar);
            }
            jar.closeEntry();
        }
        return bytes.toByteArray();
    }

    private static void rejected(Path root, String label) throws Exception {
        boolean failed = false;
        try { Bootstrap.run(root, new String[]{"ignored"}); }
        catch (IOException expected) { failed = true; }
        require(failed, label + " unexpectedly executed");
        require(!Files.exists(root.resolve("observed.txt")), label + " started the child");
    }

    private static Process launchMain(Path game) throws IOException {
        String executable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString();
        String classpath = Arrays.stream(System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
                .map(path -> Path.of(path).toAbsolutePath().toString()).collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
        return new ProcessBuilder(executable, "-cp", classpath, "com.entegre.bootstrap.Bootstrap")
                .directory(game.toFile()).redirectErrorStream(true).start();
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void remove(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
}
