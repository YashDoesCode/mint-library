package io.mint.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void testCrlfNormalizationMatchesLf() {
        Snapshot expected = new Snapshot("snap", "line1\r\nline2\r\nline3");
        Snapshot actual = new Snapshot("snap", "line1\nline2\nline3");

        SnapshotComparator.DiffResult result = comparator.compare(expected, actual);
        assertTrue(result.matches(), "Default comparator should normalize CRLF and LF");
        assertNull(result.message());
    }

    @Test
    void testStrictLineEndingMismatchWhenNormalizationDisabled() {
        SnapshotComparator strictComparator = new SnapshotComparator(false);
        Snapshot expected = new Snapshot("snap", "line1\r\nline2");
        Snapshot actual = new Snapshot("snap", "line1\nline2");

        SnapshotComparator.DiffResult result = strictComparator.compare(expected, actual);
        assertFalse(result.matches(), "Strict comparator should detect CRLF vs LF");
        assertTrue(result.message().contains("Snapshot mismatch"));
    }

    @Test
    void testEmptyContentHandling() {
        Snapshot expected = new Snapshot("snap", "");
        Snapshot actual = new Snapshot("snap", "line1");

        SnapshotComparator.DiffResult result = comparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.message().contains("Line 1:"));

        Snapshot bothEmpty1 = new Snapshot("snap", "");
        Snapshot bothEmpty2 = new Snapshot("snap", "");
        assertTrue(comparator.compare(bothEmpty1, bothEmpty2).matches());
    }

    @Test
    void testNullSnapshotThrowsNpe() {
        Snapshot actual = new Snapshot("snap", "line1");
        assertThrows(NullPointerException.class, () -> comparator.compare(null, actual));
        assertThrows(NullPointerException.class, () -> comparator.compare(actual, null));
    }

    @Test
    void testMaxDiffLimiting() {
        SnapshotComparator limitedComparator = new SnapshotComparator(true, 5);
        StringBuilder exp = new StringBuilder();
        StringBuilder act = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            exp.append("line-").append(i).append("-exp\n");
            act.append("line-").append(i).append("-act\n");
        }
        Snapshot expected = new Snapshot("snap", exp.toString());
        Snapshot actual = new Snapshot("snap", act.toString());

        SnapshotComparator.DiffResult result = limitedComparator.compare(expected, actual);
        assertFalse(result.matches());
        assertTrue(result.message().contains("more line differences"));
    }
}
