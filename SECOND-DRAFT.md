# Harmonia, second draft

**Status:** implementation authorized as one continuous pass, following this documented target.  
**Working branch:** `second-draft`  
**Preserved first draft:** `first-draft` at `d58c1136b1b90180bb36bc5b69ac43131051003e`  
**Verified first release:** `dd5cf02754241730dc6cfed71e0b92216d657da0`

The second draft makes Harmonia straightforward to read, explain, and extend while preserving its demonstrated behavior. Its intended reviewer is a senior developer who understands software engineering and has limited time to learn this particular system. Search tools and compiler feedback help development; they are not the measure of whether the design explains itself.

This is the active plan for the second draft. The completed [first implementation PRD](PRD.md), [release evidence](docs/verification/19-release.md), and existing [capability matrix](docs/capabilities.md) establish its behavioral baseline. Checkboxes here cover implementation, documentation, and verification.

## The final version

A reader opens the book at one concrete story: a bank makes a private decision, and a buyer uses its result in a shared process. The reader can step through what happened, inspect who observed it, compare the committed expectation with the actual result, and open the code that owns the action. Deeper chapters develop composition, separate applications, generated bindings, and atomic transfer using the same interaction vocabulary.

A developer following that story finds a named operation, its meaningful inputs and outcomes, the ledger boundary it uses, and its independent golden. Feature names are consistent across Scala models, JVM operations, browser presentation, tests, and chapter links. The Daml choice remains the authority for the business transition.

The final local release includes all currently demonstrated capabilities, a coherent book and live workspace, a clean source revision, fresh evidence, and working bounded launchers. The goal is a reviewable implementation whose public operations and dependencies communicate its design.

## Principles we will hold to

1. **Make the reading path apparent.** Give each capability an obvious entry point. A reviewer should be able to follow the input, operation, ledger choice, result, and test through named neighbors and direct references.
2. **Give each responsibility one owner.** Request delivery owns retries and uncertain completion. A feature owns its operation and permitted observations. Presentation owns how those observations are explained. Wiring belongs at the application boundary.
3. **Let the compiler check internal relationships.** Use exhaustive command/state types and structured package metadata. Do not infer business behavior from string searches, formatted compiler output, or incidental JSON fields. Parse external text once at an explicit boundary.
4. **Use types to express meaning.** Decode external data into validated commands and states at a named boundary. Missing or malformed required data produces an explicit failure. Preserve raw observations alongside their interpreted form.
5. **Keep Scala direct and functional.** Prefer concrete `IO`, immutable case classes and enums, ordinary functions, and `Resource`. Introduce an interface or shared abstraction when it makes a present boundary clearer. Keep platform interoperation local.
6. **Organize around capabilities.** Use the same feature vocabulary across targets. Create subfolders when they have a distinct responsibility; create build modules when they enforce a useful dependency boundary. Keep small related operations together.
7. **Preserve independent evidence.** Expected results remain authored commitments. Execute inputs, observe actual behavior, then compare. File moves and presentation changes must preserve meaningful values, identity, order, permissions, and failure effects.
8. **Explain before exposing detail.** Give each screen a clear primary task. Introduce concepts through a story. Place raw events, package identities, and provenance within reach when the reader wants evidence.
9. **Keep resources visibly owned.** Connections, polling, subscriptions, subprocesses, and browser components have defined lifetimes. Cancellation, failure, and normal completion release their resources. Tests and builds run sequentially within the existing memory budget.
10. **Prove complete slices.** Each increment leaves a working version with its relevant behavior checked. The first migrated feature establishes a concrete pattern before the rest follow it.
11. **Finish the migration.** Each replacement removes the old responsibility and updates its tests and explanation. Temporary compatibility code has a named removal step. The final entry points use the second-draft path throughout.

## The design documents

| Document | Owns |
| --- | --- |
| [Architecture](docs/second-draft/architecture.md) | Feature boundaries, target source organization, typed operations, dependency direction, and migration map |
| [Book and live experience](docs/second-draft/reader.md) | Navigation, progressive explanation, visual language, state ownership, and interaction checks |
| This plan | Principles, ordered commits, review evidence, and final acceptance |

The architecture and screen layouts describe the target. They are not claims that the migration is already implemented. Existing architecture notes describe the first draft until their corresponding migration step updates them.

## Behavior carried forward

The supported scope remains the [demonstrated capability matrix](docs/capabilities.md): direct and generated participation; progression, branches, and joins; private result handoff; financing to property offer; atomic four-party transfer; authenticated live sessions; the consented composer; and bounded package input, generation, and export.

