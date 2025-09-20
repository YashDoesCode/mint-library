# Security Policy and Threat Model

## Threat Model

Mint is designed with defensive patterns to prevent common security vulnerabilities in test execution environments:

### 1. Path Traversal Attacks
- **Threat:** Malicious snapshot names (e.g., `../../etc/passwd` or `../evil`) attempting to write or read outside designated snapshot storage.
- **Mitigation:** Implemented in `io.mint.core.SnapshotStore#resolvePath(String)`. All snapshot names have `.snap` appended, are normalized via `Path#normalize()`, and explicitly verified to start with the normalized `snapshotDir` via `Path#startsWith(Path)`. Any violation throws a `SnapshotException`.

### 2. Symlink & TOCTOU (Time-of-Check to Time-of-Use) Attacks
- **Threat:** Race conditions during snapshot reading/writing or symlink redirection.
- **Mitigation:** Implemented in `io.mint.core.SnapshotStore#save(Snapshot)` and `SnapshotStore#load(String)`. Reads directly catch `java.nio.file.NoSuchFileException` without vulnerable pre-checks (`Files.exists()`). Writes are staged to temporary files (`Files.createTempFile`) with restrictive POSIX permissions (`rw-------`) and moved atomically via `StandardCopyOption.ATOMIC_MOVE` with `REPLACE_EXISTING`.

### 3. Java Deserialization Remote Code Execution (RCE)
- **Threat:** Native Java object deserialization (`ObjectInputStream`) executing untrusted bytecode payloads.
- **Mitigation:** Mint completely avoids Java native serialization. `io.mint.core.SnapshotSerializer` is strictly unidirectional (converting runtime objects into UTF-8 text). Optional JSON serialization in `io.mint.core.serializer.JsonSnapshotSerializer` only serializes out to JSON format and never deserializes incoming data payloads into executable objects.

### 4. Insecure Snapshot Directory Permissions
- **Threat:** Broad filesystem permissions allowing other local users to inspect or tamper with snapshot files.
- **Mitigation:** Implemented in `io.mint.core.SnapshotStore#ensureDirectoryExists()`. Snapshot directories are created with POSIX attributes `rwx------` (owner read/write/execute only).

## Reporting a Vulnerability

If you discover a security vulnerability in Mint, please report it responsibly:

1. **Do not** file a public GitHub issue.
2. Email security findings to the maintainer at `yash@users.noreply.github.com`.
3. Include detailed reproduction steps and affected version numbers.
4. Maintainers will acknowledge reports within 48 hours and coordinate a security fix.
