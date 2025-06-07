package io.mint.junit5;

import io.mint.core.SnapshotComparator;
import io.mint.core.SnapshotSerializer;
import io.mint.core.SnapshotStore;

/**
 * Snapshot assertion handle injected into test methods.
 */
public final class MintSnapshot {

    MintSnapshot(SnapshotStore store, SnapshotSerializer serializer,
                 SnapshotComparator comparator, String testName) {
    }
}
