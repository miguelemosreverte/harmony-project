# Repository map

This map describes the intended repository as capabilities are implemented. Create directories when they contain a working feature; the [progress record](../progress.md) shows what exists and has been verified.

```text
harmony-project/
  README.md                    Reader's starting point
  PRD.md                       Requirements and ordered commit plan
  harmonia.md                  Imported development proposal
  harmonia-architecture.html   Imported design illustration
  docs/
    architecture/             Architecture decisions and code organization
    progress.md               Delivered commits and verification evidence
    sources.md                Provenance of imported documents
  book/                       Chapters and presentation assets
  stories/                    Committed input.md and expected.md examples
  on-ledger/                  Daml contracts, interfaces, bindings, and references
  off-ledger/
    shared/                   Models needed by JVM and browser code
    jvm/                      Scala tools, server, and Canton integration
    browser/                  Scala.js interactive application
  scripts/                    Small launch/build wrappers; application logic is Scala
```

`off-ledger/jvm/src/main/scala/harmonia/` contains the following capability packages:

| Package | Owns | Example |
| --- | --- | --- |
| `stories/` | Reading, executing, and comparing executable stories | Run an input and compare observed results with its golden |
| `bindings/` | Application package inspection and generation of typed Daml integration | Generate an adapter for an eligible financing choice |
| `workflows/` | Client operations to inspect and interact with a workflow | Request an approval under the submitting party's identity |
| `ledger/` | Canton-specific connections, submissions, queries, and observations | Obtain a command's committed or rejected outcome |
| `files/` | Filesystem access and artifact persistence | Read Markdown or write actual results and a diff |
| `app/` | Configuration, resource ownership, and entry-point wiring | Construct programs and run the CLI/server |

`ledger/` and `files/` are distinct packages. Domain parsing belongs to its feature; the filesystem package supplies file access. Domain action selection belongs to its feature; the ledger package supplies the Canton integration.

The Scala binding generator emits Daml contracts. Scala workflow operations submit requests; Daml enforces their validity. This distinction also applies to the book: a diagram displays observed behavior without implementing a second execution engine.

Tests mirror the feature packages under `src/test/scala`. The authored stories stay in the root `stories/` collection. Generated local runs and diagnostics go to ignored `.artifacts/` directories, while selected release evidence is packaged deliberately with its provenance.

See [Scala organization](scala.md) for the source tree and dependency rules, and [the PRD](../../PRD.md) for the exact delivery order.
