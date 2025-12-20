```
  __  __ _____ _   _ _____ 
 |  \/  |_   _| \ | |_   _|
 | |\/| | | | |  \| | | |  
 | |  | |_| |_| |\  | | |  
 |_|  |_|_____|_| \_| |_|  
```

# Mint

> A micro-library for asserting complex system outputs in Java

[![CI](https://github.com/YashDoesCode/mint-library/actions/workflows/ci.yml/badge.svg)](https://github.com/YashDoesCode/mint-library/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](https://github.com/YashDoesCode/mint-library/releases/tag/v1.0.0)
[![JitPack](https://jitpack.io/v/YashDoesCode/mint-library.svg)](https://jitpack.io/#YashDoesCode/mint-library)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Why Mint?

- **Zero runtime dependencies**: `mint-core` has zero mandatory external dependencies.
- **Atomic & Secure storage**: Protects against TOCTOU, path traversals, and symlink attacks.
- **Frictionless JUnit 5**: Effortlessly inject `MintSnapshot` with automatic first-run baseline writes.

## Installation

```xml
<dependency>
    <groupId>com.github.YashDoesCode.mint-library</groupId>
    <artifactId>mint-junit5</artifactId>
    <version>v1.0.0</version>
    <scope>test</scope>
</dependency>
```

## Quick start

```java
@EnableMintSnapshots
class UserServiceTest {
    @Test
    void testUserReport(MintSnapshot mint) {
        String report = userService.generateReport();
        mint.assertMatches(report);
    }
}
```

Snapshots are stored at `src/test/resources/__snapshots__/<testName>.snap`. Commit `.snap` files into git.

## Updating snapshots

Update snapshots locally after intentional behavior changes:
```bash
mvn test -Dmint.update=true
```
Never enable update flags in CI.

## Serializers

- **StringSerializer (default)**: Serializes text, CharSequences, and `String.valueOf(obj)`.
- **JsonSnapshotSerializer (optional)**: Deterministic, indented, key-sorted JSON via Jackson:
  ```java
  mint.with(new JsonSnapshotSerializer()).assertMatches(userObject);
  ```

## Security notes

Mint applies path normalization, restricted POSIX permissions, and atomic moves to ensure safety. See [SECURITY.md](SECURITY.md) for our threat model.

## Release Notes (v1.0.0)

- **What shipped:** Production release of the Mint snapshot testing library. Includes `mint-core` engine, thread-safe `SnapshotSerializer` SPI, atomic `SnapshotStore` with POSIX permissions and TOCTOU protection, `MintConfig` update mode, optional Jackson JSON serialization, and full JUnit 5 `@EnableMintSnapshots` parameter resolver integration.
- **Breaking changes:** None.
- **Migration notes:** None.

## Roadmap

- XML snapshot serializer
- Java 9 `module-info` descriptors
- Spock framework test adapter

## License

Licensed under the [MIT License](LICENSE).
