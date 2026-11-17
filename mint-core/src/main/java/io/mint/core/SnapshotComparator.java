package io.mint.core;

import java.util.Objects;

/**
 * Compares snapshots and produces line-by-line diff results.
 *
 * <p>By default, line endings (\r\n and \r) are normalized to \n prior to comparison
 * to ensure deterministic cross-platform execution on Windows, Linux, and macOS.
 */
public final class SnapshotComparator {

    private static final int DEFAULT_MAX_DIFFS = 100;

    private final boolean normalizeLineEndings;
    private final int maxDiffs;

    /**
     * Result of a snapshot comparison.
     *
     * @param matches true if contents are identical
     * @param message detailed diff message if mismatch, or null
     */
    public record DiffResult(boolean matches, String message) {
    }

    /**
     * Constructs a {@code SnapshotComparator} with line-ending normalization enabled
     * and a default maximum of 100 diff entries.
     */
    public SnapshotComparator() {
        this(true, DEFAULT_MAX_DIFFS);
    }

    /**
     * Constructs a {@code SnapshotComparator} with specified line-ending normalization.
     *
     * @param normalizeLineEndings {@code true} to normalize CRLF and CR to LF before comparison
     */
    public SnapshotComparator(boolean normalizeLineEndings) {
        this(normalizeLineEndings, DEFAULT_MAX_DIFFS);
    }

    /**
     * Constructs a {@code SnapshotComparator} with specified line-ending normalization
     * and maximum diff entries.
     *
     * @param normalizeLineEndings {@code true} to normalize CRLF and CR to LF before comparison
     * @param maxDiffs             maximum number of mismatched line entries to include in diff message
     * @throws IllegalArgumentException if {@code maxDiffs} is less than 1
     */
    public SnapshotComparator(boolean normalizeLineEndings, int maxDiffs) {
        if (maxDiffs < 1) {
            throw new IllegalArgumentException("maxDiffs must be at least 1");
        }
        this.normalizeLineEndings = normalizeLineEndings;
        this.maxDiffs = maxDiffs;
    }

    /**
     * Compares an expected snapshot against an actual snapshot.
     *
     * @param expected the expected baseline snapshot, must not be null
     * @param actual   the actual received snapshot, must not be null
     * @return a {@link DiffResult} containing comparison outcome and diff
     * @throws NullPointerException if {@code expected} or {@code actual} is null
     */
    public DiffResult compare(Snapshot expected, Snapshot actual) {
        Objects.requireNonNull(expected, "expected snapshot must not be null");
        Objects.requireNonNull(actual, "actual snapshot must not be null");

        String expContent = normalizeLineEndings ? normalize(expected.content()) : expected.content();
        String actContent = normalizeLineEndings ? normalize(actual.content()) : actual.content();

        if (Objects.equals(expContent, actContent)) {
            return new DiffResult(true, null);
        }

        String[] expLines = expContent.split("\n", -1);
        String[] actLines = actContent.split("\n", -1);
        int max = Math.max(expLines.length, actLines.length);

        StringBuilder sb = new StringBuilder("Snapshot mismatch for: ")
                .append(expected.name()).append("\n--- Expected\n+++ Actual\n");

        int diffCount = 0;
        int totalDiffs = 0;
        for (int i = 0; i < max; i++) {
            String exp = i < expLines.length ? expLines[i] : "<missing>";
            String act = i < actLines.length ? actLines[i] : "<missing>";
            if (!Objects.equals(exp, act)) {
                totalDiffs++;
                if (diffCount < maxDiffs) {
                    diffCount++;
                    sb.append("  Line ").append(i + 1).append(":\n")
                            .append("  - ").append(exp).append("\n")
                            .append("  + ").append(act).append("\n");
                }
            }
        }

        if (totalDiffs > maxDiffs) {
            sb.append("  ... and ").append(totalDiffs - maxDiffs).append(" more line differences\n");
        }

        return new DiffResult(false, sb.toString().stripTrailing());
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}
