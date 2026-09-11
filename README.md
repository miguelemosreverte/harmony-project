# Harmonia

Compose independently owned Canton/Daml applications into a shared workflow. The ledger authorizes actions and records progression; the Scala service observes state and submits commands; the browser makes the workflow visible.

## Version 0.2 in progress

The active work revisits the original product requirements from the fourth-draft
baseline. Read the [linear plan](docs/0.2/PLAN.md), [product contract](docs/0.2/product-contract.md),
and [numbered Git history](docs/versions.md). The field guide uses shared HTML infographics for workflow playback, code exploration,
diagram review, and original-document comparison. The Scala workspace uses the same
components for live financing, composition, and package stages.
See the [approved visual contract and verification](docs/0.2/REFERENCE-FIDELITY.md),
[reference comparison](design/0.2/infographic/reference-review/compare.html), and
[reader principles](docs/0.2/READER-EXPERIENCE.md), and
[page-by-page walkthrough with screenshots](docs/0.2/WALKTHROUGH.md).

After the [runtime setup](book/setup.md) and initial `scripts/build`, run
`scripts/start-sandbox`. Open the private `open.html` launcher printed by the command:
Bank approves, Buyer continues, Reviewer observes. The command prints separate product and book addresses. The product has no book routes.
One disposable Canton environment runs at a time; Ctrl-C closes it.

For recorded exploration without a ledger, run `scripts/design-preview` and open
[the field guide](http://127.0.0.1:56202/design/0.2/book-overview.html).
[Code browser](design/0.2/code.html) · [Diagram review](design/0.2/reviewer.html) ·
[Originals beside implementation](design/0.2/author.html) · [Workflows](design/0.2/workflows.html).
The original [quotation coverage](design/0.2/coverage.html) remains independently checked.
The full property offer and custody transfer remain recorded examples.

## Read the product

Start in **[product/](product/README.md)**. It contains the contracts, service, shared API types, HTML scene renderer, live browser, and reviewed package mappings. The [reading guide](docs/fourth-draft/reading-guide.md) follows one operation through those owners.

```text
product/     Contracts, service, API, scene renderer, live browser, package inputs
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

Read the [book and detailed laboratory](book/README.md) or open recorded runs using the [playback guide](book/playback.md). The book presents concrete inputs, independent expectations, observed results, interactive progression, and source inspection. A [packaged delivery](docs/release/packaging.md) provides `run-product`, `run-book`, `run-live`, and `run-verify`.

## Review the fourth-draft baseline

[Fourth-draft principles and plan](FOURTH-DRAFT.md) · [Measurements](docs/fourth-draft/measurements.md) · [Progress](docs/progress.md) · [Acceptance](docs/release/acceptance.md) · [Capabilities](docs/capabilities.md) · [Compatibility](docs/compatibility.md)

The earlier versions remain on the numbered archive branches listed in [version history](docs/versions.md). Their plans and verification notes are in [history](docs/history/README.md). Imported proposals and attribution are recorded in [sources](docs/sources.md). Development history is backed up in the private [GitHub repository](https://github.com/miguelemosreverte/harmony-project). Public release and licensing remain undecided.
