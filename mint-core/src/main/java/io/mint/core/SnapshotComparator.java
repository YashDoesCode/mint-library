package io.mint.core;

import java.util.Objects;

/**
 * Compares snapshots and produces line-by-line diff results.
 */
public final class SnapshotComparator {

    /**
     * Result of a snapshot comparison.
     *
     * @param matches true if contents are identical
     * @param message detailed diff message if mismatch, or null
     */
    public record DiffResult(boolean matches, String message) {
    }

    /**
     * Constructs a {@code SnapshotComparator}.
     */
    public SnapshotComparator() {
    }

    /**
     * Compares an expected snapshot against an actual snapshot.
     *
     * @param expected the expected baseline snapshot
     * @param actual   the actual received snapshot
     * @return a {@link DiffResult} containing comparison outcome and diff
     * @throws NullPointerException if {@code expected} or {@code actual} is null
     */
    public DiffResult compare(Snapshot expected, Snapshot actual) {
        Objects.requireNonNull(expected, "expected snapshot must not be null");
        Objects.requireNonNull(actual, "actual snapshot must not be null");
        if (Objects.equals(expected.content(), actual.content())) {
            return new DiffResult(true, null);
        }
        String[] expLines = expected.content().split("\n", -1);
        String[] actLines = actual.content().split("\n", -1);
        int max = Math.max(expLines.length, actLines.length);
        StringBuilder sb = new StringBuilder("Snapshot mismatch for: ")
                .append(expected.name()).append("\n--- Expected\n+++ Actual\n");
        for (int i = 0; i < max; i++) {
            String exp = i < expLines.length ? expLines[i] : "<missing>";
            String act = i < actLines.length ? actLines[i] : "<missing>";
            if (!Objects.equals(exp, act)) {
                sb.append("  Line ").append(i + 1).append(":\n")
                  .append("  - ").append(exp).append("\n")
                  .append("  + ").append(act).append("\n");
            }
        }
        return new DiffResult(false, sb.toString().stripTrailing());
    }
}
