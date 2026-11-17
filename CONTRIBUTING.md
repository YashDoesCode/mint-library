# Contributing to Mint

Thank you for your interest in contributing to Mint! Mint is an open-source snapshot testing library designed for high correctness, security, and developer ergonomics.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher.
- **Apache Maven:** Version 3.8+ or higher.
- **Git:** Version 2.20+.

### Building and Testing

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YashDoesCode/mint-library.git
   cd mint-library
   ```

2. **Run the full test suite and build verification:**
   ```bash
   mvn verify
   ```

3. **Run a specific test class:**
   ```bash
   mvn test -pl mint-core -Dtest=SnapshotStoreTest -Dsurefire.failIfNoSpecifiedTests=false
   ```

4. **Update snapshot baselines intentionally:**
   ```bash
   mvn test -Dmint.update=true
   ```
   > **Note:** Only update snapshots when code changes intentionally modify expected output. Never update snapshots to mask unexpected regressions.

---

## Development Workflow & Commit Conventions

Mint follows [Conventional Commits v1.0.0](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>
```

### Allowed Types

- `feat`: A new feature or user-facing capability
- `fix`: A bug fix or security patch
- `test`: Adding or refactoring unit/integration tests
- `refactor`: Internal code improvements without functional changes
- `docs`: Documentation updates
- `build`: Build system or plugin configuration changes
- `ci`: Continuous integration workflow updates
- `chore`: Routine repository maintenance

### Scopes

- `core`: Changes in `mint-core`
- `junit5`: Changes in `mint-junit5`
- `example`: Changes in `mint-example`
- `security`: Security mitigations and vulnerability fixes
- `deps`: Dependency updates

---

## Engineering Standards

1. **Zero Runtime Dependencies Invariant:** `mint-core` must maintain zero mandatory runtime dependencies. Optional integrations (such as Jackson) must be marked `<optional>true</optional>`.
2. **Immutability & Encapsulation:** Public classes must be `final` unless explicitly intended for subclassing. Use records or immutable value objects with private final fields.
3. **Filesystem Safety:** Always use `java.nio.file` (`Path`, `Files`). Never use legacy `java.io.File`.
4. **Encoding:** All file and string operations must explicitly specify `StandardCharsets.UTF_8`.
5. **No Console Pollution:** Never use `System.out` or `System.err` in production library code.
6. **Documentation:** Every public class, method, and constructor must include comprehensive Javadoc documentation explaining parameters, return values, and exceptions.

---

## Pull Request Guidelines

1. Create a dedicated feature branch from `main`:
   ```bash
   git checkout -b feat/my-new-feature main
   ```
2. Verify that all tests pass before submitting (`mvn verify`).
3. Ensure no sensitive credentials or keys are committed (`gitleaks detect`).
4. Fill out the pull request template completely, including problem statement and test evidence.
