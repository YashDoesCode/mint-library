package io.mint.core.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.mint.core.SnapshotException;
import io.mint.core.SnapshotSerializer;

import java.util.Objects;

/**
 * Optional snapshot serializer formatting objects as indented, key-ordered JSON via Jackson.
 *
 * <p><strong>Security Note:</strong> Do NOT use with types that trigger polymorphic
 * deserialization on the consumer side. This class strictly performs serialization to text
 * snapshots and does not deserialize untrusted data.
 */
public final class JsonSnapshotSerializer implements SnapshotSerializer {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code JsonSnapshotSerializer} with standard indented output
     * and sorted map keys for deterministic formatting.
     */
    public JsonSnapshotSerializer() {
        this(new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));
    }

    /**
     * Constructs a new {@code JsonSnapshotSerializer} with a custom {@link ObjectMapper}.
     *
     * @param objectMapper the object mapper to use, must not be null
     * @throws NullPointerException if {@code objectMapper} is null
     */
    public JsonSnapshotSerializer(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    /**
     * Serializes an object to an indented JSON snapshot string.
     *
     * @param object the object to serialize, may be null
     * @return the serialized JSON string, never null
     * @throws SnapshotException if JSON processing fails
     */
    @Override
    public String serialize(Object object) throws SnapshotException {
        if (object == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SnapshotException("Failed to serialize object to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the format name for JSON serialization.
     *
     * @return {@code "json"}
     */
    @Override
    public String formatName() {
        return "json";
    }
}
