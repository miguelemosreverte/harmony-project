# Second-draft architecture

**Target design, not yet implemented.** The active sequence is in [the second-draft plan](../../SECOND-DRAFT.md). This document owns the source and interaction boundaries; the [reader design](reader.md) owns presentation.

## A feature should explain a complete operation

For financing approval, the intended reading order is:

```text
Markdown scenario
  → ApproveFinancing command
  → financing operation, using an authenticated participant
  → Daml application choice
  → validated participant observation
  → independent comparison and reader presentation
```

The ledger remains authoritative. Eligibility shown in a view is a convenience derived from observations; it does not grant authority. The identity used for execution comes from the authenticated session and its bound ledger client. A browser-supplied actor name cannot choose or elevate that identity.

An illustrative API shape is:

```scala
final case class ApproveFinancing(
    requestId: RequestId,
    application: ApplicationId,
    observedVersion: SnapshotVersion
)

final class Financing(ledger: FinancingLedger):
  def approve(command: ApproveFinancing): IO[ApprovalResult]
```

This is a design sketch. The migration defines and verifies the actual types. `FinancingLedger` is a narrow application-facing boundary whose implementation owns SDK translation and uses an already authenticated participant. Its existence is justified by that boundary; every small helper does not need a trait.

## Source ownership

Keep the existing JVM/Scala.js toolchain and its small number of build targets initially. Consistent Scala package names define feature ownership across the platform directories. Platform-specific files stay in their actual target; do not create empty target folders for symmetry.

```text
on-ledger/
  interfaces/                    Common action/result/transfer contracts
  core/                          Workflow and continuation rules
  applications/                  Independently owned reference applications
  bindings/                      Typed unchanged-application adapters
  demo/                          Live reference assembly and setup
  tests/                         Scripted integration/limit checks

off-ledger/
  shared/src/main/scala/harmonia/
    protocol/                    Small shared delivery/identity value types
    financing/                   Financing commands and observed view state
    composition/                 Composition commands and observed view state
    packages/                    Package-operation messages and results
    stories/                     Recording envelope and pure comparison
  jvm/src/main/scala/harmonia/
    app/                         CLI/HTTP routing, configuration, resource wiring
    ledger/                      Authenticated Canton transport and decoding
    submission/                  Request lifecycle and reconciliation
    financing/                   Financing operation and ledger mapping
    composition/                 Proposal, consent, execution, projection
    packages/                    Acquire, inspect, compile/export coordination
    bindings/                    Explicit mapping validation and source generation
    stories/                     Read, execute, compare, retain, project
    book/                        Export chapters, assets, and evidence
    release/                     Assemble and verify the local delivery
    files/, processes/           Narrow filesystem and subprocess boundaries
  browser/src/main/scala/harmonia/
    app/                         Reader/workspace startup and screen composition
    book/                        Chapter, experiment, and evidence presentation
    financing/                   Financing view and interactions
    composition/                 Draft editor, consent, and execution views
    packages/                    Package input and generated-project views
    ui/                          Shared visual primitives and component lifecycle

examples/                        One discoverable collection of scenarios/goldens
book/                            Authored chapters and presentation assets
docs/                            Architecture, setup, and review guidance
scripts/                         Small build and launch wrappers
```

Normal `src/test/scala` packages mirror the feature they test. Add subfolders when a feature develops separate cohesive responsibilities. A file called `ApproveFinancing.scala` can contain the small related definitions needed to read that operation; there is no requirement for one definition per file.

The shared `protocol` package contains only vocabulary that is actually shared across features or transport. Financing/composition/package-specific data stays in its own feature. Raw evidence and generic structural comparison can remain JSON where their open document structure is useful.

## Dependency direction

| Owner | May use | Must remain independent of |
| --- | --- | --- |
| Feature data and pure operations | Standard values and genuinely shared domain vocabulary | HTTP, protobuf, DOM, filesystem, application startup |
| Ledger transport | SDK, authentication, bounded streams, resource primitives | Financing/composer action selection and reader layout |
| Submission lifecycle | Request identity, status, a supplied execution/reconciliation boundary | Specific business choices, story IDs, chapter names |
| Feature operation | Its model, its ledger boundary, explicit shared results | Browser rendering and release/check harnesses |
| Scenario runner/projector | The scenario model, actual observations, its feature operations where applicable | Using an expectation to choose or fabricate an outcome |
| Reader | Validated recording/presentation types and shared UI primitives | Test script names, SDK transport, business-rule execution |
| Application wiring | Feature entry points and concrete infrastructure | Hidden global resource acquisition |

