package io.mint.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotTest {

    @Test
    void testConstructorNullValidation() {
        assertThrows(NullPointerException.class, () -> new Snapshot(null, "content"));
        assertThrows(NullPointerException.class, () -> new Snapshot("name", null));
    }

    @Test
    void testAccessors() {
        Snapshot snapshot = new Snapshot("users", "user1,user2");
        assertEquals("users", snapshot.name());
        assertEquals("user1,user2", snapshot.content());
    }

    @Test
    void testEqualsAndHashCode() {
        Snapshot s1 = new Snapshot("test", "data");
        Snapshot s2 = new Snapshot("test", "data");
        Snapshot s3 = new Snapshot("other", "data");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "test");
    }

    @Test
    void testToStringRedaction() {
        Snapshot shortSnapshot = new Snapshot("short", "hello");
        assertEquals("Snapshot[name=short, content=hello]", shortSnapshot.toString());

        String longContent = "A".repeat(250);
        Snapshot longSnapshot = new Snapshot("long", longContent);
        String str = longSnapshot.toString();
        assertTrue(str.contains("... [truncated]"));
        assertTrue(str.startsWith("Snapshot[name=long, content=" + "A".repeat(200)));
    }
}
