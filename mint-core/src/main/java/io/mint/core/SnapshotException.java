package io.mint.core;

/**
 * Exception thrown when snapshot operations fail, including I/O failure,
 * serialization failure, or path traversal security violations.
 */
public final class SnapshotException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new snapshot exception with the specified detail message.
     *
     * @param message the detail message
     */
    public SnapshotException(String message) {
        super(message);
    }

    /**
     * Constructs a new snapshot exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public SnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}