Internal commands and states use exhaustive types. Compiler package inspection uses structured LF metadata, not regular expressions over its human-readable pretty-print output. Names at external boundaries are decoded once; arbitrary user text and documentation markup remain legitimate text formats.

Use explicit imports and a few meaningful build boundaries. A focused dependency check may catch forbidden imports; it cannot establish semantic separation on its own. Application routing may dispatch over the finite supported command types. A plugin registry or general dependency-injection framework is not required by the current capabilities.

Daml source applications retain independent package identities. Core imports the common interface vocabulary. A generated binding imports its pinned source and supported Harmonia API. Live demo assembly and scripted checks consume those packages; production-facing Scala operations do not depend on a test harness to define their behavior.

## One delivery lifecycle, explicit feature commands

The delivery owner handles request identity, reuse protection, bounded scheduling, stale versions, and reconciliation after uncertain submission. A feature handles selection and mapping of its supported command. Resource wiring makes it clear which participant and connection the operation uses.

Preserve these distinctions in types and visible states:

| Situation | Meaning |
| --- | --- |
| Invalid input | No valid command was formed |
| Failed observation before submission | The required state could not be established; no business rejection was observed |
| Missing required visible contract | This operation is unavailable to the session at that observation |
| Stale version | State changed relative to the submitted view |
| Definite ledger rejection | A submission response established rejection |
| Unconfirmed submission | The command may have committed; reconcile or retry its established identity |
| Confirmed commit | An actual ledger result or observed matching transaction establishes completion |

Cancellation of a wait releases its client resources; it does not establish transaction rollback. Raw transport errors belong at the transport boundary. HTTP status and user-facing text are translations of explicit outcomes, not substitutes for their semantics.

## Recording and presentation

All book-specific models, projectors, export, navigation, and rendering belong under `harmonia.book` in their applicable shared/JVM/browser target. Generic UI primitives live under `harmonia.ui`; the live workspace must not import them from the book.

A recording has an explicit envelope: identity, validated scenario, expected result, actual result, differences, provenance, and references to raw evidence. Its presentation is derived by a named scenario projector into sections such as a sequence of attempts, participant observations, a state comparison, or measurements.

A projector may understand an individual feature or scenario family. The generic envelope and reader do not test for `Boundaries:run`, inspect arbitrary field presence to select behavior, or silently replace malformed data with an empty passing display. Required data fails decoding with a useful path. Legitimately absent data is represented explicitly.

Use one inventory for required example discovery, coverage, chapter references, and release packaging. It records identity and location, not copies of actors, actions, expectations, or observed values. Use the existing meaningful scenario discriminators when decoding; do not add repeated infrastructure tags to every YAML block solely to satisfy the implementation.

## Migration map

| First-draft pressure point | Target owner | Removal gate |
| --- | --- | --- |
| `live/actions/LiveActions` mixes delivery and business selection | `submission`, feature operations, application wiring | Financing/composition commands and uncertainty checks use the new path |
| `live/state/LiveSnapshot` combines generic observation and financing presentation | Ledger observation, financing projection, typed view state | All live consumers use validated feature states |
| `RecordedStory` recognizes an individual boundary script | Scenario projector plus generic presentation sections | All baseline recordings render through the new envelope |
| Views repeatedly interpret JSON and rebuild the application root | Typed view state and owned components | Navigation, refresh, edit, and focus walkthroughs pass |
| `smoke` owns broad integration tests and live setup | Clearly named test/demo assemblies | Live startup and required scenarios use the new explicit assemblies |
| `stories` and `evaluations` have separate discovery paths | One example inventory and collection | Every baseline path/content maps to a required new example |
| Literal recording/chapter totals embedded in release code | A checked inventory and release manifest | Missing, extra, duplicate, or unreferenced required entries fail verification |

Keep compatibility adapters small and temporary. Avoid a repository-wide file move before the reference slice establishes the naming and reading pattern. Preserve the old and new path correspondence in review notes and retire each adapter in its owning step.

## Reading review

For financing, composition, and package generation, provide a short trace naming the public operation, its data, its external boundary, its independent test, and its chapter. Record the explanations a reviewer would need because a name, responsibility, or interaction remains implicit. Improve those sources or document the unavoidable boundary.

The criterion is understandable ownership and behavior. Neither an arbitrary maximum file size nor a required minimum number of abstractions is a substitute for that review.
