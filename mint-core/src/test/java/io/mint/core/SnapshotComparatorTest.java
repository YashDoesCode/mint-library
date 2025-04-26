package io.mint.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotComparatorTest {

    private final SnapshotComparator comparator = new SnapshotComparator();

    @Test
    void testEqualSnapshots() {
        Snapshot s1 = new Snapshot("snap", "line1\nline2\nline3");
        Snapshot s2 = new Snapshot("snap", "line1\nline2\nline3");

        SnapshotComparator.DiffResult result = comparator.compare(s1, s2);
        assertTrue(result.matches());
        assertNull(result.message());
    }

    @Test
    void testSingleLineMismatch() {
        Snapshot expected = new Snapshot("snap", "line1\nexpected-val\nline3");
        Snapshot actual = new Snapshot("snap", "line1\nactual-val\nline3");

        SnapshotComparator.DiffResult result = comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.message().contains("Snapshot mismatch for: snap"));
        assertTrue(result.message().contains("--- Expected"));
        assertTrue(result.message().contains("+++ Actual"));
        assertTrue(result.message().contains("Line 2:"));
        assertTrue(result.message().contains("- expected-val"));
        assertTrue(result.message().contains("+ actual-val"));
    }

    @Test
    void testMissingLine() {
        Snapshot expected = new Snapshot("snap", "line1\nline2");
        Snapshot actual = new Snapshot("snap", "line1");

        SnapshotComparator.DiffResult result = comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.message().contains("Line 2:"));
        assertTrue(result.message().contains("- line2"));
        assertTrue(result.message().contains("+ <missing>"));
    }

    @Test
    void testTrailingNewline() {
        Snapshot expected = new Snapshot("snap", "line1\n");
        Snapshot actual = new Snapshot("snap", "line1");

        SnapshotComparator.DiffResult result = comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.message().contains("Line 2:"));
    }
}
