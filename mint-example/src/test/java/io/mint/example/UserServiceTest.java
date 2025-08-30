package io.mint.example;

import io.mint.core.serializer.JsonSnapshotSerializer;
import io.mint.junit5.EnableMintSnapshots;
import io.mint.junit5.MintSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Example test demonstrating string and JSON snapshot assertions.
 *
 * <p>To update snapshots when baseline outputs change intentionally, run:
 * <pre>
 *   mvn test -Dmint.update=true
 * </pre>
 */
@EnableMintSnapshots
class UserServiceTest {

    @Test
    void testStringSnapshot(MintSnapshot mint) {
        String report = "USER REPORT\nID: 101\nName: Alice Smith\nStatus: ACTIVE";
        mint.assertMatches(report);
    }

    @Test
    void testJsonSnapshot(MintSnapshot mint) {
        Map<String, Object> user = Map.of(
                "id", 101,
                "name", "Alice Smith",
                "email", "alice@example.com",
                "roles", List.of("ADMIN", "USER")
        );
        mint.with(new JsonSnapshotSerializer()).assertMatches(user);
    }
}
