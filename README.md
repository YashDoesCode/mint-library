# Mint

> A micro-library for asserting complex system outputs

Mint simplifies regression testing by capturing complex outputs as deterministic snapshots.
Instead of writing fragile multi-field assertions, assert entire payloads with a single call.
Compare expected and actual outputs effortlessly across unit and integration tests.

<!-- TODO: Add build and release badges -->

## Installation

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    testImplementation 'com.github.YashDoesCode.mint-library:mint-junit5:v0.1.0'
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.YashDoesCode.mint-library</groupId>
    <artifactId>mint-junit5</artifactId>
    <version>v0.1.0</version>
    <scope>test</scope>
</dependency>
```

## Quick start

Add `mint-junit5` to your test dependencies:

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
