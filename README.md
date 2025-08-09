# Mint

> A micro-library for asserting complex system outputs

Mint simplifies regression testing by capturing complex outputs as deterministic snapshots.
Instead of writing fragile multi-field assertions, assert entire payloads with a single call.
Compare expected and actual outputs effortlessly across unit and integration tests.

<!-- TODO: Add build and release badges -->

## Updating snapshots

When intentional changes occur, update existing snapshots locally using:
```bash
mvn test -Dmint.update=true
```
In CI environments, never set `MINT_UPDATE` to ensure baseline snapshots are strictly validated.

## License

Licensed under the [MIT License](LICENSE).
