# Scala principles

Use concrete Cats Effect `IO`, immutable values, ordinary named functions, and explicit `Resource` ownership. Keep a related operation together. A smaller file obtained by scattering its logic does not improve the reading experience.

The [product reading guide](../fourth-draft/reading-guide.md) follows the features. The [repository map](repository.md) explains the compiler boundaries between product, book, and harness.

## Values inside, decoding at boundaries

`FinancingAction`, `CompositionAction`, `CompositionCommand`, and `WorkspaceCommand` make supported operations explicit and exhaustively matched. A `LedgerExercise` names the choice submitted. Financing and composition observations decode visible contracts once; malformed visible data fails with context, and missing disclosure remains optional.

The server constructs the public `WorkspaceSnapshot`, `CompositionState`, and `PackageState`; the browser decodes those same types. A small `JsonCodec` derives the established snake-case field names. Unusual envelopes remain explicit. The composition editor builds a typed `Composition` and calls the same validation as the HTTP decoder. It does not construct internal JSON merely to validate its own values.

`BuilderInput` carries typed inspection, source, and compilation facts. Generation permissions follow from those facts. Raw metadata and generation manifests remain evidence. `LfArchive` reads bounded archives through SDK LF protobuf classes, and `TemplateShapeReader` checks their structures. Text matching belongs at authored-name, route, Markdown, and identifier boundaries.

## Effects have owners

`Connections` supplies existing participant clients and their catalog. `Workspace` routes commands and coordinates submissions. `Submissions` checks the observed version before running a prepared effect, reconciles uncertain outcomes against ledger command identities, and owns its background work. Failed observations do not become definite business rejection.

The standalone service reads operator-supplied local connection configuration. The harness owns disposable networks, sample parties, and demo JWT signing. HTTP executors, ledger channels, polling, fetches, and subprocesses close with their owning `Resource`.

The live view keeps its editor and package controls mounted during refresh. The book owns its own browser, navigation, recording projection, and source inspector. `CompareResults` supplies the independent structural comparison used by checks and recorded playback.

## Verification and memory

Golden expectations remain authored Markdown beside their inputs. No checker rewrites them automatically. Tests cover behavior and boundaries: malformed data, authority, missing observations, transport fields, uncertain submissions, cancellation, and compiler isolation.

Build and check one heavy stage at a time. Heap caps are sbt 1 GiB, application tools 512 MiB, Canton 2 GiB, Script 512 MiB, and recorded playback 128 MiB. Heap caps are not resident-memory measurements. A workspace lease prevents overlapping ledgers. Launch scripts export immutable runtime JARs so sbt exits before ledger execution begins.
