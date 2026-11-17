package io.mint.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableMintSnapshots(directory = "target/test-snapshots-ext")
class MintExtensionTest {

    private static final Path SNAPSHOT_DIR = Path.of("target", "test-snapshots-ext");

    @AfterAll
    static void tearDownAll() throws IOException {
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
    void testStandardSnapshotNaming(MintSnapshot mint) {
        assertEquals("testStandardSnapshotNaming", mint.testName());
        mint.assertMatches("standard-content");
        assertTrue(Files.exists(SNAPSHOT_DIR.resolve("testStandardSnapshotNaming.snap")));
    }

    @Test
    void testCustomNamedSnapshot(MintSnapshot mint) {
        MintSnapshot custom = mint.named("custom_checkpoint");
        assertEquals("custom_checkpoint", custom.testName());
        custom.assertMatches("checkpoint-data");
        assertTrue(Files.exists(SNAPSHOT_DIR.resolve("custom_checkpoint.snap")));

        assertThrows(NullPointerException.class, () -> mint.named(null));
        assertThrows(IllegalArgumentException.class, () -> mint.named("  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void testParameterizedTestIsolation(String input, MintSnapshot mint) {
        mint.assertMatches("param-" + input);
        assertTrue(mint.testName().startsWith("testParameterizedTestIsolation_"));
        assertTrue(Files.exists(SNAPSHOT_DIR.resolve(mint.testName() + ".snap")));
    }

    @RepeatedTest(2)
    void testRepeatedTestIsolation(MintSnapshot mint) {
        mint.assertMatches("repeated-run");
        assertTrue(mint.testName().startsWith("testRepeatedTestIsolation_"));
        assertTrue(Files.exists(SNAPSHOT_DIR.resolve(mint.testName() + ".snap")));
    }

    @Nested
    class WhenNestedContext {

        @Test
        void testNestedSnapshotNamingAndInheritedAnnotation(MintSnapshot mint) {
            assertTrue(mint.testName().contains("WhenNestedContext_testNestedSnapshotNamingAndInheritedAnnotation"),
                    "Expected nested prefix but was: " + mint.testName());
            mint.assertMatches("nested-content");
            assertTrue(Files.exists(SNAPSHOT_DIR.resolve(mint.testName() + ".snap")));
        }
    }
}
