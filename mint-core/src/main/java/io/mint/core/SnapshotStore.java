package io.mint.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;

/**
 * Manages the loading and atomic saving of snapshots on the filesystem.
 */
public final class SnapshotStore {

    private final Path snapshotDir;

    /**
     * Constructs a {@code SnapshotStore} with the target directory.
     *
     * @param snapshotDir the base directory storing snapshot files
     * @throws NullPointerException if {@code snapshotDir} is null
     */
    public SnapshotStore(Path snapshotDir) {
        Objects.requireNonNull(snapshotDir, "snapshotDir must not be null");
        this.snapshotDir = snapshotDir.toAbsolutePath().normalize();
    }

    /**
     * Returns the base directory where snapshots are stored.
     *
     * @return the normalized absolute snapshot directory path
     */
    public Path snapshotDir() {
        return snapshotDir;
    }

    /**
     * Loads a snapshot by name from the store.
     *
     * @param name the snapshot name
     * @return the loaded {@link Snapshot}, or {@code null} if no snapshot file exists
     * @throws SnapshotException if an I/O error occurs, the snapshot name is invalid, or a symlink is encountered
     */
    public Snapshot load(String name) {
        Objects.requireNonNull(name, "Snapshot name must not be null");
        Path path = resolvePath(name);
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attrs.isSymbolicLink()) {
                throw new SnapshotException("Symbolic links are not permitted in snapshot storage: " + path);
            }
            if (!attrs.isRegularFile()) {
                throw new SnapshotException("Snapshot path is not a regular file: " + path);
            }
            byte[] bytes;
            try (InputStream in = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
                bytes = in.readAllBytes();
            }
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            String content = decoder.decode(ByteBuffer.wrap(bytes)).toString();
            return new Snapshot(name, content);
        } catch (NoSuchFileException e) {
            return null;
        } catch (CharacterCodingException e) {
            throw new SnapshotException("Malformed UTF-8 sequence in snapshot file: " + path, e);
        } catch (IOException e) {
            throw new SnapshotException("Failed to read snapshot file: " + path, e);
        }
    }

    /**
     * Atomically saves a snapshot to disk.
     *
     * @param snapshot the snapshot to save
     * @throws SnapshotException if directory creation or file write fails
     */
    public void save(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        ensureDirectoryExists();
        Path targetPath = resolvePath(snapshot.name());
        Path tempFile = null;
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            try {
                tempFile = Files.createTempFile(snapshotDir, ".snap-tmp-", ".tmp", attr);
            } catch (UnsupportedOperationException e) {
                tempFile = Files.createTempFile(snapshotDir, ".snap-tmp-", ".tmp");
            }
            Files.writeString(tempFile, snapshot.content(), StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                throw new SnapshotException("Atomic snapshot replacement is not supported on filesystem: " + targetPath, e);
            }
        } catch (IOException e) {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Suppress cleanup failure to surface primary exception
                }
            }
            throw new SnapshotException("Failed to save snapshot file: " + targetPath, e);
        }
    }

    private Path resolvePath(String name) {
        if (name.isBlank() || name.contains("/") || name.contains("\\") || name.indexOf('\0') != -1) {
            throw new SnapshotException("Invalid snapshot name: " + name);
        }
        Path resolved = snapshotDir.resolve(name + ".snap").normalize();
        if (!resolved.startsWith(snapshotDir) || !resolved.getParent().equals(snapshotDir)) {
            throw new SnapshotException("Snapshot name escapes snapshot directory: " + name);
        }
        return resolved;
    }

    private void ensureDirectoryExists() {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            Files.createDirectories(snapshotDir, attr);
        } catch (UnsupportedOperationException e) {
            try {
                Files.createDirectories(snapshotDir);
            } catch (IOException ioEx) {
                throw new SnapshotException("Failed to create snapshot directory: " + snapshotDir, ioEx);
            }
        } catch (IOException e) {
            throw new SnapshotException("Failed to create snapshot directory: " + snapshotDir, e);
        }
    }
}
