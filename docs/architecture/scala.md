# Scala organization and style

The code uses concrete Cats Effect `IO`, immutable values and enums, ordinary functions, and explicit `Resource` ownership. Platform directories reflect JVM and browser compilation; capability packages explain the product. See the [repository map](repository.md) and [third-draft reading guide](../third-draft/reading-guide.md).

A normal reading path is a shared command/model, a JVM operation and observation boundary, and its browser view. Related small definitions stay together. There is no universal service interface or one class per file rule.

The financing operation is an exhaustive match over `FinancingAction`. Composition plans use `CompositionActor` and `CompositionAction`, while `CompositionCommand` owns proposal, acceptance, cancellation, and execution. `WorkspaceCommand` routes these feature families exhaustively. Operations return a named `LedgerExercise`. Financing observations and composition payload schemas are decoded once, with explicit failures for malformed visible data.

`WorkspaceSnapshot`, `CompositionState`, and `PackageState` are constructed by the server and decoded by the browser using shared codecs. HTTP serialization preserves the established field names. `BuilderInput` stores typed inspection, source, and compiled-project facts; generation permissions and output flags are derived from them. Raw metadata and generation manifests remain retained evidence, not control state.

`Submissions` owns request delivery. Its preparation function observes current state and returns an optional submission effect. The owner checks the observed version before running that effect. `Workspace` supplies authenticated participants and routes commands to features. `SubmitChoice` interprets actual gRPC submission failures; failed observations never become definite business rejection. Reconciliation uses command identities recovered from ledger history.

`LfArchive` reads bounded DAR archives and the SDK's public LF protobuf classes. `TemplateShapeReader` checks supported record and choice structures. Text matching remains appropriate for external identifiers, Markdown directives, HTTP routes, and authored names; it does not replace type checking of internal commands or compiler syntax trees.

The book's `PresentStory` and `BoundaryPhases` return typed reader units; `ExportBook` constructs `RecordedStory` and `BookChapter` directly before writing JSON. Independent expected/actual comparison remains in `CompareResults`. Missing observations stay missing, and verification phases do not acquire fictional business outcomes. `Examples` supplies discovery, chapter coverage, and release membership.

The browser owns one `BookView` and one inspector for a reader lifetime. `BookNavigation` stores chapter/example/attempt context in the address. `LiveView` owns stable regions; its editor and package controls remain mounted during participant refreshes. Polling/listeners, cancellable fetches, server executors, ledger channels, and subprocesses have scoped owners.

Build and test one heavy stage at a time. The existing heap limits remain: sbt 1 GiB, application tools 512 MiB, Canton 2 GiB, Script 512 MiB, recorded book 128 MiB. A workspace lease prevents overlapping ledger environments. `scripts/harmonia` exports an immutable runtime JAR so sbt exits before a ledger starts.

Tests mirror the capability they verify. Goldens are authored Markdown beside their inputs; no test updates its expectation automatically. Boundary and failure tests check meaningful malformed inputs, authority, missing observations, uncertain outcomes, and cancellation.
