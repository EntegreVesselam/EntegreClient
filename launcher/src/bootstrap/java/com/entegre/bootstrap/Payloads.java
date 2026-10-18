package com.entegre.bootstrap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Fixed-path, bounded payload reads shared by the shim and the one-time transition. */
public final class Payloads {
    public static final int MAX_JAR_BYTES = 16 * 1024 * 1024;
    public static final String ORIGINAL_SHA256 = "a8fbb24dc604278e97f4688e82d3d91a318b98efc08d5dbfcbcbcab6443d116c";
    public static final String DIRECTORY = "entegre-updater/";

    private Payloads() {}

    public static Path root(Path directory) throws IOException {
        if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Güvenli oyun dizini bulunamadı.");
        }
        return directory.toRealPath();
    }

    public static Path path(Path root, String relative) throws IOException {
        Path name = Path.of(relative);
        if (name.isAbsolute() || name.normalize().startsWith("..") || !name.equals(name.normalize())) {
            throw new IOException("Geçersiz güncelleyici dosyası.");
        }
        Path cursor = root;
        for (Path part : name) {
            cursor = cursor.resolve(part);
            if (Files.isSymbolicLink(cursor)) throw new IOException("Bağlantılı dosya kabul edilmedi.");
        }
        return cursor;
    }

    public static byte[] read(Path root, String relative, int limit) throws IOException {
        Path file = path(root, relative);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Güncelleyici dosyası eksik.");
        byte[] bytes;
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            long size = channel.size();
            if (size < 1 || size > limit) throw new IOException("Güncelleyici dosya boyutu geçersiz.");
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) < 0) throw new IOException("Eksik güncelleyici dosyası.");
            }
            if (channel.read(ByteBuffer.allocate(1)) != -1) throw new IOException("Güncelleyici dosyası değişti.");
            bytes = buffer.array();
        }
        path(root, relative);
        return bytes;
    }

    public static byte[] verified(Path root, String filename) throws IOException {
        String hash = new String(read(root, DIRECTORY + filename + ".sha256", 66), StandardCharsets.US_ASCII);
        if (!hash.matches("[0-9a-fA-F]{64}(?:\\r?\\n)?")) throw new IOException("Doğrulama kodu geçersiz.");
        byte[] payload = read(root, DIRECTORY + filename + ".jar", MAX_JAR_BYTES);
        if (!sha256(payload).equalsIgnoreCase(hash.strip())) throw new IOException("Güncelleyici doğrulanamadı.");
        return payload;
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    public static void writeForced(Path file, byte[] bytes) throws IOException {
        boolean created = false;
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW, LinkOption.NOFOLLOW_LINKS)) {
            created = true;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) channel.write(buffer);
            channel.force(true);
        } catch (IOException failure) {
            // Never leave an incomplete backup created by this attempt when it can be removed safely.
            if (created) {
                try { Files.deleteIfExists(file); } catch (IOException cleanup) { failure.addSuppressed(cleanup); }
            }
            throw failure;
        }
    }
}
