package io.mint.core.serializer;

import io.mint.core.SnapshotException;
import io.mint.core.SnapshotSerializer;

/**
 * Default serializer converting objects to their string representations.
 *
 * <p>This serializer is always safe to use with any object.
 */
public final class StringSnapshotSerializer implements SnapshotSerializer {

    /**
     * Constructs a new {@code StringSnapshotSerializer}.
     */
    public StringSnapshotSerializer() {
    }

    /**
     * Serializes an object to its string representation.
     *
     * @param object the object to serialize, may be null
     * @return {@code "null"} if object is null, the string content if {@link CharSequence},
     *         or {@link String#valueOf(Object)} otherwise
     * @throws SnapshotException never thrown by this implementation
     */
    @Override
    public String serialize(Object object) throws SnapshotException {
        if (object == null) {
            return "null";
        }
        if (object instanceof CharSequence) {
            return object.toString();
        }
        return String.valueOf(object);
    }

    /**
     * Returns the format name for text serialization.
     *
     * @return {@code "text"}
     */
    @Override
    public String formatName() {
        return "text";
    }
}
