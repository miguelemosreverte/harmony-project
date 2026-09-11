# Supported runtime and integration combinations

This matrix describes demonstrated combinations. All examples use synthetic data and local Canton. The runtime and compiler are pinned to Daml SDK 3.4.11, with Java 17. Off-ledger code builds with Scala 3.3.6 and Cats Effect 3.6.3; the browser uses the pinned Scala.js toolchain in `off-ledger/build.sbt`.

| Source | Daml-LF | Integration | Evidence |
| --- | --- | --- | --- |
| Owned financing 0.1.0 | 2.2 | Direct `StepAction` implementation | `workflow-approved` and `workflow-rejected` |
| Frozen legacy financing 0.1.0 | 2.2 | Hand-written and generated adapters | `adapter-*`, `generated-*`, and the generated financing example |
| Primitive approval fixture 0.1.0 | 2.2 | Generated adapter with Party/Text/Int/Bool/Decimal arguments | Generated primitive example and negative mapping checks |
| Pinned Splice token metadata 1.0.0 | 2.1 | Imported types in the package example | Two fresh participant networks and byte-identical administrator DAR retrieval |
| Property financing and property offer 0.1.0 | 2.2 | Independent direct-interface applications with scoped result handoff | Eight `purchase-*` stories across four participants |
| Source custody, destination custody, and transfer 0.1.0 | 2.2 | Typed reference coordinator exposed through `StepAction` | Six `transfer-*` stories across four participants |

[The input manifest](../packages/inputs.md) is the authoritative home for pinned source digests and package IDs. Generated projects record their own source, interfaces, core, library, and example hashes. Package resolution rejects changed identities or unsupported LF versions before generation; it never selects a newer source implicitly.

The source-only legacy package has no Harmonia dependency and remains byte-identical. The primitive fixture is separate from that frozen source. Generated LF 2.1 action integration is not demonstrated by the LF 2.1 metadata import; an external application must validate its own supported mapping and runtime conditions.

Generated adapters accept the bounded shape in [Decision 008](architecture/008-binding-generation.md). Nonconsuming choices, other result shapes, nested records, collections, optional values, and unsupported field declarations fail with diagnostics. Both current generated projects must also match their reviewed book specimens. The regular suite rebuilds one project in a fresh output directory and compares complete generation manifests, including compiled DAR hashes.

One local JVM hosts the multi-participant examples with distinct participant identities and stores on one shared synchronizer. This proves the demonstrated ledger behavior, not a production deployment's operational isolation or scale. Recorded perspective switching is a view of saved evidence; authenticated live sessions are a separate delivery step.
