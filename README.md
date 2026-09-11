# Harmonia

Compose independently owned Canton/Daml applications into a shared workflow. The ledger authorizes actions and records progression; the Scala service observes state and submits commands; the browser makes the workflow visible.

## Version 0.2 design

The active work revisits the original product requirements from the fourth-draft
baseline. Read the [linear plan](docs/0.2/PLAN.md), [product contract](docs/0.2/product-contract.md),
and [numbered Git history](docs/versions.md). The new book and application designs
are available in the [design review](design/0.2/README.md); the book plays preserved ledger recordings and the workspace is a labelled simulation.

Run `scripts/design-preview` and open [the new field guide](http://127.0.0.1:56202/design/0.2/book-overview.html).
Inspect the [current design review](http://127.0.0.1:56202/design/0.2/review.html) and
[quotation coverage](http://127.0.0.1:56202/design/0.2/coverage.html).

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

## Review the fourth-draft baseline

[Fourth-draft principles and plan](FOURTH-DRAFT.md) · [Measurements](docs/fourth-draft/measurements.md) · [Progress](docs/progress.md) · [Acceptance](docs/release/acceptance.md) · [Capabilities](docs/capabilities.md) · [Compatibility](docs/compatibility.md)

The earlier versions remain on the numbered archive branches listed in [version history](docs/versions.md). Their plans and verification notes are in [history](docs/history/README.md). Imported proposals and attribution are recorded in [sources](docs/sources.md). Public repository destination and licensing remain undecided.
