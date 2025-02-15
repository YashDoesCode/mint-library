package io.mint.core;

import java.util.Objects;

/**
 * Immutable value object representing a named snapshot and its content.
 */
public final class Snapshot {

    private final String name;
    private final String content;

    /**
     * Constructs a new {@code Snapshot}.
     *
     * @param name    the snapshot name, must not be null
     * @param content the snapshot content, must not be null
     * @throws NullPointerException if {@code name} or {@code content} is null
     */
    public Snapshot(String name, String content) {
        this.name = Objects.requireNonNull(name, "Snapshot name must not be null");
        this.content = Objects.requireNonNull(content, "Snapshot content must not be null");
    }

    /**
     * Returns the snapshot name.
     *
     * @return the snapshot name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the snapshot content.
     *
     * @return the snapshot content
     */
    public String content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Snapshot snapshot = (Snapshot) o;
        return Objects.equals(name, snapshot.name) && Objects.equals(content, snapshot.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content);
    }

    @Override
    public String toString() {
        String displayedContent = content.length() > 200
                ? content.substring(0, 200) + "... [truncated]"
                : content;
        return "Snapshot[name=" + name + ", content=" + displayedContent + "]";
    }
}
