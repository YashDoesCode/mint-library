# Contributing to Mint

Thank you for your interest in contributing to Mint!

## Build & Test

- **Full build and verification:**
  ```bash
  mvn verify
  ```
- **Run a single test:**
  ```bash
  mvn test -Dtest=SnapshotStoreTest
  ```
- **Update snapshots intentionally:**
  ```bash
  mvn test -Dmint.update=true
  ```

## Commit Conventions

We adhere to [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` new feature or capability
- `fix:` bug fix
- `docs:` documentation updates
- `test:` test suite additions or refactors
- `refactor:` code improvements without behavior changes
- `chore:` build tooling or dependency updates

## Code Style & Architecture Guidelines

- **Target:** Java 17 baseline.
- **Dependencies:** `mint-core` must maintain zero mandatory runtime dependencies.
- **No Lombok:** Maintain explicit immutable value objects, records, and constructors.
- **I/O Safety:** Always use `java.nio.file` and `StandardCharsets.UTF_8`. Never use `java.io.File`, default platform charsets, or `System.out`/`System.err` in production code.
- **Encapsulation:** Every public class must be `final` unless explicitly designed for extension.

## Pull Request Checklist

Before submitting a PR:
1. Ensure all tests pass (`mvn verify`).
2. Verify that no snapshot files are modified unless accompanied by explicit evidence requiring `MINT_UPDATE`.
3. Check that public APIs include thorough Javadoc and package-info files.
