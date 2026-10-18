import com.entegre.bootstrap.Payloads;
import com.entegre.transition.BootstrapTransition;
import com.entegre.transition.BootstrapTransition.Result;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermission;

public final class TransitionTest {
    private static final String RELEASE_COMMAND = "\"$INST_JAVA\" -jar \"$INST_DIR/minecraft/packwiz-installer-bootstrap.jar\" "
            + "https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml";
    // Exact value shipped by the public v2.0 Prism profile: escaped quotes, no outer quotes.
    private static final String RELEASE_INI = "\\\"$INST_JAVA\\\" -jar \\\"$INST_DIR/minecraft/packwiz-installer-bootstrap.jar\\\" "
            + "https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml";
    private static byte[] original;
    private static byte[] shim;
    private static byte[] engine;

    public static void main(String[] args) throws Exception {
        original = Files.readAllBytes(Path.of(args.length == 0 ? "vendor/packwiz-installer-bootstrap.jar" : args[0]));
        check(Payloads.sha256(original).equals(Payloads.ORIGINAL_SHA256), "Unknown official fixture");
        shim = BootstrapTest.fakeJar();
        engine = BootstrapTest.fakeJar();
        Path base = Files.createTempDirectory("entegre-transition-tests-").toRealPath();
        try {
            String canonical = BootstrapTransition.SUPPORTED_COMMAND;
            String escapedCanonical = canonical.replace("\"", "\\\"");
            List<String> commands = List.of(canonical, escapedCanonical, "\"" + escapedCanonical + "\"",
                    RELEASE_COMMAND, RELEASE_INI, "\"" + RELEASE_INI + "\"");
            for (int format = 0; format < commands.size(); format++) {
                String command = commands.get(format);
                Path game = instance(base, "install-format-" + format, command, format >= 3 ? "minecraft" : ".minecraft");
                byte[] cfg = Files.readAllBytes(game.getParent().resolve("instance.cfg"));
                byte[] personal = "synthetic personal data\u0000".getBytes(StandardCharsets.UTF_8);
                Files.write(game.resolve("options.txt"), personal);
                Files.createDirectory(game.resolve("saves"));
                Files.write(game.resolve("saves/synthetic.dat"), personal);
                check(BootstrapTransition.apply(game) == Result.INSTALLED, "Known original was not migrated");
                check(Arrays.equals(Files.readAllBytes(target(game)), shim), "Replacement differs");
                check(Arrays.equals(Files.readAllBytes(backup(game)), original), "Original backup differs");
                var stamp = Files.getLastModifiedTime(target(game));
                check(BootstrapTransition.apply(game) == Result.ALREADY_INSTALLED, "Repeat not idempotent");
                check(Files.getLastModifiedTime(target(game)).equals(stamp), "Repeat rewrote the bootstrap");
                check(Arrays.equals(Files.readAllBytes(game.getParent().resolve("instance.cfg")), cfg), "instance.cfg modified");
                check(Arrays.equals(Files.readAllBytes(game.resolve("options.txt")), personal), "Personal options modified");
                check(Arrays.equals(Files.readAllBytes(game.resolve("saves/synthetic.dat")), personal), "Personal world modified");
                noStage(game);
            }
            System.out.println("PASS transition: all 6 exact command formats including public v2.0 minecraft profile; backup/repeat/personal files retained");

            Path unknown = instance(base, "unknown", false);
            byte[] other = "some other bootstrap".getBytes(StandardCharsets.UTF_8);
            Files.write(target(unknown), other);
            check(BootstrapTransition.apply(unknown) == Result.SKIPPED, "Unknown bootstrap not skipped");
            check(Arrays.equals(Files.readAllBytes(target(unknown)), other), "Unknown bootstrap overwritten");
            Path absent = instance(base, "absent", false);
            Files.delete(target(absent));
            check(BootstrapTransition.apply(absent) == Result.SKIPPED && !Files.exists(target(absent)), "Absent bootstrap created");
            Path disabled = instance(base, "disabled", false);
            Files.writeString(disabled.resolve(".entegre-updater-disabled"), "");
            unchanged(disabled, Result.SKIPPED);
            for (String command : List.of("java -jar packwiz-installer-bootstrap.jar", BootstrapTransition.SUPPORTED_COMMAND + " --flag",
                    RELEASE_INI + " --flag", RELEASE_INI.replace("/minecraft/", "/other-instance/"),
                    RELEASE_INI.replace("/main/pack.toml", "/other/pack.toml"))) {
                Path game = instance(base, "unsupported-" + Math.abs(command.hashCode()), command, "minecraft");
                unchanged(game, Result.SKIPPED);
            }
            Path wrongDirectory = instance(base, "release-command-wrong-directory", RELEASE_INI, ".minecraft");
            unchanged(wrongDirectory, Result.SKIPPED);
            Path global = instance(base, "global-command", false);
            Files.writeString(global.getParent().resolve("instance.cfg"), "[General]\nPreLaunchCommand=" + BootstrapTransition.SUPPORTED_COMMAND + "\n");
            unchanged(global, Result.SKIPPED);
            System.out.println("PASS transition: unknown/absent bootstrap, disabled marker and unsupported/global commands skipped");

            for (String payload : List.of("engine", "bootstrap")) {
                Path missing = instance(base, "missing-" + payload, false);
                Files.delete(missing.resolve("entegre-updater/" + payload + ".jar"));
                unchanged(missing, Result.FAILED);
                Path badHash = instance(base, "bad-hash-" + payload, false);
                Files.writeString(badHash.resolve("entegre-updater/" + payload + ".sha256"), "0".repeat(64));
                unchanged(badHash, Result.FAILED);
                Path truncated = instance(base, "truncated-" + payload, false);
                Files.write(truncated.resolve("entegre-updater/" + payload + ".jar"), new byte[]{1, 2, 3});
                unchanged(truncated, Result.FAILED);
                Path malformed = instance(base, "malformed-" + payload, false);
                Files.writeString(malformed.resolve("entegre-updater/" + payload + ".sha256"), Payloads.sha256(shim) + " extra");
                unchanged(malformed, Result.FAILED);
            }
            System.out.println("PASS transition: both payloads checked; wrong hash, truncation and malformed checksum fail closed");

            for (String relative : List.of("entegre-updater/engine.jar", "entegre-updater/engine.sha256",
                    "entegre-updater/bootstrap.jar", "entegre-updater/bootstrap.sha256", "packwiz-installer-bootstrap.jar")) {
                Path game = instance(base, "link-" + Math.abs(relative.hashCode()), false);
                Path outside = base.resolve("external-" + Math.abs(relative.hashCode()));
                Files.move(game.resolve(relative), outside);
                byte[] before = Files.readAllBytes(outside);
                Files.createSymbolicLink(game.resolve(relative), outside);
                check(BootstrapTransition.apply(game) == Result.FAILED, "Symbolic file accepted");
                check(Files.isSymbolicLink(game.resolve(relative)), "Symbolic file replaced");
                check(Arrays.equals(Files.readAllBytes(outside), before), "Symbol target changed");
                noStage(game);
            }
            Path directoryLink = instance(base, "directory-link", false);
            Path outside = base.resolve("outside-managed");
            Files.move(directoryLink.resolve("entegre-updater"), outside);
            Files.createSymbolicLink(directoryLink.resolve("entegre-updater"), outside);
            unchanged(directoryLink, Result.FAILED);
            Path cfgLink = instance(base, "cfg-link", false);
            Path cfgOutside = base.resolve("outside-instance.cfg");
            Files.move(cfgLink.getParent().resolve("instance.cfg"), cfgOutside);
            Files.createSymbolicLink(cfgLink.getParent().resolve("instance.cfg"), cfgOutside);
            unchanged(cfgLink, Result.FAILED);
            System.out.println("PASS transition: symbolic payloads, target, managed directory and configuration rejected");

            Path collision = instance(base, "backup-collision", false);
            Files.writeString(backup(collision), "unknown backup");
            unchanged(collision, Result.FAILED);
            check(Files.readString(backup(collision)).equals("unknown backup"), "Backup collision overwritten");
            Path backupLink = instance(base, "backup-link", false);
            Path pinnedOutside = base.resolve("outside-original.jar");
            Files.write(pinnedOutside, original);
            Files.createSymbolicLink(backup(backupLink), pinnedOutside);
            unchanged(backupLink, Result.FAILED);
            Path retry = instance(base, "existing-backup-retry", false);
            Files.write(backup(retry), original);
            check(BootstrapTransition.apply(retry) == Result.INSTALLED, "Verified backup did not permit retry");
            System.out.println("PASS transition: backup collision/link preserved; verified prior backup permits retry");

            // Invoke the production move, without a failure-injection hook, against a real incompatible target.
            Path moveRoot = Files.createDirectory(base.resolve("move-failure"));
            Path staged = Files.write(moveRoot.resolve("staged.jar"), shim);
            Path blocked = Files.createDirectory(moveRoot.resolve("target"));
            Path sentinel = Files.write(blocked.resolve("original.jar"), original);
            var move = BootstrapTransition.class.getDeclaredMethod("atomicReplace", Path.class, Path.class);
            move.setAccessible(true);
            boolean failed = false;
            try { move.invoke(null, staged, blocked); }
            catch (InvocationTargetException expected) { failed = expected.getCause() instanceof IOException; }
            check(failed, "Incompatible atomic target unexpectedly replaced");
            check(Arrays.equals(Files.readAllBytes(sentinel), original) && Files.exists(staged), "Failed move changed either side");

            Path publication = instance(base, "backup-publication-failure", false);
            Files.writeString(backup(publication), "existing unknown backup");
            Path backupStage = Files.write(publication.resolve("entegre-updater/staged-original.tmp"), original);
            var publish = BootstrapTransition.class.getDeclaredMethod("publishBackup", Path.class, Path.class);
            publish.setAccessible(true);
            failed = false;
            try { publish.invoke(null, backupStage, backup(publication)); }
            catch (InvocationTargetException expected) { failed = expected.getCause() instanceof java.nio.file.FileAlreadyExistsException; }
            check(failed, "Backup publication replaced an existing file");
            check(Files.readString(backup(publication)).equals("existing unknown backup"), "Colliding backup changed");
            check(Arrays.equals(Files.readAllBytes(target(publication)), original), "Publication failure changed bootstrap");
            Files.delete(backup(publication));
            Files.delete(backupStage);
            check(BootstrapTransition.apply(publication) == Result.INSTALLED, "Publication recovery did not retry");

            Path readonly = instance(base, "readonly-parent", false);
            if (Files.getFileStore(readonly).supportsFileAttributeView("posix")) {
                Set<PosixFilePermission> before = Files.getPosixFilePermissions(readonly);
                try {
                    Files.setPosixFilePermissions(readonly, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
                    if (Files.isWritable(readonly)) throw new AssertionError("Read-only fixture ineffective under this user");
                    unchanged(readonly, Result.FAILED);
                } finally { Files.setPosixFilePermissions(readonly, before); }
                check(BootstrapTransition.apply(readonly) == Result.INSTALLED, "Filesystem recovery did not permit retry");

                Path backupReadonly = instance(base, "readonly-backup-directory", false);
                Path managed = backupReadonly.resolve("entegre-updater");
                Set<PosixFilePermission> managedBefore = Files.getPosixFilePermissions(managed);
                try {
                    Files.setPosixFilePermissions(managed, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
                    unchanged(backupReadonly, Result.FAILED);
                    check(!Files.exists(backup(backupReadonly)), "Failed backup write left final backup");
                } finally { Files.setPosixFilePermissions(managed, managedBefore); }
                check(BootstrapTransition.apply(backupReadonly) == Result.INSTALLED, "Backup write recovery did not retry");
            } else {
                System.out.println("SKIP POSIX permission test on this filesystem; incompatible atomic target test still ran");
            }
            System.out.println("PASS transition: real filesystem move/write failures keep original; recovery retries safely");
        } finally {
            BootstrapTest.remove(base);
        }
    }

    private static Path instance(Path base, String name, boolean escaped) throws IOException {
        String command = BootstrapTransition.SUPPORTED_COMMAND;
        if (escaped) command = "\"" + command.replace("\"", "\\\"") + "\"";
        return instance(base, name, command, ".minecraft");
    }

    private static Path instance(Path base, String name, String command, String directoryName) throws IOException {
        Path parent = Files.createDirectory(base.resolve(name));
        Path game = Files.createDirectory(parent.resolve(directoryName));
        Files.createDirectory(game.resolve("entegre-updater"));
        Files.writeString(parent.resolve("instance.cfg"), "[General]\nOverrideCommands=true\nPreLaunchCommand=" + command + "\n");
        Files.write(target(game), original);
        Files.write(game.resolve("entegre-updater/engine.jar"), engine);
        Files.writeString(game.resolve("entegre-updater/engine.sha256"), Payloads.sha256(engine) + "\n");
        Files.write(game.resolve("entegre-updater/bootstrap.jar"), shim);
        Files.writeString(game.resolve("entegre-updater/bootstrap.sha256"), Payloads.sha256(shim) + "\n");
        return game;
    }

    private static Path target(Path game) { return game.resolve("packwiz-installer-bootstrap.jar"); }
    private static Path backup(Path game) { return game.resolve("entegre-updater/bootstrap-backup.jar"); }
    private static void unchanged(Path game, Result expected) throws IOException {
        check(BootstrapTransition.apply(game) == expected, "Unexpected transition result for " + game.getFileName());
        check(Arrays.equals(Files.readAllBytes(target(game)), original), "Original bootstrap changed after refused transition");
        noStage(game);
    }
    private static void noStage(Path game) throws IOException {
        try (var paths = Files.list(game)) {
            check(paths.noneMatch(path -> path.getFileName().toString().startsWith(".entegre-bootstrap-")), "Staging file leaked");
        }
        Path managed = game.resolve("entegre-updater");
        if (Files.isDirectory(managed)) {
            try (var paths = Files.list(managed)) {
                check(paths.noneMatch(path -> path.getFileName().toString().startsWith(".bootstrap-backup-")), "Backup staging file leaked");
            }
        }
    }
    private static void check(boolean condition, String message) { BootstrapTest.require(condition, message); }
}
