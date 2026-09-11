# Repository map

Three directories explain the implementation: **[product](../../product/README.md)** is the software, **[book](../../book/README.md)** teaches it, and **[harness](../../harness/README.md)** supplies demonstrations and evidence. Start with the [product reading guide](../fourth-draft/reading-guide.md).

| Root | Contents |
| --- | --- |
| `product/` | Ledger contracts, Scala service, shared public types, live browser, pinned inputs |
| `book/` | Chapters, diagrams, independent browser, recording models, exporter, styling |
| `harness/` | Golden execution, disposable networks, tests, fixtures, release assembly |
| `examples/` | Markdown/YAML inputs and independent committed expectations |
| `docs/` | Current architecture and delivery; prior plans and evidence under `history/` |
| `scripts/` | Small build, launch, and check commands |
| `build.sbt`, `project/` | Build definition and pinned plugins |

## Compilation boundaries

An arrow means “depends on.” Shared source directories are compiled only into their named consumers.

```text
service     ← product/server + product/api
web         ← product/web + product/api
runner      → service              (+ harness/model)
bookExport  → runner               (+ book/model)
reader      ← book/browser + book/model + harness/model
tools       → bookExport
```

`service` and `web` have no dependency on a book or harness project. `reader` has a different Scala.js entry point and output from `web`. The book exporter can interpret harness recordings without placing those interpreters in the product.

JVM launchers select explicit classpaths. `run-product` contains the service JAR and its libraries. `run-book` uses the book exporter classpath; `run-live` and `run-verify` use the tools classpath. The shared bundle `lib/` directory stores each distinct JAR once, while `classpaths/` records what each launcher can load.

## Inside the product

The server, API, and browser repeat meaningful feature names: `financing`, `composition`, and `packages`. Each operation stays with its observations and view. `app` connects those operations to HTTP; `ledger` owns authenticated transport and typed SDK decoding; `submission` owns request coordination. Bindings inspect structured LF metadata and generate the supported adapter. Small `files`, `processes`, and `ui` helpers serve their concrete boundaries.

The [ledger map](../../product/ledger/README.md) describes common interfaces, workflow execution, owned applications, and integration. Demo and test assemblies live under `harness/ledger` and depend on the product contracts.

Generated projects, local networks, recordings, and release bundles live under ignored `.artifacts/`. Compiler outputs use ignored `target/` and `.daml/` beside their source. A release copies selected evidence and verifies source identity and payload hashes. Private participant credentials stay in the execution directory.
