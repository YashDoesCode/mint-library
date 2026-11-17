# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `XmlSnapshotSerializer` decoupled from `java.desktop` and `java.beans`, featuring deterministic property sorting, native Java 17 record support, POJO reflection, and circular reference guards.
- JUnit 5 test isolation in `MintExtension` for `@ParameterizedTest`, `@RepeatedTest`, and `@Nested` test suites.
- `MintSnapshot.named(String)` method allowing multiple independent snapshot assertions within a single test method.
- Automatic line ending normalization (`\r\n` and `\r` to `\n`) in `SnapshotComparator` for cross-platform test stability on Windows checkouts.
- Configurable `maxDiffs` threshold in `SnapshotComparator` with readable truncation diagnostics for large diffs.
- Contributor pull request template (`.github/PULL_REQUEST_TEMPLATE.md`).
- Automated Gitleaks secret detection workflow in GitHub Actions.

### Fixed
- Hardened `SnapshotStore` path validation to strictly reject names containing directory separators (`/`, `\`), null bytes (`\0`), or blank strings.
- Closed symlink redirection and TOCTOU race window during snapshot loading via `NOFOLLOW_LINKS` streams.
- Enforced strict UTF-8 charset decoding in `SnapshotStore` with `CodingErrorAction.REPORT` instead of silent character substitution.
- Configured Maven Surefire plugin management to automatically discover and execute integration test suites matching `**/*IT.java`.
- Corrected DeepSource CLI execution path in CI workflow.

### Security
- Enforced atomic replacement semantics with clear failure propagation via `SnapshotException` when unsupported by the filesystem.
- Hardened XML parsing and transformation against XXE, external general/parameter entities, and external DTD access.

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