The baseline has 27 regular stories and five further reader recordings: authenticated handoff, two composer stories, the package builder, and execution bounds. Required checks also include Daml and Scala tests, package reproduction, generation determinism, detection of a deliberately compiled regression, participant visibility, concurrent advances, and portable project reproduction.

Preserve the existing inputs and expectations by content when moving them. Keep one explicit inventory mapping their old and new locations. Add focused checks only for new or previously unverified behavior. If an intended behavior must change, document it separately and review the exact baseline change; a refactor is not a reason to bless a mismatch.

The current toolchain and Daml package identities stay pinned during the initial restructuring. Platform upgrades, wider workflow semantics, and new integrations would need separate justification and evidence. File organization can change without silently changing the demonstrated product contract.

## Ordered implementation commits

Execute this plan in one autonomous implementation pass, with small commits and appropriate checks along the way. These steps are work boundaries, not repeated approval gates.

The first three actual commits on `second-draft` form one acceptance sequence: document the target, establish the financing boundary, then prove a complete working financing slice. Later steps may use several small commits; the progress record must identify them.

### SD01 — Establish the second-draft target

- [x] Preserve `first-draft` and create `second-draft` from the current clean version.
- [x] Document the principles, architecture, reader experience, and ordered plan.
- [x] Link the active plan from the repository entry points and identify the first-draft baseline.
- [x] Validate document links and distinguish planned work from implemented work.

### SD02 — Give financing a typed boundary

- [x] Introduce meaningful financing commands, observations, and results under consistent feature ownership.
- [x] Add strict transport/ledger decoding at the boundary and focused malformed-input/observation checks.
- [x] Adapt the existing financing path to the new types while preserving its wire behavior and passing examples.
- [x] **Acceptance:** the primary operation and its data contract are readable together; SDK values and raw JSON do not flow into the financing view.

### SD03 — Prove the complete financing slice

- [x] Connect the financing operation, authenticated participant dependency, and typed browser presentation.
- [x] Give the first chapter a direct path from one approval attempt to its observation, expectation, and relevant code.
- [x] Run actual approval, wrong-actor, and failure/recovery checks with unchanged business expectations; capture the browser result.
- [x] **Acceptance:** the third second-draft commit proves a real ledger action and a readable input-to-result path, and records where a reviewer still needs explanation.

### SD04 — Separate delivery and resource ownership

- [x] Extract submission tracking, duplicate protection, stale-view handling, and uncertain-result reconciliation from feature action selection.
- [x] Give application wiring explicit ownership of authenticated ledger clients, HTTP servers, subprocesses, and polling.
- [x] Preserve bounded queries, exact package identity checks, auth checks, and the distinction between observation failure and definite rejection.
- [x] **Acceptance:** the submission lifecycle contains no financing/composer business decisions; fault and cancellation checks still pass with no retained ledger/build processes.

### SD05 — Complete composition as a feature

- [ ] Group proposal, acceptance, cancellation, execution, and observations under consistent composition ownership.
- [ ] Decode composition state once; keep the editor's draft state separate from refreshed server observations.
- [ ] Use the shared delivery lifecycle and prove both direct and generated composer stories.
- [ ] **Acceptance:** a developer can follow consent and execution from the feature entry points, and refresh preserves the reader's unfinished input and focus.

### SD06 — Clarify package and binding responsibilities

- [ ] Give package acquisition/inspection and reviewed binding generation explicit APIs and an acyclic dependency direction.
- [ ] Replace formatted compiler-text matching with structured LF metadata for supported binding shapes.
- [ ] Keep compilation, archive creation, and downloads owned and bounded; expose typed progress and diagnostics to the workspace.
- [ ] Reproduce package identities, both generated projects, determinism, the deliberate regression check, and portable output.
- [ ] **Acceptance:** the feature trace explains exactly what is acquired, validated, generated, compiled, and downloaded; unsupported shapes remain explicit.

### SD07 — Separate observations from reader presentation

- [ ] Introduce a validated recording envelope with explicit typed presentation sections and linked raw evidence.
- [ ] Move scenario-specific interpretation into named projectors; remove script-name and JSON-shape inference from general reader models/views.
- [ ] Keep comparison based on independent expected and actual results, including meaningful missing data.
- [ ] **Acceptance:** all 32 baseline examples remain renderable; a boundary report is represented honestly as verification phases rather than fabricated workflow actions.

