# Harmonia

Compose independently owned Canton/Daml applications into a shared workflow, and understand the result through executable stories and an interactive book.

The ledger enforces actions and records progression. Scala tools execute stories, compare observations with committed golden files, generate application bindings, and present the results.

## Start here

- [Product requirements and ordered commit plan](PRD.md)
- [Implementation progress and verification](docs/progress.md)
- [Repository map](docs/architecture/repository.md)
- [Scala organization and functional style](docs/architecture/scala.md)
- [Source documents and provenance](docs/sources.md)
- [The working book](book/README.md)

The first three commits establish the plan, a working runtime, and a real ledger story verified against a Markdown golden. Start with [local setup](book/setup.md), then run:

```sh
scripts/build
scripts/harmonia smoke
scripts/harmonia check
```

To explore the printed run directory in the interactive book, use `scripts/book .artifacts/check-RUN`. See the [playback guide](book/03-recorded-playback.md) for exporting, opening, and inspecting recorded evidence.

## Delivery principles

Each capability ships with readable code, a concrete story, committed expectations, and an explanation. The same execution artifacts power regression checks and interactive demonstrations.

Off-ledger application code is Scala with Cats Effect `IO`. On-ledger contracts are Daml. Code is organized into cohesive feature slices. Markdown and YAML remain human-readable, with explicit meanings and minimal duplication.

## Publication

Development currently takes place in this local repository. Public repository destination and licensing must be settled before publication. Imported source documents retain their original attribution; see the provenance record.
