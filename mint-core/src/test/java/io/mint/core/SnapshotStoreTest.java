package io.mint.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadRoundtrip() {
        SnapshotStore store = new SnapshotStore(tempDir);
        Snapshot original = new Snapshot("user-profile", "{\"id\":1,\"name\":\"Alice\"}");

        store.save(original);
        Snapshot loaded = store.load("user-profile");

        assertNotNull(loaded);
        assertEquals(original.name(), loaded.name());
        assertEquals(original.content(), loaded.content());
    }

    @Test
    void testMissingSnapshotReturnsNull() {
        SnapshotStore store = new SnapshotStore(tempDir);
        assertNull(store.load("non-existent"));
    }

    @Test
    void testPathTraversalThrowsSnapshotException() {
        SnapshotStore store = new SnapshotStore(tempDir);
        assertThrows(SnapshotException.class, () -> store.load("../evil"));
        assertThrows(SnapshotException.class, () -> store.save(new Snapshot("../evil", "payload")));
    }

    @Test
    void testRejectBlankOrInvalidNames() {
        SnapshotStore store = new SnapshotStore(tempDir);
        assertThrows(SnapshotException.class, () -> store.load(""));
        assertThrows(SnapshotException.class, () -> store.load("   "));
        assertThrows(SnapshotException.class, () -> store.load("sub/dir"));
        assertThrows(SnapshotException.class, () -> store.load("sub\\dir"));
        assertThrows(SnapshotException.class, () -> store.load("test\0bad"));

        assertThrows(SnapshotException.class, () -> store.save(new Snapshot("", "payload")));
        assertThrows(SnapshotException.class, () -> store.save(new Snapshot("   ", "payload")));
        assertThrows(SnapshotException.class, () -> store.save(new Snapshot("sub/dir", "payload")));
        assertThrows(SnapshotException.class, () -> store.save(new Snapshot("sub\\dir", "payload")));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testSymlinkEscapeRejectedOnLoad() throws IOException {
        SnapshotStore store = new SnapshotStore(tempDir);
        Path externalTarget = Files.createTempFile("mint-outside-", ".snap");
        Files.writeString(externalTarget, "secret data");

        Path symlink = tempDir.resolve("escaped.snap");
        try {
            Files.createSymbolicLink(symlink, externalTarget);
            assertThrows(SnapshotException.class, () -> store.load("escaped"));
        } finally {
            Files.deleteIfExists(externalTarget);
            Files.deleteIfExists(symlink);
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testSymlinkOverwrittenSafelyOnSave() throws IOException {
        SnapshotStore store = new SnapshotStore(tempDir);
        Path externalTarget = Files.createTempFile("mint-target-", ".snap");
        Files.writeString(externalTarget, "original");

        Path symlink = tempDir.resolve("link-test.snap");
        try {
            Files.createSymbolicLink(symlink, externalTarget);
            store.save(new Snapshot("link-test", "safe-content"));

            assertFalse(Files.isSymbolicLink(symlink));
            assertEquals("safe-content", store.load("link-test").content());
            assertEquals("original", Files.readString(externalTarget));
        } finally {
            Files.deleteIfExists(externalTarget);
            Files.deleteIfExists(symlink);
        }
    }

    @Test
    void testMalformedUtf8ThrowsSnapshotException() throws IOException {
        SnapshotStore store = new SnapshotStore(tempDir);
        Path snapFile = tempDir.resolve("malformed.snap");
        // Write invalid UTF-8 byte sequence (0xFF, 0xFE are invalid UTF-8 start bytes)
        Files.write(snapFile, new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00});

        assertThrows(SnapshotException.class, () -> store.load("malformed"));
    }

    @Test
    void testAtomicOverwrite() {
        SnapshotStore store = new SnapshotStore(tempDir);
        Snapshot v1 = new Snapshot("config", "version=1");
        Snapshot v2 = new Snapshot("config", "version=2");

        store.save(v1);
        assertEquals("version=1", store.load("config").content());

        store.save(v2);
        assertEquals("version=2", store.load("config").content());
    }

    @Test
    void testConcurrentSavesToDifferentSnapshots() throws InterruptedException {
        SnapshotStore store = new SnapshotStore(tempDir);
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failures = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    store.save(new Snapshot("snap-" + index, "content-" + index));
                    Snapshot loaded = store.load("snap-" + index);
                    if (loaded == null || !("content-" + index).equals(loaded.content())) {
                        failures.incrementAndGet();
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
        assertEquals(0, failures.get());
    }
}
