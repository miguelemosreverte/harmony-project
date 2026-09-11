# Second-draft architecture

This is the implemented design. The [ordered plan](../SECOND-DRAFT.md) records verification status, the [repository map](../../architecture/repository.md) describes folders, and the [reading traces](reading-traces.md) connect examples to operations and tests.

## A feature explains a complete operation

The financing reading path is:

```text
Markdown scenario → FinancingAction → Financing.select
  → authenticated ledger choice → FinancingObservation
  → FinancingState → FinancingPanel
```

The browser can only submit a supported `WorkspaceCommand` from its provisioned session. The ledger owns business authority; displayed eligibility does not grant it. An actor name in a browser request cannot select a different authenticated participant.

`Financing.select` and `ComposerCommands.select` are ordinary functions mapping typed commands and observed contracts to choices. `Workspace` supplies the authenticated dependency. `Submissions` observes a prepared version, checks it against the request, and runs the supplied effect. Its lifecycle contains no feature selection.

## Ownership across targets

The JVM and Scala.js targets share immutable feature models, enums, transport decoders, the example inventory, and structural comparison. JVM features own SDK/file/process boundaries; browser features own interaction. The same capability names appear in both targets, without empty placeholder folders or one build module per small class.

All book-specific envelope types, projectors, export, navigation, inspection, and rendering live under `harmonia.book`. Generic DOM primitives live under `harmonia.ui`. Live views use the generic primitives directly. `LiveView` owns mounted regions, and the composition editor owns its unfinished draft.

On ledger, common interfaces and core are separate from independently compiled applications. `composition` owns consented process creation. `demo` provisions the live reference and does not import the test assembly. `tests` owns integration scripts. `fixtures` owns primitive-generation and metadata examples. Business package identities stay stable across folder moves; demo/test assembly names describe their responsibility.

## Dependency direction

| Owner | Uses | Remains independent of |
| --- | --- | --- |
| Shared feature data | Standard values, finite protocol types | HTTP, SDK, DOM, filesystem |
| Ledger transport | SDK, authenticated connections, bounded streams | Financing/composition action selection and reader layout |
| Submission lifecycle | Request data, status, supplied preparation/submission effect | Business choice selection and chapters |
| Feature operation | Its command/model and observed ledger boundary | Browser and release harness |
| Package workspace | Acquisition/inspection and binding generation | Browser rendering |
| Binding generation | Reviewed mapping, inspected types, bounded compilation | Package workspace and reader |
| Package inspection | Archive/schema validation and compiler metadata | Binding generation |
| Scenario runner/projector | Validated scenario and actual observations | Using expectations to determine an outcome |
| Reader | Validated presentation and raw comparison/evidence | Script names, SDK transport, authoritative execution |
| Application wiring | Concrete feature and infrastructure entry points | Hidden resource acquisition |

Text remains at real external boundaries: Markdown/YAML, HTTP names, ledger field names, and authored package mappings. Commands and live states are decoded into Scala types before internal routing/rendering. LF shape checks use the published protobuf structure, including interned references and type applications. No decision depends on a regular expression over compiler pretty-print output.

## One delivery lifecycle

| Situation | Meaning |
| --- | --- |
| Invalid input | No valid command was formed |
| Observation failure | Required state could not be established; no business rejection was observed |
| Missing visible prerequisite | The operation is unavailable to this observation/session |
| Stale version | Visible contracts changed relative to the submitted view |
| Definite rejection | A submission response established rejection |
| Unconfirmed submission | The command may have committed; reconcile its established identity |
| Confirmed commit | A ledger response or matching transaction proves completion |

`Resource` owns supervisors, channels, HTTP executors, inspector fetches, listeners, and subprocesses. Cancelling a wait releases resources and does not establish rollback. The workspace lease and bounded JVM launchers remain in place. Heavy verification stages run sequentially.

## Evidence and presentation

`Examples` is the explicit inventory for default story discovery, chapter links, and exact release membership. Scenario facts remain in readable Markdown; the inventory contains identity, location, family, and chapter ownership. The [move manifest](example-moves.json) preserves the first-draft input/expectation bytes.

`StoryFormat.result` selects a validated schema from an explicit `ResultKind`. `PresentStory` projects real observations into typed reader units; `BoundaryPhases` labels measurement phases without fabricating business action outcomes. `RecordedStory` retains the independent expectation, actual result, provenance, and projected presentation. Complete structural comparison still detects extra effects, ordering differences, and missing data.

The live SDK projections remain deliberate boundary code using ledger field names. The package workspace supports a finite reviewed mapping list. Source generation does not imply automatic registration or arbitrary runtime plugins. These are explicit limits of the demonstrated implementation.

## Removed first-draft indirections

- Mixed `LiveActions` becomes `Submissions`, feature choice mapping, and application wiring.
- Mixed `LiveSnapshot` becomes `LedgerSnapshot` and `FinancingObservation`.
- Scala `composer` becomes the consistently named `composition` feature.
- Root-level `builder` becomes package workspace, package view, and package verification.
- Pretty-printed LF matching becomes bounded protobuf inspection.
- Script-name/field-shape presentation inference becomes an explicit envelope and projectors.
- Implicit story discovery and repeated release totals become exact inventory membership.
- The reader's repeated view construction becomes one owned view, navigation address, and inspector.

The final [reading traces](reading-traces.md) identify remaining intentional cross-feature boundaries. Human review is still needed to judge whether those traces feel straightforward; compilation and successful tests establish behavior, not a human readability verdict.
