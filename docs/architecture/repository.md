# Repository map

Start with [a chapter](../../book/README.md) or [an example](../../examples/README.md), then follow its named capability. The [third-draft reading guide](../third-draft/reading-guide.md) links typed operations, boundaries, and independent tests directly.

```text
examples/      Human-readable inputs and independent expectations
book/          Nine authored chapters, diagrams, setup, and site styling
on-ledger/     Interfaces, core, applications, bindings, composition, demo, tests
off-ledger/   Scala application, browser, shared models, and focused tests
docs/          Architecture, walkthroughs, evidence, and release records
product/packages/      Pinned external/local DAR identities and reviewed mappings
scripts/       Small bounded launch, build, check, and packaging commands
```

`off-ledger` has two build targets, JVM and Scala.js, with shared pure types. Inside them, Scala packages use the same capability vocabulary:

| Package | Owns | Entry point |
| --- | --- | --- |
| `financing` | Approval, continuation, private observations, and their view | `Financing`, `FinancingObservation`, `FinancingState`, `FinancingPanel` |
| `composition` | Validated plans, consent/execution choices, observations, and the draft editor | `Composition`, `ComposerCommands`, `ComposerView` |
| `packages` | Acquisition, structured LF inspection, package workspace and export | `ResolvePackages`, `LfArchive`, `PackageBuilder` |
| `bindings` | Reviewed mapping, supported type checks, generated code, independent proof | `TemplateShapeReader`, `GenerateSources`, `GenerateBinding` |
| `submission` | Duplicate requests, stale observations, scheduling, and reconciliation | `Submissions` |
| `ledger` | Authenticated transport, values, queries, networks, and Script execution | `ParticipantLedger`, `LedgerSnapshot`, `CantonNetwork` |
| `stories` | Scenario parsing, execution, normalized observations, comparison, provenance | `CheckStories`, feature runners, `CompareResults` |
| `examples` | Required example and chapter membership | `Examples` |
| `book` | Recording presentation, chapter export, reader navigation and inspection | `PresentStory`, `ExportBook`, `BookApp` |
| `app` | CLI, HTTP, authenticated live resource wiring, feature dispatch | `Main`, `LiveRuntime`, `Workspace` |
| `live` | Browser session and HTTP client; dedicated live verification | `LiveApp`, `LiveView`, `CheckLive` |
| `ui` | Small generic DOM primitives shared by book and workspace | `Elements` |
| `release`, `files`, `processes` | Delivery assembly and narrow operating-system boundaries | `CheckRelease`, `ArtifactFiles`, `ManagedProcess` |

All book-specific models, projectors, export and browser code live under `harmonia.book`. Shared visual primitives belong to `harmonia.ui`. The server constructs the same `WorkspaceSnapshot`, `CompositionState`, `PackageState`, and recording models that consumers decode. HTTP/file encoding happens at the boundary. The browser renders observations and submits supported commands; the ledger owns business authority.

The [ledger map](../../product/ledger/README.md) explains package ownership. The live demo assembly has no dependency on the test assembly. Core depends on common interfaces; applications retain independent compiled identities. Existing business rules and input/expectation contents survive the second draft.

Generated projects, networks, local recordings, and release bundles live under ignored `.artifacts/`. Ordinary compiler output uses ignored `target/` and `.daml/` directories beside its source. A release copies selected evidence and verifies its clean source revision and file hashes. It does not package live credentials or network authorization files.
