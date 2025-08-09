package io.mint.junit5;

import io.mint.core.MintConfig;
import io.mint.core.Snapshot;
import io.mint.core.SnapshotComparator;
import io.mint.core.SnapshotSerializer;
import io.mint.core.SnapshotStore;

import java.util.Objects;

/**
 * Handle injected into JUnit 5 tests providing snapshot assertion methods.
 */
public final class MintSnapshot {

    private final SnapshotStore store;
    private final SnapshotSerializer serializer;
    private final SnapshotComparator comparator;
    private final String testName;

    MintSnapshot(SnapshotStore store, SnapshotSerializer serializer,
                 SnapshotComparator comparator, String testName) {
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.serializer = Objects.requireNonNull(serializer, "serializer must not be null");
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
        this.testName = Objects.requireNonNull(testName, "testName must not be null");
    }

    /**
     * Creates a new immutable {@code MintSnapshot} configured with a custom serializer.
     *
     * @param serializer the serializer to use, must not be null
     * @return a new {@code MintSnapshot} instance
     * @throws NullPointerException if {@code serializer} is null
     */
    public MintSnapshot with(SnapshotSerializer serializer) {
        Objects.requireNonNull(serializer, "serializer must not be null");
        return new MintSnapshot(this.store, serializer, this.comparator, this.testName);
    }

    /**
     * Asserts that the actual object matches the stored snapshot for this test.
     *
     * <p>First-run write semantics: On the first test execution when no baseline snapshot file
     * exists yet, this method writes the serialized actual output as the baseline snapshot and
     * returns successfully without failure. On subsequent runs, it compares the serialized actual
     * output against the stored snapshot, throwing an {@link AssertionError} with line-by-line diffs
     * if any mismatch is detected. If {@link MintConfig#UPDATE} is enabled, existing snapshots
     * are automatically overwritten with new values.
     *
     * @param actual the actual object to serialize and assert against baseline snapshot
     * @throws AssertionError if a snapshot exists, update mode is false, and output mismatches
     */
    public void assertMatches(Object actual) {
        String content = serializer.serialize(actual);
        Snapshot actualSnapshot = new Snapshot(testName, content);
        Snapshot expected = store.load(testName);
        if (expected == null || MintConfig.UPDATE) {
            store.save(actualSnapshot);
            return;
        }
        SnapshotComparator.DiffResult result = comparator.compare(expected, actualSnapshot);
        if (!result.matches()) {
            throw new AssertionError(result.message());
        }
    }
}
