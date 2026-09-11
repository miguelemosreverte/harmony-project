# Harmonia

Compose independently owned Canton/Daml applications into a shared workflow. The ledger authorizes actions and records progression; the Scala service observes state and submits commands; the browser makes the workflow visible.

## Read the product

Start in **[product/](product/README.md)**. It contains the contracts, service, shared API types, live browser, and reviewed package mappings. The [reading guide](docs/fourth-draft/reading-guide.md) follows one operation through those owners.

```text
product/     Contracts, service, API, live browser, package inputs
book/        Chapters, recorded browser, recording models, exporter
harness/     Disposable networks, golden runners, tests, release tools
examples/    Readable inputs and committed independent expectations
docs/        Architecture and acceptance; prior plans under history/
scripts/     Small build and launch commands
build.sbt    The compilation boundaries
```

The product compiles and runs without book or harness classes. The book consumes recorded evidence; the harness provisions demonstrations and checks the product. Each has its own entry point. See the [repository map](docs/architecture/repository.md) and [Scala principles](docs/architecture/scala.md).

## Run it

Follow [setup](book/setup.md) for the pinned tools. Build the product with `scripts/build-product`, then connect to configured local participants with `scripts/product serve configuration.json`. The [product guide](product/README.md) describes that configuration.

For the complete demonstration, run `scripts/build` followed by `scripts/demo live`. The [harness guide](harness/README.md) lists the focused checks and full suite. One ledger environment runs at a time.

Read the [nine-chapter book](book/README.md) or open recorded runs using the [playback guide](book/playback.md). The book presents concrete inputs, independent expectations, observed results, interactive progression, and source inspection. A [packaged delivery](docs/release/packaging.md) provides `run-product`, `run-book`, `run-live`, and `run-verify`.

## Review this draft

[Fourth-draft principles and plan](FOURTH-DRAFT.md) · [Measurements](docs/fourth-draft/measurements.md) · [Progress](docs/progress.md) · [Acceptance](docs/release/acceptance.md) · [Capabilities](docs/capabilities.md) · [Compatibility](docs/compatibility.md)

The earlier versions remain on `first-draft`, `second-draft`, and `third-draft`. Their plans and verification notes are in [history](docs/history/README.md). Imported proposals and attribution are recorded in [sources](docs/sources.md). Public repository destination and licensing remain undecided.
