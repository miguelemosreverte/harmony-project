# The development harness

The harness supplies the environment and evidence around the product. It owns disposable Canton participants, synthetic input, golden execution, verification, and local release assembly.

| Directory | Responsibility |
| --- | --- |
| `runner/` | Demo setup, story parsing/execution, focused verification, and tests |
| `model/` | Example inventory and pure expected/actual comparison shared with the reader |
| `ledger/` | Demo assembly, Daml tests, and metadata fixtures |
| `tools/` | Development command dispatch and release packaging |

The runner depends on the product service. Book export depends on the runner's recording interpretation. Tools compose both. The dependency never points from product to harness.

From the repository root:

```sh
scripts/build                  # Product, harness, and book builds
scripts/demo live              # Disposable authenticated demonstration
scripts/harmonia check         # Regular stories and direct/generated parity
scripts/harmonia live-check    # A separately launched product against a real ledger
scripts/check                  # Complete sequential suite
scripts/harmonia release-check # Same gates from a clean committed checkout, then package
```

Goldens are authored under [examples](../examples/README.md). The checker writes actual results and differences under ignored `.artifacts/`; it never updates an expectation to pass a test. The release requires all 32 matching recordings from its exact clean source revision.

`Demo` owns the network and sample parties. It can write a connection configuration for the standalone product. `ConfiguredService` launches that product using only its exported service classpath, so live verification exercises the real entry point. Demo credentials and sessions stay in private artifact directories.

Every process has a scoped owner. Build and check stages run sequentially, and a workspace lease prevents overlapping ledger environments. Ctrl-C closes the owned network. See [memory limits](../docs/architecture/scala.md) and [packaging](../docs/release/packaging.md).
