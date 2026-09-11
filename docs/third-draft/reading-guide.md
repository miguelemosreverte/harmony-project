# Read the third draft through its return types

The [third-draft design](../../THIRD-DRAFT.md) identifies the changes from the verified second draft. The folder structure remains organized by compilation target and capability. This pass makes the producer, operation, and consumer agree on named values throughout each path.

## Financing: observe, choose, submit

Start with the [private handoff input](../../examples/evaluations/live-handoff/input.md) and its [independent expectation](../../examples/evaluations/live-handoff/expected.md).

1. [FinancingState](../../product/api/src/main/scala/harmonia/financing/FinancingState.scala) defines the finite actions and observed states, with one codec for the established transport fields.
2. [FinancingObservation.read](../../product/server/src/main/scala/harmonia/financing/FinancingObservation.scala) decodes the participant's visible application and shared progress once. `ObservedApplication` contains its typed status and private details; `ObservedProgress` contains its typed status. An absent contract is optional. A malformed visible contract returns `LedgerDecodingFailure`.
3. [Financing.select](../../product/server/src/main/scala/harmonia/financing/Financing.scala) matches the action and returns an optional [LedgerExercise](../../product/server/src/main/scala/harmonia/ledger/client/LedgerExercise.scala): contract, choice, and argument have names. The SDK choice mapping remains here.
4. [Workspace](../../product/server/src/main/scala/harmonia/app/workspace/Workspace.scala) supplies the authenticated participant. [Submissions](../../product/server/src/main/scala/harmonia/submission/Submissions.scala) checks the observed version before running the effect; [SubmitChoice](../../product/server/src/main/scala/harmonia/ledger/client/SubmitChoice.scala) classifies actual submission responses.
5. `Workspace.state` returns [WorkspaceSnapshot](../../product/api/src/main/scala/harmonia/workspace/WorkspaceSnapshot.scala). The [HTTP server](../../product/server/src/main/scala/harmonia/app/http/LiveServer.scala) encodes it; [FinancingPanel](../../product/web/src/main/scala/harmonia/financing/FinancingPanel.scala) consumes its typed financing state.

Previously, the observation repeatedly searched contracts, converted status through JSON, and could turn missing text into an empty string. The operation returned a positional tuple. The workspace merged several independently assembled JSON objects. Those paths are replaced.

The authoritative transitions remain [PrivateFinancing.Approve](../../product/ledger/applications/private-financing/daml/PrivateFinancing.daml) and [SharedProgress.Continue](../../product/ledger/core/daml/Harmonia/SharedProgress.daml). The new [observation tests](../../harness/runner/src/test/scala/harmonia/financing/FinancingObservationSuite.scala) distinguish missing disclosure from malformed payloads. [CheckLive](../../harness/runner/src/main/scala/harmonia/live/verify/CheckLive.scala) proves actual authority and recovery.

## Composition: one command family, one observation model

Read [the generated composition input](../../examples/evaluations/composer-generated/input.md), then [CompositionCommand](../../product/api/src/main/scala/harmonia/composition/CompositionCommand.scala). Proposal, acceptance, cancellation, and advance belong to that family. [WorkspaceCommand](../../product/api/src/main/scala/harmonia/workspace/WorkspaceCommand.scala) routes financing and composition exhaustively. The composition executor cannot receive a financing command.

[ComposerCommands](../../product/server/src/main/scala/harmonia/composition/ledger/ComposerCommands.scala) maps those commands to the actual [Composer](../../product/ledger/composition/daml/Composer.daml) and core choices. Its result is a named `LedgerExercise`.

For observation, [CompositionContracts](../../product/server/src/main/scala/harmonia/composition/ledger/CompositionContracts.scala) contains the ledger payload schemas together: workspace references, draft steps, process definition, roles, bindings, and adapter source. [ComposerSnapshot.read](../../product/server/src/main/scala/harmonia/composition/ledger/ComposerSnapshot.scala) uses those values to produce [CompositionState](../../product/api/src/main/scala/harmonia/composition/CompositionState.scala). Required missing roles, bindings, lists, and source status fail explicitly; an undisclosed source remains optional.

The [editor](../../product/web/src/main/scala/harmonia/composition/CompositionEditor.scala) still owns unfinished input, while the [view](../../product/web/src/main/scala/harmonia/composition/ComposerView.scala) consumes the shared model. New observation/transport checks complement the unchanged [direct and generated ledger checks](../../harness/runner/src/main/scala/harmonia/composition/verify/CheckComposer.scala).

## Packages: stored facts determine available operations

Read [the pinned input manifest](../../product/packages/inputs.md) and a [reviewed mapping](../../product/packages/mappings/financing.md), then [BuilderInput](../../product/server/src/main/scala/harmonia/packages/workspace/BuilderInput.scala).

That file keeps related state together: inspected identity and included metadata, a matched pinned source, the finite `SupportedBinding`, and an optional `CompiledProject` containing the archive and retained manifest. The public flags are derived from those facts. There is no stored JSON record that the generator must parse to decide what to do.

[PackageBuilder](../../product/server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala) acquires the input, checks its identity, calls [GenerateBinding](../../product/server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala), and owns the bounded operation. Its state and mutation methods return [PackageState](../../product/api/src/main/scala/harmonia/packages/PackageState.scala). HTTP encoding preserves included metadata and publishes a generation manifest only after compilation.

Structured [LF inspection](../../product/server/src/main/scala/harmonia/packages/inspect/LfArchive.scala), reviewed mapping validation, deterministic generation, and independent execution remain the authority for whether a supported adapter works. A displayed flag does not grant installation or ledger authority.

## Book: typed interpretation, complete raw evidence

[ExportBook](../../book/export/src/main/scala/harmonia/book/ExportBook.scala) verifies source artifact hashes, reads the scenario and independent results, and constructs [RecordedStory](../../book/model/src/main/scala/harmonia/book/RecordedStory.scala) directly.

[PresentStory](../../book/export/src/main/scala/harmonia/book/project/PresentStory.scala) returns `StoryPresentation`; its units are `StoryUnit` values. [BoundaryPhases](../../book/export/src/main/scala/harmonia/book/project/BoundaryPhases.scala) uses the same envelope for explicitly labeled verification phases. [BookChapter](../../book/model/src/main/scala/harmonia/book/BookChapter.scala) represents authored chapters. Encoding happens when writing the evidence file.

Raw input, expectation, actual result, and provenance remain intact. A missing actual action stays `Json.Null` in that unit and appears as missing in the reader. [CompareResults](../../harness/model/src/main/scala/harmonia/stories/compare/CompareResults.scala) still compares the complete independent results. A successful projection never establishes a successful test.

## Deliberate boundaries that remain

- SDK/protobuf values and authored field names enter through ledger, package, and scenario decoders. Their required shapes must be checked at that boundary.
- Daml choice names and argument construction remain in the small operation adapters. The compiler checks Scala relationships; real ledger tests check the mapping to Daml authority.
- Raw JSON remains useful for retained package metadata, generation manifests, transactions, and golden evidence. Internal workspace, package, and book state use named models.
- JVM and browser are separate build targets. Feature packages communicate ownership within those targets; they are not all independent build modules.
- The example inventory contains membership and chapter associations. Markdown owns scenario facts and independent expectations.

The gain is fewer relationships a reader must infer. The number of files alone is not a quality measure, and this guide is material for human review rather than a claim that such a review has already happened.
