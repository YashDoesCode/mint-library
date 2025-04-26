package io.mint.core;

import io.mint.core.serializer.StringSnapshotSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringSnapshotSerializerTest {

    private final StringSnapshotSerializer serializer = new StringSnapshotSerializer();

    @Test
    void testSerializeNull() {
        assertEquals("null", serializer.serialize(null));
    }

    @Test
    void testSerializeCharSequence() {
        assertEquals("hello world", serializer.serialize("hello world"));
        assertEquals("buffer", serializer.serialize(new StringBuilder("buffer")));
    }

    @Test
    void testSerializeObject() {
        assertEquals("12345", serializer.serialize(12345));
    }

    @Test
    void testFormatName() {
        assertEquals("text", serializer.formatName());
    }
}