### SD08 — Clarify ledger packages and example ownership

- [ ] Group interfaces, core, reference applications, and bindings by responsibility; give the live demo assembly and integration tests accurate ownership and names.
- [ ] Consolidate story/evaluation discovery into one example inventory without duplicating scenario facts or changing baseline contents.
- [ ] Replace implicit source paths and repeated package/example lists with explicit build/runtime inputs where those lists currently disagree or obscure ownership.
- [ ] **Acceptance:** core depends on common interfaces; application/test/demo dependencies are explicit and acyclic; all moved examples and preserved package identities are accounted for.

### SD09 — Complete the book and live workspace

- [ ] Apply the [reader design](docs/second-draft/reader.md) across all chapters and demonstrations, with a shared visual vocabulary and stable component lifetimes.
- [ ] Provide contextual code/evidence inspection, navigable reader state, and a clear return to the story.
- [ ] Give the live workspace clear financing, composition, and package tasks under its authenticated session.
- [ ] **Acceptance:** desktop and narrow-screen walkthroughs cover keyboard access, focus, editing, refresh, pending/uncertain/rejected states, and all baseline recordings.

### SD10 — Remove migration scaffolding and finish the reading guide

- [ ] Delete replaced coordinators, duplicate codecs, obsolete paths, and completed compatibility adapters.
- [ ] Update the repository map, architecture notes, chapter source links, and developer walkthrough to the actual final structure.
- [ ] Record before/after traces for financing, composition, and package generation, including remaining unavoidable cross-feature boundaries.
- [ ] **Acceptance:** examples can be followed through their public operations without relying on the original implementation narrative, and the shipped source contains one active path for each migrated responsibility.

### SD11 — Produce the second-draft release

- [ ] Run the complete required gate sequentially from a clean source revision, retaining independent outputs and diffs.
- [ ] Package software, book, example inventory, and fresh evidence from that same revision; verify hashes and relocated launchers.
- [ ] Record current memory/process measurements, supported limits, exact commits, and the source-reading walkthrough.
- [ ] **Acceptance:** both reference workflows and integration paths still work, every required baseline example is present, the book matches the code, and the release is reproducible with the documented environment.

## How we judge the finished version

| Question | Required evidence |
| --- | --- |
| Can a reviewer find who owns an action? | A short trace from scenario to named operation, ledger choice, observed result, test, and chapter |
| Do types clarify the interaction? | Boundary decoding tests; domain operations and views accepting meaningful values rather than interpreted JSON fragments |
| Are feature boundaries real? | A documented dependency direction, no reverse imports from delivery/reader infrastructure into individual business features, and focused build/import checks where useful |
| Does the UI preserve the user's work? | Recorded desktop/mobile interactions covering editing, focus, refresh, navigation, and recovery |
| Did behavior survive the refactor? | Unchanged independent expectations, meaningful negative cases, the complete required gate, and an explicit example inventory |
| Is the runtime still economical? | Bounded heaps, sequential heavy checks, scoped processes/subscriptions, measured cleanup, and one retained preview at most |

A file-count or line-count reduction alone does not establish readability. Review notes must explain which indirections, mixed responsibilities, repeated interpretations, or misleading names were removed. Any claimed human-review outcome requires an actual review; the local deliverable includes the material needed for that review.

## Progress

SD01 (`f40cb17`), SD02 (`eafeea7`), and SD03 are complete. SD03 passed 30 Scala tests, JVM/browser compilation, and the authenticated real-ledger check (approval, wrong actor/API bypass, stale views, deduplication, and reconnect). In the browser the Bank approved, the Buyer continued, the submission was confirmed, and the Buyer could not see the private financing details. Evidence: `.artifacts/second-03-build.log`, `.artifacts/second-03-live.log`, and `.artifacts/live-check-186607939419738880/live-handoff/`; browser capture `second-draft-financing-complete`.

The chapter links distinguish the introductory shared application from the private live handoff. The next steps remove the remaining mixed ownership in `LiveActions` and `LiveSnapshot`, then apply typed boundaries to composition and package inspection. The preserved first-draft book remains available during migration.

SD04 separates `submission/Submissions`, `ledger/client`, `financing/FinancingObservation`, and `app/workspace/Workspace`. Thirty Scala tests pass, including observation failures versus definite submission rejection; JVM and browser builds pass (`.artifacts/second-04-final.log`). HTTP diagnostics retain the actual validation error. The full release gate will repeat the real ledger recovery and resource checks.
