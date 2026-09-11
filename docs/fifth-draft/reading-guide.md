# Read one operation

The five directories under [product](../../product/README.md) are the entry point. Each operation uses ordinary immutable values and concrete IO; HTTP, participant connections, and background work have explicit owners. The [file-level plan](implementation-plan.md) records each change and its evidence.

## Approve financing

1. [FinancingPanel](../../product/web/src/main/scala/harmonia/financing/FinancingPanel.scala) offers the supported `FinancingAction`. [LiveApp](../../product/web/src/main/scala/harmonia/live/LiveApp.scala) creates an `ActionRequest` containing its identity, command, and observed version. Unconfirmed retries retain that same value.
2. [LiveServer](../../product/server/src/main/scala/harmonia/app/http/LiveServer.scala) checks the origin and authenticated session. [RequestBody](../../product/server/src/main/scala/harmonia/app/http/RequestBody.scala) bounds the bytes; the shared [ActionRequest](../../product/api/src/main/scala/harmonia/workspace/ActionRequest.scala) decoder checks the envelope and command. The actor comes from the session.
3. [Workspace](../../product/server/src/main/scala/harmonia/app/workspace/Workspace.scala) observes the participant. [FinancingObservation](../../product/server/src/main/scala/harmonia/financing/FinancingObservation.scala) decodes visible contracts once. The 19-line [Financing](../../product/server/src/main/scala/harmonia/financing/Financing.scala) selector names the required contract and ledger choice.
4. [Submissions](../../product/server/src/main/scala/harmonia/submission/Submissions.scala) checks request reuse and the observed version before running the prepared effect. Its supervisor owns the work. [SubmitChoice](../../product/server/src/main/scala/harmonia/ledger/client/SubmitChoice.scala) distinguishes confirmed commits, definite ledger rejection, and uncertain completion.
5. A refreshed snapshot supplies confirmed command IDs for reconciliation and typed state for the browser. Submission jobs retain status and detail; raw transaction history stays in the ledger observations.

Read the independent [live input](../../examples/evaluations/live-handoff/input.md), [business expectation](../../examples/evaluations/live-handoff/expected.md), and [authority expectation](../../examples/evaluations/live-handoff/security-expected.md). The ledger remains the authority for approval and disclosure.

## Propose and execute a composition

1. [CompositionEditor](../../product/web/src/main/scala/harmonia/composition/CompositionEditor.scala) builds a typed [Composition](../../product/api/src/main/scala/harmonia/composition/model/Composition.scala). Its validation checks names, roles, ordered actions, and duplicate steps. HTTP decoding invokes the same validation.
2. [CompositionCommand](../../product/api/src/main/scala/harmonia/composition/CompositionCommand.scala) names propose, accept, cancel, and advance requests. The shared request and submission path above preserves identity and retries.
3. [ComposerCommands](../../product/server/src/main/scala/harmonia/composition/ledger/ComposerCommands.scala) selects the supported ledger exercise. [Composer.daml](../../product/ledger/composition/daml/Composer.daml) enforces consent and creates the process; application choices enforce their own authority.
4. [ComposerSnapshot](../../product/server/src/main/scala/harmonia/composition/ledger/ComposerSnapshot.scala) turns visible contracts into the shared composition state. The editor remains mounted during refresh.

The independent [direct expectation](../../examples/evaluations/composer-direct/expected.md) and [generated-adapter expectation](../../examples/evaluations/composer-generated/expected.md) cover both execution routes. The [execution-boundary story](../../examples/evaluations/execution-boundaries/input.md) explains the atomicity limits.

## Inspect, generate, and download a package

1. [PackageBuilder](../../product/server/src/main/scala/harmonia/packages/workspace/PackageBuilder.scala) serializes bounded uploads or participant retrieval. [ResolvePackages](../../product/server/src/main/scala/harmonia/packages/resolve/ResolvePackages.scala) acquires bytes, verifies the pinned digest, and returns the pin, path, and inspection as `ResolvedPackage`.
2. [InspectDar](../../product/server/src/main/scala/harmonia/packages/inspect/InspectDar.scala) reads bounded structured LF and compiler metadata, checks that the main package identity agrees, and returns `InspectedDar`. Raw metadata remains saved evidence. [BuilderInput](../../product/server/src/main/scala/harmonia/packages/workspace/BuilderInput.scala) derives generation availability from these facts.
3. [GenerateBinding](../../product/server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala) reads the authored mapping. [TemplateShapeReader](../../product/server/src/main/scala/harmonia/bindings/inspect/TemplateShapeReader.scala) validates the mapped fields against structured LF. [GenerateSources](../../product/server/src/main/scala/harmonia/bindings/generate/GenerateSources.scala) emits the typed Daml adapter and example; compilation proves they typecheck.
4. The returned `GeneratedProject` carries paths, member names, source digest, and the final manifest. [ProjectArchive](../../product/server/src/main/scala/harmonia/packages/workspace/ProjectArchive.scala) checks containment and regular files, then builds a deterministic ZIP from those members. The producer does not reread its own manifest to continue.

The [builder expectation](../../examples/evaluations/package-builder/expected.md) covers package behavior. The [portable check](../../harness/runner/src/main/scala/harmonia/verification/CheckPortableProject.scala) independently opens the archive, rebuilds from empty outputs, compares DAR hashes, and executes the existing golden on a real ledger.

## Why some decoding and lookups remain

Compiler metadata, ledger observations, HTTP, authored Markdown/YAML, saved browser state, and an independently opened ZIP are actual input boundaries. They must be decoded and validated. Reads of an existing generation ownership marker protect an output directory supplied by the caller. Harness reads of saved files verify the artifact itself.

Generated field lookup relies on the exact field/key agreement already established by `TemplateShapeReader`; identifier and authored mapping readers reject unsupported names and types before generation. Raw compiler dependency metadata remains JSON because it is external evidence. These are localized boundaries with explicit validation, not an internal JSON protocol between operations.

The [book](../../book/README.md) and [harness](../../harness/README.md) remain separate from product compilation and runtime. Their models, presentations, and verification machinery deepen a review without entering the product's call path.
