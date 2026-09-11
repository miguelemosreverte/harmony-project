# Read the second draft through three operations

These traces identify the code that now owns each responsibility. Start at the example and move right. The ledger choice remains authoritative; browser eligibility only reflects the current participant observation.

## Private approval and shared continuation

1. Read [the live input](../../examples/evaluations/live-handoff/input.md) and its [independent expectation](../../examples/evaluations/live-handoff/expected.md).
2. [FinancingAction and FinancingState](../../off-ledger/shared/src/main/scala/harmonia/financing/FinancingState.scala) name supported operations and visible outcomes. [Financing](../../off-ledger/jvm/src/main/scala/harmonia/financing/Financing.scala) maps each operation to its actual choice.
3. [PrivateFinancing](../../on-ledger/applications/private-financing/daml/PrivateFinancing.daml) owns approval. [SharedProgress](../../on-ledger/core/daml/Harmonia/SharedProgress.daml) owns continuation using its bound result.
4. [FinancingObservation](../../off-ledger/jvm/src/main/scala/harmonia/financing/FinancingObservation.scala) projects participant-visible contracts. [FinancingPanel](../../off-ledger/browser/src/main/scala/harmonia/financing/FinancingPanel.scala) receives the typed state.
5. [CheckLive](../../off-ledger/jvm/src/main/scala/harmonia/live/verify/CheckLive.scala) verifies actual HTTP and ledger authority, observation privacy, stale requests, repeats, and recovery. [Chapter 3](../../book/03-participant-views.md) explains the same disclosure boundary.

Previously, business choice selection and financing projection lived inside the mixed live coordinator. Now [Workspace](../../off-ledger/jvm/src/main/scala/harmonia/app/workspace/Workspace.scala) only composes authenticated dependencies and routes commands. [Submissions](../../off-ledger/jvm/src/main/scala/harmonia/submission/Submissions.scala) owns the delivery lifecycle; [SubmitChoice](../../off-ledger/jvm/src/main/scala/harmonia/ledger/client/SubmitChoice.scala) classifies submission responses. This cross-feature routing is intentional and localized.

## Propose, consent, execute

1. Read [direct composition](../../examples/evaluations/composer-direct/input.md) or [generated composition](../../examples/evaluations/composer-generated/input.md), beside their expectations.
2. [Composition](../../off-ledger/shared/src/main/scala/harmonia/composition/model/Composition.scala) validates names, finite actors/actions, order, and role consistency. [WorkspaceCommand](../../off-ledger/shared/src/main/scala/harmonia/workspace/WorkspaceCommand.scala) carries a typed proposal, acceptance, cancellation, or advance.
3. [ComposerCommands](../../off-ledger/jvm/src/main/scala/harmonia/composition/ledger/ComposerCommands.scala) maps commands to [Composer choices](../../on-ledger/composition/daml/Composer.daml). Core still executes the resulting process.
4. [ComposerSnapshot](../../off-ledger/jvm/src/main/scala/harmonia/composition/ledger/ComposerSnapshot.scala) translates ledger records at the external boundary. [CompositionState](../../off-ledger/shared/src/main/scala/harmonia/composition/CompositionState.scala) is decoded once before the browser view.
5. [CompositionEditor](../../off-ledger/browser/src/main/scala/harmonia/composition/CompositionEditor.scala) owns unfinished input. [LiveView](../../off-ledger/browser/src/main/scala/harmonia/live/LiveView.scala) keeps its DOM mounted while updating observed processes. [CheckComposer](../../off-ledger/jvm/src/main/scala/harmonia/composition/verify/CheckComposer.scala) and [Chapter 8](../../book/08-compose-a-workflow.md) establish the executable proof and explanation.

The former `composer` Scala package is consistently named `composition`. Literal JSON field access remains inside SDK/transport projection, where external data is decoded; views and commands use meaningful Scala types. The Daml package name `harmonia-composer` remains stable because the folder change does not justify changing business package identity.

## Inspect an application and generate its adapter

1. Read [the package input example](../../examples/evaluations/package-builder/input.md), [pinned sources](../../packages/inputs.md), and [the financing mapping](../../packages/mappings/financing.md).
2. [PackageBuilder](../../off-ledger/jvm/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala) coordinates bounded upload/retrieval, reviewed mapping selection, and project download. [ResolvePackages](../../off-ledger/jvm/src/main/scala/harmonia/packages/resolve/ResolvePackages.scala) checks archive and main-package identities.
3. [LfArchive](../../off-ledger/jvm/src/main/scala/harmonia/packages/inspect/LfArchive.scala) reads the public protobuf schema. [TemplateShapeReader](../../off-ledger/jvm/src/main/scala/harmonia/bindings/inspect/TemplateShapeReader.scala) checks real field and choice types. It no longer interprets formatted compiler output.
4. [GenerateSources](../../off-ledger/jvm/src/main/scala/harmonia/bindings/generate/GenerateSources.scala) is pure source generation. [GenerateBinding](../../off-ledger/jvm/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala) owns file creation, compilation, and identity checks. [ProjectArchive](../../off-ledger/jvm/src/main/scala/harmonia/packages/workspace/ProjectArchive.scala) creates the portable result.
5. [CheckBindings](../../off-ledger/jvm/src/main/scala/harmonia/bindings/verify/CheckBindings.scala) proves both applications, determinism, and detection of a compiled regression. [Chapter 7](../../book/07-generated-bindings.md) presents that evidence.

Dependency direction is explicit: package workspace coordinates generation; binding generation uses package acquisition/inspection; inspection knows nothing about the workspace or generation. The finite supported mappings remain a reviewed list. New arbitrary runtime integrations are outside the demonstrated scope.

## How the book stays honest

[Examples](../../off-ledger/shared/src/main/scala/harmonia/examples/Examples.scala) owns membership and chapter coverage. [PresentStory](../../off-ledger/jvm/src/main/scala/harmonia/book/project/PresentStory.scala) supplies reader labels from actual observations, while [BoundaryPhases](../../off-ledger/jvm/src/main/scala/harmonia/book/project/BoundaryPhases.scala) explicitly describes verification phases. [RecordedStory](../../off-ledger/shared/src/main/scala/harmonia/book/RecordedStory.scala) decodes the presentation and retains input, expectation, actual result, and provenance. Comparisons still use the complete independent results.

[BookNavigation](../../off-ledger/browser/src/main/scala/harmonia/book/BookNavigation.scala) preserves chapter/example/attempt context. [Inspector](../../off-ledger/browser/src/main/scala/harmonia/book/Inspector.scala) opens raw evidence or source without losing the reader's place. Source, charts, and comparison are different views of retained artifacts; none replaces the golden test.
