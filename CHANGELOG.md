# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-12-05

### Added
- Stable production release with strict thread-safe and deterministic contracts.
- Verified zero mandatory runtime dependencies in `mint-core`.
- Automated POSIX file security permissions and path traversal defense.
- `MintConfig` update mode support via `-Dmint.update=true` or `MINT_UPDATE=true`.

## [0.1.0] - 2025-10-11

### Added
- Core snapshot testing engine (`mint-core`) with zero mandatory runtime dependencies.
- Immutable `Snapshot` value object and `SnapshotComparator` with line-by-line diffing.
- Secure atomic `SnapshotStore` with path traversal guards and TOCTOU protection.
- String and optional Jackson JSON snapshot serializers (`mint-core`).
- JUnit 5 extension and `@EnableMintSnapshots` parameter resolver (`mint-junit5`).
- Continuous integration matrix workflow for Java 17 and 21.
