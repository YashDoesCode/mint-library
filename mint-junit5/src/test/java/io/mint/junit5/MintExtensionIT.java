package io.mint.junit5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableMintSnapshots(directory = "target/test-snapshots")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MintExtensionIT {

    private static final Path SNAPSHOT_DIR = Path.of("target", "test-snapshots");

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.exists(SNAPSHOT_DIR)) {
            try (Stream<Path> walk = Files.walk(SNAPSHOT_DIR)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
    }

    @Test
    @Order(1)
    void testFirstCallWritesFile(MintSnapshot mint) {
        Path snapFile = SNAPSHOT_DIR.resolve("testFirstCallWritesFile.snap");
        mint.assertMatches("initial-payload");
        assertTrue(Files.exists(snapFile));
    }

    @Test
    @Order(2)
    void testSameOutputPassesSilently(MintSnapshot mint) {
        mint.assertMatches("consistent-payload");
        assertDoesNotThrow(() -> mint.assertMatches("consistent-payload"));
    }

    @Test
    @Order(3)
    void testMismatchThrowsAssertionError(MintSnapshot mint) {
        mint.assertMatches("expected-value");
        AssertionError error = assertThrows(AssertionError.class, () -> mint.assertMatches("different-value"));
        assertTrue(error.getMessage().contains("Line 1"));
        assertTrue(error.getMessage().contains("expected-value"));
        assertTrue(error.getMessage().contains("different-value"));
    }
}
