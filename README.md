# Mint

> A micro-library for asserting complex system outputs

Mint simplifies regression testing by capturing complex outputs as deterministic snapshots.
Instead of writing fragile multi-field assertions, assert entire payloads with a single call.
Compare expected and actual outputs effortlessly across unit and integration tests.

<!-- TODO: Add build and release badges -->

## Quick start

Add `mint-junit5` to your test dependencies:

```xml
<dependency>
    <groupId>io.github.yashdoescodemint</groupId>
    <artifactId>mint-junit5</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

Annotate your test class with `@EnableMintSnapshots` and inject `MintSnapshot`:

```java
@EnableMintSnapshots
class UserServiceTest {

    @Test
    void testUserOutput(MintSnapshot mint) {
        String result = userService.generateReport();
        mint.assertMatches(result);
    }
}
```

Snapshots are stored in `src/test/resources/__snapshots__/<testName>.snap`.

> **CI Note:** Always commit generated `.snap` baseline snapshot files into version control.

## Updating snapshots

When intentional changes occur, update existing snapshots locally using:
```bash
mvn test -Dmint.update=true
```
In CI environments, never set `MINT_UPDATE` to ensure baseline snapshots are strictly validated.

## License

Licensed under the [MIT License](LICENSE).
