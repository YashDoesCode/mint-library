package io.mint.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void testAtomicOverwrite() {
        SnapshotStore store = new SnapshotStore(tempDir);
        Snapshot v1 = new Snapshot("config", "version=1");
        Snapshot v2 = new Snapshot("config", "version=2");

        store.save(v1);
        assertEquals("version=1", store.load("config").content());

        store.save(v2);
        assertEquals("version=2", store.load("config").content());
    }
}
