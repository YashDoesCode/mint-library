package io.mint.core;

/**
 * Service provider interface for converting objects into string snapshot representations.
 *
 * <p>Implementors MUST be thread-safe and produce deterministic output. Implementations
 * must never return {@code null} from {@link #serialize(Object)}; throw a
 * {@link SnapshotException} instead if serialization cannot be completed.
 */
public interface SnapshotSerializer {

    /**
     * Serializes the given object into a snapshot string.
     *
     * @param object the object to serialize
     * @return the serialized snapshot string, never null
     * @throws SnapshotException if serialization fails
     */
    String serialize(Object object) throws SnapshotException;

    /**
     * Returns the format name of this serializer (e.g. "text", "json").
     *
     * @return the format name
     */
    String formatName();
}
