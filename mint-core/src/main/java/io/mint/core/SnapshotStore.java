package io.mint.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
     * Loads a snapshot by name from the store.
     *
     * @param name the snapshot name
     * @return the loaded {@link Snapshot}, or {@code null} if no snapshot file exists
     * @throws SnapshotException if an I/O error occurs or the snapshot name is invalid
     */
    public Snapshot load(String name) {
        Objects.requireNonNull(name, "Snapshot name must not be null");
        Path path = resolvePath(name);
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new Snapshot(name, content);
        } catch (NoSuchFileException e) {
            return null;
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
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
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
        Path resolved = snapshotDir.resolve(name + ".snap").normalize();
        if (!resolved.startsWith(snapshotDir)) {
            throw new SnapshotException("snapshot name escapes snapshot directory: " + name);
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
