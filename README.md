```
  __  __ _____ _   _ _____ 
 |  \/  |_   _| \ | |_   _|
 | |\/| | | | |  \| | | |  
 | |  | |_| |_| |\  | | |  
 |_|  |_|_____|_| \_| |_|  
```

# Mint

> Lightweight, secure, and deterministic snapshot-testing library for Java 17+ and JUnit 5.

[![CI](https://github.com/YashDoesCode/mint-library/actions/workflows/ci.yml/badge.svg)](https://github.com/YashDoesCode/mint-library/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](https://github.com/YashDoesCode/mint-library/releases/tag/v1.0.0)
[![JitPack](https://jitpack.io/v/YashDoesCode/mint-library.svg)](https://jitpack.io/#YashDoesCode/mint-library)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Overview

Mint simplifies regression testing of complex Java data structures, API responses, generated documents, and system state by capturing golden baseline snapshots and verifying subsequent test outputs against them.

### Core Guarantees

- **Zero Mandatory Runtime Dependencies**: `mint-core` operates with 100% standard JDK 17+ APIs.
- **Defensive Filesystem Security**: Hardened against path traversal, symlink redirection, and TOCTOU races with atomic replacement semantics and strict POSIX file permissions.
- **Cross-Platform Determinism**: Normalizes line endings (`\r\n` and `\r` to `\n`) across Windows, macOS, and Linux checkouts.
- **Seamless JUnit 5 Integration**: Parameter resolution for `MintSnapshot`, full support for `@ParameterizedTest`, `@RepeatedTest`, `@Nested` suites, and multi-checkpoint assertions via `mint.named(...)`.
- **First-Run Baseline Workflow**: Automatically saves golden snapshots on first execution, failing on subsequent runs only when discrepancies are detected.

---

## Installation

Add Mint to your Maven build:

```xml
<dependency>
    <groupId>com.github.YashDoesCode.mint-library</groupId>
    <artifactId>mint-junit5</artifactId>
    <version>v1.0.0</version>
    <scope>test</scope>
</dependency>
```

If you only need the core snapshot engine without JUnit 5:

```xml
<dependency>
    <groupId>com.github.YashDoesCode.mint-library</groupId>
    <artifactId>mint-core</artifactId>
    <version>v1.0.0</version>
</dependency>
```

---

## Usage Guide

### 1. Basic Snapshot Testing

Annotate your test class with `@EnableMintSnapshots` and declare `MintSnapshot` as a test method parameter:

```java
import io.mint.junit5.EnableMintSnapshots;
import io.mint.junit5.MintSnapshot;
import org.junit.jupiter.api.Test;

@EnableMintSnapshots
class OrderReportTest {

    @Test
    void testOrderSummary(MintSnapshot mint) {
        String report = orderReportService.generateSummary(1001);
        mint.assertMatches(report);
    }
}
```

On the first run, Mint writes the baseline file to `src/test/resources/__snapshots__/testOrderSummary.snap`. On subsequent runs, it compares the actual output against the stored snapshot.

### 2. JSON Snapshots (Jackson)

Use `JsonSnapshotSerializer` for deterministic, indented JSON with keys ordered alphabetically:

```java
import io.mint.core.serializer.JsonSnapshotSerializer;

@Test
void testUserPayload(MintSnapshot mint) {
    User user = userService.findById(42);
    mint.with(new JsonSnapshotSerializer()).assertMatches(user);
}
```

*(Note: Requires optional `jackson-databind` on the test classpath).*

### 3. XML Snapshots (Standard JDK)

Use `XmlSnapshotSerializer` for deterministic XML formatting with zero third-party dependencies. It natively supports Java 17 records, POJOs, maps, collections, and DOM trees:

```java
import io.mint.core.serializer.XmlSnapshotSerializer;

public record Invoice(String id, double total, List<String> items) {}

@Test
void testInvoiceXml(MintSnapshot mint) {
    Invoice invoice = new Invoice("INV-2026", 99.50, List.of("Widget", "Cable"));
    mint.with(new XmlSnapshotSerializer()).assertMatches(invoice);
}
```

### 4. Multiple Checkpoints per Test (`mint.named`)

When asserting multiple intermediate steps within a single test method, use `named()` to avoid filename collisions:

```java
@Test
void testMultiStepWorkflow(MintSnapshot mint) {
    Workflow workflow = workflowService.initialize();
    mint.named("step1_initial").assertMatches(workflow.render());

    workflow.advance();
    mint.named("step2_advanced").assertMatches(workflow.render());
}
```

### 5. Parameterized and Repeated Tests

Mint automatically namespaces snapshots for JUnit 5 parameterized and repeated tests so each invocation receives an isolated snapshot:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedTest
@ValueSource(strings = {"bronze", "silver", "gold"})
void testTiers(String tier, MintSnapshot mint) {
    TierBenefits benefits = tierService.getBenefits(tier);
    // Writes snapshots: testTiers_1_bronze.snap, testTiers_2_silver.snap, etc.
    mint.assertMatches(benefits);
}
```

### 6. Nested Test Suites

Nested test classes inherit `@EnableMintSnapshots` configuration from enclosing classes and automatically namespace snapshots:

```java
import org.junit.jupiter.api.Nested;

@EnableMintSnapshots
class AccountServiceTest {

    @Nested
    class WhenAccountFrozen {

        @Test
        void shouldRejectWithdrawal(MintSnapshot mint) {
            // Snapshot saved as: WhenAccountFrozen_shouldRejectWithdrawal.snap
            mint.assertMatches(accountService.attemptWithdrawal(500));
        }
    }
}
```

### 7. Custom Snapshot Storage Directory

Configure custom snapshot storage locations using the `directory` attribute:

```java
@EnableMintSnapshots(directory = "src/test/resources/custom-snapshots")
class CustomDirectoryTest {
    // Snapshots will be written to and loaded from custom-snapshots/
}
```

### 8. Custom Serializer Implementation

Implement `SnapshotSerializer` to serialize proprietary domain objects:

```java
import io.mint.core.SnapshotSerializer;

public final class YamlSnapshotSerializer implements SnapshotSerializer {

    @Override
    public String serialize(Object object) {
        return YamlParser.dump(object);
    }

    @Override
    public String formatName() {
        return "yaml";
    }
}
```

---

## Updating Snapshots

When application output changes intentionally, update snapshots by passing the `mint.update` system property or environment variable:

```bash
# Update snapshots via Maven system property
mvn test -Dmint.update=true

# Or via environment variable
MINT_UPDATE=true mvn test
```

> **Warning:** Never enable update flags in automated CI environments. CI should run in verify mode to detect unexpected regressions.

---

## Architecture

```
mint/
├── mint-core/       # Core snapshot engine, storage, comparison, and serializers
├── mint-junit5/     # JUnit 5 extension, parameter resolver, and annotations
└── mint-example/    # Usage demonstrations and verification fixtures
```

- **`mint-core`**: Defines the `Snapshot` value object, `SnapshotStore` (atomic I/O), `SnapshotComparator` (unified diffing), and serializers (`StringSnapshotSerializer`, `JsonSnapshotSerializer`, `XmlSnapshotSerializer`).
- **`mint-junit5`**: Provides `@EnableMintSnapshots`, `MintExtension`, and the `MintSnapshot` assertion handle.

---

## Security Model

Mint is built defensively for modern build environments:
- **Strict Path Validation**: Snapshot names containing directory separators (`/`, `\`), null bytes (`\0`), or blank values are rejected with `SnapshotException`.
- **Symlink Containment**: Detects and refuses to follow symbolic links during snapshot loading to prevent sandbox escapes.
- **Atomic File Operations**: Uses temporary staging files with restrictive POSIX permissions (`rw-------`) and atomic directory moves to eliminate partial write corruption and TOCTOU race conditions.
- **Safe XML Processing**: Disables DOCTYPE declarations, external general/parameter entities, and external DTD access.

See [SECURITY.md](SECURITY.md) for complete details.

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) and adhere to [Conventional Commits](https://www.conventionalcommits.org/).

---

## License

This project is licensed under the [MIT License](LICENSE).
