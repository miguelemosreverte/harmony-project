# Harmonia: working software, executable stories, and an interactive book

**Product requirements and linear implementation plan**  
**Date:** 2026-09-10  
**Status:** Implementation authorized; proceed in the commit order below  
**Product name:** Harmonia; the current workspace is named `harmony-project`.

Harmonia lets independently developed Canton/Daml applications participate in a shared business process. The ledger enforces eligible transitions and application permissions. A reader learns the system through working stories whose committed inputs and expected outputs also power regression tests and interactive explanations.

**The product is delivered as working software and its accompanying book, developed together.** Every demonstrated capability must lead back to a reproducible example and evidence of its actual behavior.

## Reading this document

- [1. Purpose and readers](#1-purpose-and-readers)
- [2. Principles agreed in discussion](#2-principles-agreed-in-discussion)
- [3. End deliverables](#3-end-deliverables)
- [4. Release scope](#4-release-scope)
- [5. Architecture and decisions](#5-architecture-and-decisions)
- [6. Executable stories and golden files](#6-executable-stories-and-golden-files)
- [7. The book and interactive demonstrations](#7-the-book-and-interactive-demonstrations)
- [8. Linear commit plan](#8-linear-commit-plan)
- [9. Release acceptance checklist](#9-release-acceptance-checklist)
- [10. Open decisions and evidence gates](#10-open-decisions-and-evidence-gates)
- [11. Relationship to the proposal](#11-relationship-to-the-proposal)

## 1. Purpose and readers

### The problem

Organizations already own applications for approvals, assets, custody, and offers. Connecting them into a larger process often requires custom integration code. Harmonia provides a reusable model for the shared progression: which step is available, who may perform it, what evidence it requires, and what its result enables.

For example, a bank's financing process produces an approval. A buyer uses that approval to begin a separately owned property-offer process. Harmonia connects the two processes while preserving their owners, permissions, and relevant privacy boundaries.

### The reader's journey

The assumed opening audience is a technically curious evaluator who understands a business process but may be new to Canton. This is a proposed default; the first-reader preference remains open. Later chapters serve developers integrating their own applications.

A reader should be able to:

- Understand a concrete business story and its participants.
- Explore a recorded execution without first operating a ledger.
- Run the same story against a local Canton environment.
- Inspect its committed input, expected output, actual output, and differences.
- Change an input or attempt an unauthorized action and observe the result.
- Trace that behavior into readable Daml and integration code.
- Reuse the demonstrated integration pattern in another application.

### Starting point

At the planning baseline, the workspace contained only the imported [development proposal](harmonia.md), [architecture illustration](harmonia-architecture.html), and planning documents. Implementation and frequent local commits are now authorized. The [progress record](docs/progress.md) identifies delivered commits, verification evidence, and the next step.

The proposal establishes the intended release scope. The illustration supplies a useful design sketch; its template names and several package relationships are explicitly provisional. This PRD records our subsequent product and engineering decisions without treating those sketches as proven implementations.

## 2. Principles agreed in discussion

1. **Keep a working version at every increment.** Each implementation commit leaves a buildable, demonstrable state. Early demonstrations may be narrow; they must accurately describe what they prove.
2. **Develop software and book together.** A capability arrives with its story, expectation, and explanation. Chapters evolve alongside the implementation.
3. **Use concrete stories as executable specifications.** Committed inputs and committed expected results are the baseline. A disagreement fails the regression check.
4. **Make the same evidence renderable.** Tests, charts, and chapters consume the same structured story and result formats.
5. **Keep code simple and understandable.** Prefer small modules, explicit rules, familiar names, and abstractions supported by actual examples.
6. **Demonstrate claims.** Authorization, privacy, atomicity, and reusable composition need executable evidence, including meaningful failure cases.
7. **Keep application authority intact.** A workflow assignment does not grant permission to perform an underlying application action.
8. **Keep workflow correctness on the ledger.** Clients submit commands and display results. Persisted state and transition rules remain authoritative on the ledger.
9. **Make baseline changes deliberate.** Ordinary test runs never rewrite expected results. A baseline change includes a readable diff and a reason for the changed behavior.
10. **Distinguish evidence from presentation.** A committed expectation, a recorded execution, a fresh execution, and a simulation are visibly different kinds of content.
11. **Deliver a bounded, reusable system.** Supported workflow shapes and integration boundaries are explicit. Unsupported requests receive useful explanations.
12. **Write artifacts for people.** Markdown and YAML must be intuitive to read and edit. Give each fact one authoritative home, avoid redundant bookkeeping, and keep the prose focused on intent instead of paraphrasing every data field.
13. **Write organized functional Scala off-ledger.** Use Cats Effect with concrete `IO` for effects, pure functions for transformations, immutable data, and explicit resource ownership. Group related code into vertical feature slices with meaningful names.
14. **Prove working behavior by the third commit.** Commit 03 must execute a real local Canton/Daml action and compare observed results with a committed golden. Format-only tests cannot satisfy this gate. Continue making small commits after appropriate validation.

## 3. End deliverables

| Deliverable | What the reader receives | Evidence of completion |
| --- | --- | --- |
| Working Harmonia release | Reusable Daml core, application interfaces, eligible bindings, and versioned build artifacts | Both participation paths execute through the same core-managed model |
| Story collection | Markdown inputs and committed Markdown golden outputs, including failure stories | Clean runs match expectations; deliberate discrepancies fail |
| Interactive book | Chapters connecting business stories, visual executions, source, and experiments | Every demonstrated capability references the matching version and story |
| Viewer and bounded composer | Workflow graph, event progression, participant views, current state, and eligible actions | Recorded and live modes are clear; live operations use ledger authorization |
| Build-time tooling | Readable generated bindings and project boilerplate for eligible existing DARs | Generated projects build and reproduce the hand-written integration's business behavior |
| Reference applications | Independently packaged applications covering financing/offer and four-party transfer | At least two source-application DARs and two reproducible end-to-end workflows |
| Evaluation kit | Setup instructions, commands, troubleshooting, supported boundaries, and walkthrough material | A fresh environment can reproduce the documented release |

## 4. Release scope

### Supported capabilities

- Workflow definitions with explicit versions, start states, end states, and party-bound roles.
- Persisted workflow instances and observable progression.
- Human-triggered and system-triggered actions using explicitly authorized parties.
- Eligible application contract creation and choice exercise through typed integration code.
- Sequential progression and exclusive branches.
- Basic joins with precisely specified prerequisite semantics.
- Explicit waiting for a required action or valid result.
- Continuation from one independently owned workflow into another.
- Staged multi-party coordination and bounded atomic execution when valid.
- Direct application interface implementation and generated integration for eligible existing DARs.
- Local DAR inputs and reproducible retrieval from a selected package source.
- A simple viewer and composer using the supported workflow vocabulary.

### Boundaries

- Full BPMN compatibility, a generic BPMN editor, arbitrary dynamic choice dispatch, timers, escalations, compensation, rich exception handling, and reusable subprocess libraries are outside this release.
- Waiting means persisted state awaiting a permitted command or result. It does not imply a scheduler or automatic expiry service.
- A system step still needs a submitting client or party-owned automation. On-ledger rules do not spontaneously submit transactions.
- Managed hosting, production operations, custom adopter integrations, and domain-specific production products are outside scope.
- Independently owned business domains do not automatically imply execution across different Canton synchronizers. Deployment topology and any reassignment boundary must be stated separately.
- Existing DAR integration is limited to choices, types, permissions, and disclosure patterns the implementation can support. The builder must report ineligible cases.
- Reference assets and financing evidence are synthetic examples. The demonstrations make no claim of production financial integration.

## 5. Architecture and decisions

### Agreed direction and proposed realization

| Area | Decision or starting proposal | Status |
| --- | --- | --- |
| Execution | Daml owns workflow transitions and validation of application actions/results | Agreed direction |
| User interface | A thin viewer/composer submits commands and displays ledger-derived state | Agreed direction |
| Integration | Both participation paths converge on the same core model | Required by proposal |
| Existing applications | Prove a hand-written adapter against an unchanged source DAR before generating it | Implemented for the demonstrated approval binding; see ADR 001 |
| Story artifacts | Human-readable Markdown with a concise, designated YAML block; avoid duplicated metadata and narrative | Agreed format and readability requirement |
| Regression checks | Compare structured expected and observed results; produce renderable differences | Agreed direction |
| Packaging | Start with a monorepo and explicit dependencies between logical packages | Proposed implementation default |
| Shared interfaces | Consider a small, separate interface package to avoid coupling source apps to core implementation | Implemented; see ADR 001 |
| State privacy | Separate shared progress from private domain payloads and disclose only required results | Implemented and observed on separate participants; see ADR 003 |
| Off-ledger language | Scala for the runner, builder, services, book tooling, and interactive application code; Daml remains on-ledger | User requirement |
| Effect style | Cats Effect with concrete `IO` for effects; pure functions for deterministic work; `Resource[IO, A]` for owned resources | User direction, with Cats Effect as the interpretation of the spoken library name |
| Code organization | Vertical feature slices, cohesive objects/functions, immutable models, explicit dependencies, and mirrored test packages | User requirement; concrete layout proposed |
| Runtime targets | JVM for tools/services and Scala.js for browser interactions; Scala 3 is the starting proposal | Implemented and pinned for both targets; see ADR 002 |

### Logical components

| Component | Responsibility | Dependency boundary |
| --- | --- | --- |
| `harmonia-core` | Definitions, instances, step rules, workflow state, continuation, execution bounds | Must not acquire a dependency on every source application |
| Shared interfaces | Common action and result contracts used by the core and integrations | Keep application-specific template identifiers and code out of the common API |
| Binding DARs | Typed implementations connecting supported source actions to the common model | Depend on the relevant source packages and common API; avoid circular imports |
| `harmonia-builder` | Validate explicit mappings and generate binding/project code | Build time only; generated output is inspectable |
| `harmonia-references` | Independent sample applications and composed workflows | Application ownership remains visible in package and contract boundaries |
| Story runner | Parse inputs, drive real commands, capture actual observations, and compare results | Expected files are comparison inputs, never a source of execution results |
| Book/viewer | Render stories, recorded results, live results, and diffs | Reuse the runner's artifact schema; do not implement a second workflow engine |

The exact location of interface declarations differs between passages of the imported proposal. The early architecture decision record must resolve that ambiguity and explain the resulting dependency graph. A DAR is a distribution artifact containing packages; diagrams must distinguish package dependencies from deployed contracts.

### Scala code organization

The [Scala architecture note](docs/architecture/scala.md) defines the proposed source tree, dependency direction, effect conventions, and review criteria. Each feature, such as running a story or generating a binding, owns its related code. Keep the shared model small and platform-independent; keep ledger SDK and filesystem integration on the JVM. Select specific libraries within the Scala requirement as the relevant examples are implemented.

The [repository map](docs/architecture/repository.md) explains the root folders and the separate responsibilities of `bindings/`, `workflows/`, `ledger/`, and `files/` within the Scala implementation.

### Execution and evidence rules

- Advance a synchronous step together with its successful application action in one transaction.
- For staged actions, require a valid application-produced result or narrowly scoped authorization contract, tied to the correct instance, parties, and step.
- Verify issuer, subject, permitted use, and freshness where the supported story requires them. Prevent reuse where evidence is intended for one use.
- Never accept a client-supplied success flag as proof that an application action occurred.
- Keep role assignment, command-submission authority, application authorization, and visibility as distinct concepts.
- Inspect the authorization and disclosure consequences of the full transaction tree. A shared parent contract must not accidentally expose private child operations.
- An unavailable participant, missing view, or incomplete observation must be reported as incomplete or failed evidence; it cannot count as proof of privacy.
- Start the multi-participant acceptance environment on a common synchronizer. Broader topology claims need their own executable evidence.
- UI commands must remain valid only when submitted under the correct party authority, including when someone bypasses the UI.

### Provisional contract concepts

`WorkflowDefinition`, `WorkflowInstance`, `StepAssignment`, an action interface, and a continuation/result contract are useful starting concepts. Their exact names, payloads, signatories, and consuming choices are design outputs. In particular, the illustration's instance signed by every bound party must be evaluated for disclosure, authority, and contention before adoption.

## 6. Executable stories and golden files

### Files and ownership

```text
stories/
  financing-approved/
    input.md
    expected.md
  financing-rejected/
    input.md
    expected.md

.artifacts/                         # Generated local/CI evidence; ignored by Git
  <run-id>/
    <story-id>/
      actual.md
      diff.md
      run.json                     # Execution provenance and diagnostics references
```

Committed story inputs and expectations are human-reviewed product specifications. Generated actual results are evidence from a run. Selected successful results can be bundled into a versioned book release with their provenance; transient run directories stay out of source control.

### Markdown contract

- `input.md` has one designated `yaml` fenced block directly under a `## Scenario` heading.
- `expected.md` and `actual.md` have one designated `yaml` fenced block directly under a `## Result` heading.
- Other prose and illustrative code blocks are allowed and are not executable.
- The parser uses Markdown structure to locate the designated block and rejects missing or ambiguous blocks.
- Infer story identity from its directory and document kind from its filename. Record the schema version once in the story collection's configuration; include that version in exported run provenance.
- An explicit schema rejects unknown fields, duplicate YAML keys, invalid value types, unresolved references, and unsupported actions.
- The YAML loader must reject executable/custom tags and use predictable scalar handling. Dates, identifiers, and financial decimal quantities have explicit schema types; decimal quantities use strings where needed for exact values.
- Each supported action has a defined input/result schema. Integration mappings select typed compiled actions; YAML is not a language for arbitrary ledger calls.
- Schema evolution is explicit. A migration may propose edits; it cannot silently reinterpret older expectations.

### Human readability is an acceptance requirement

- A reader can identify the starting situation, actor, attempted action, and expected outcome without first learning the runner's internals.
- Prefer business names such as `application`, `approval`, `Alice`, and `Northbank` over opaque identifiers or positional names such as `contract-17`.
- Keep nesting shallow. Use lists for sequences, mappings for named things, and a consistent vocabulary across stories.
- Avoid wrappers and metadata that the surrounding file already establishes. Do not add `arguments`, `payload`, or `data` layers unless they clarify an actual distinction.
- Introduce setup facts once. Shared fixtures should be small, named, and directly inspectable; readers should not need a chain of overrides to understand a story.
- Record each observed fact in one place. Derive display-only summaries from those recorded facts instead of committing several copies of the same event, status, and progress summary.
- Repeat a name where it makes a reference clear, or a value where an independent expectation needs to assert it. Do not remove necessary assertions merely to shorten the file.
- Avoid YAML anchors, merge keys, templating, and hidden business defaults. A little explicit repetition is preferable to indirection that makes the story harder to follow.
- Prose explains motivation, consequences, and surprising behavior. It does not narrate the YAML line by line.
- Omit empty noise only where the schema defines omission precisely. An omitted effects collection means no such effects are expected, never that those effects go unchecked.
- Error messages identify the file, field, and business action with a useful explanation.
- Before adding fields to the format, show how they read in a small real story. Readability is reviewed alongside correctness.

### Canonical examples

The executable examples are maintained in one place:

- [Financing approval input](stories/financing-approved/input.md) and [its committed expectation](stories/financing-approved/expected.md).
- [Already-approved input](stories/already-approved/input.md) and [its committed expectation](stories/already-approved/expected.md).
- [Current format and observation scope](stories/README.md).

These files are the reader-facing specification used by the runner. They replace the initial inline schema sketches to avoid duplicating examples that could drift apart. As later commits extend the supported actions, update the format documentation and affected stories together.

The first observation scope includes the financing application's queried state, contract consumption, active application count, and the two parties' observed visibility. It is deliberately narrower than the final cross-application release. Unexpected effects inside the declared scope must fail comparison. Expected rejection stories record the actual rejected attempt and verify its unchanged effects.

### Comparison and determinism

- Compare validated structured values, independent of prose, YAML indentation, and mapping-key order.
- Preserve ordered action sequences and causal relationships. Treat a collection as unordered only when its schema explicitly says so.
- Use authored aliases for parties and known contracts. Assign deterministic aliases to new contracts while preserving distinct identities and references.
- Normalize incidental generated identifiers and run timestamps; retain raw values in execution evidence for traceability.
- Do not normalize away amounts, actor differences, package mismatches, missing events, authorization outcomes, or timing facts required by a tested condition.
- Compare the complete declared result scope, including unexpected effects and visibility. An expected file is not merely a subset of assertions the runner chooses to check.
- A rejected action can be an expected story outcome. A runner crash, disconnected participant, or observation failure cannot be substituted for that rejection.
- Control interleavings for deterministic concurrency goldens. Test genuinely unordered outcomes through explicitly defined invariants and causal constraints rather than sorting away a bug.
- Record source revision, input/expectation digests, package identities, runtime versions, topology, and execution mode in the run provenance.
- Preserve raw observations separately from normalization. The normalizer must not manufacture outcomes from the expected file.

### Baseline workflow

1. Author or change the story input and its intended behavior.
2. Run it against the relevant environment and capture actual output.
3. Review the structured and visual diff against the committed expected output.
4. Decide whether the difference is a regression or an intentional behavior change.
5. For an intentional change, explicitly update the expected file and explain the reason in the commit.
6. Re-run the story and required checks before committing the new baseline.

An explicit update command may assist step 5, but regular tests and CI never approve or overwrite goldens. New scenarios need a reviewed expectation before entering the regression suite. A mismatch exits unsuccessfully and retains useful artifacts.

## 7. The book and interactive demonstrations

### Proposed chapter sequence

| Chapter | Reader's question | Working demonstration |
| --- | --- | --- |
| 1. Meet Harmonia | What business problem does this solve? | A small approval story with input, expected result, and observed execution |
| 2. Read an execution | How do I know what happened? | Step through a run and inspect a deliberate golden mismatch |
| 3. Parties and permissions | Who may act, and who can see the result? | Switch recorded party views; compare accepted and rejected actions |
| 4. Waiting and decisions | How does a process advance, wait, branch, and join? | Exercise permitted paths and inspect a premature transition failure |
| 5. Applications working together | How does one owner's result enable another owner's process? | Financing approval followed by a separately owned offer workflow |
| 6. Atomic completion | Which actions happen together? | Four-party transfer, including a failure with no partial settlement |
| 7. Bring an existing application | How do I integrate a source DAR? | Inspect a hand-written adapter, generate its counterpart, and run both |
| 8. Compose and run | How can I define a supported workflow? | Use a bounded composer and execute under the appropriate party identity |
| 9. Extend and evaluate | What can I reuse, and where are the limits? | Add an eligible action/story and reproduce a versioned release |

### Chapter requirements

- Start with the business situation and the outcome the reader wants.
- Introduce the participants and ownership boundaries visually.
- Link to the committed input and expectation.
- Include an interactive execution using the same result schema as the regression suite.
- Explain the important states, decisions, and rejected attempts in plain language.
- Show only the code needed to explain the behavior; link to the full implementation.
- Offer a small experiment and an executable way to reproduce it.
- State the tested boundary and link to deeper technical detail.

### Visualization requirements

- A workflow graph shows current, completed, waiting, and available steps.
- A timeline or stepper shows attempted actions and their observed outcomes.
- Participant views show what the selected party actually observed.
- Contract inspection explains relevant fields, lifecycle changes, and references.
- Expected/actual comparison highlights the first meaningful divergence and affected state.
- Recorded playback works without a live ledger. Live execution connects to the documented local environment and displays its run identity.
- A recorded synthetic demo may switch perspectives freely. Live party selection never grants credentials or exposes another party's private data.
- The graph and timeline must distinguish causal ordering from presentation layout.
- Provide keyboard navigation, readable contrast, text explanations, and indicators that do not rely only on color.
- Adjustable experiments create new inputs/results; they do not overwrite a committed expectation.
- Runtime provenance belongs in an evidence/details view where it helps the reader assess a run.

The viewer renders evidence. It does not recompute the business process to guess what should have happened.

## 8. Linear commit plan

Work proceeds in the order below. Each top-level checkbox represents a planned commit, not an already completed action. Nested checkboxes specify its scope and acceptance evidence. Record the actual commit hash and evidence paths when completing an item.

Keep each commit reviewable and green. If an item proves too large, split it into explicitly recorded consecutive subcommits before marking it complete. Preserve the dependency order and working state. Feature commits include their story/chapter updates; the later documentation pass completes navigation and editorial consistency.

The first three actual Git commits have a fixed acceptance sequence: **document the plan → establish a working runtime → prove a golden story on the ledger**. Do not insert extra planning or scaffold commits before the third-commit proof. Later items may be split as needed, with actual hashes recorded in the progress log.

### Commit 01 — `docs: establish the Harmonia product and delivery baseline`

- [x] Record the agreed project baseline in Git.
  - [x] Initialize the repository and preserve both imported source documents with provenance.
  - [x] Commit this PRD, a short reader-oriented README, and an index of decisions still to prove.
  - [x] Record which imported diagram assets are unavailable instead of leaving readers to infer that they exist locally.
  - [x] Establish source, generated-artifact, and secret exclusions; record the open-source license decision before publication.
  - [x] **Acceptance:** a reader can find the purpose, sources, scope, next commit, and pending decisions from the README.

### Commit 02 — `build: add a reproducible local Canton and Daml baseline`

- [x] Establish a small runtime that can be rebuilt and exercised.
  - [x] Select and pin compatible runtime/compiler versions and required dependencies.
  - [x] Establish the Scala/JVM build with Cats Effect, formatting conventions, and the first feature/test packages following the Scala architecture note.
  - [x] Add a minimal Daml package and a successful local create/exercise smoke example.
  - [x] Document setup, start, stop, reset, and smoke-check commands with explicit prerequisites.
  - [x] Add build/smoke automation and continuous integration configuration.
  - [x] **Acceptance:** a fresh project environment builds and executes the documented example; the book's setup page reproduces it.

### Commit 03 — `feat: prove a Markdown golden story against the local ledger`

- [x] Implement the first versioned story/result contract.
  - [x] Add schemas, designated-block parsing, validation, and deterministic serialization.
  - [x] Implement reading and comparison as Scala feature slices, keeping pure comparison separate from `IO` file access.
  - [x] Add a small authored story/result pair that exercises the real Daml sample from Commit 02, plus focused comparator checks.
  - [x] Review the pair for plain names, shallow nesting, non-repetitive prose, and metadata recorded only where needed.
  - [x] Produce `actual.md`, `diff.md`, and run provenance without modifying `expected.md`.
  - [x] Verify malformed input fails clearly, prose-only edits are harmless, and a meaningful data mismatch fails the check.
  - [x] Drive the action from the parsed input using Scala `IO`, capture the ledger's response and queried state, and compare that observed result independently of the expected file.
  - [x] **Acceptance:** before creating the third Git commit, one documented command runs a real passing ledger story, and a deliberately changed expectation fails with a retained readable diff. The book explains exactly which behavior this small example proves.

### Commit 04 — `feat: execute the first direct-interface workflow story`

- [x] Connect the story runner to a minimal real workflow.
  - [x] Define the smallest common action interface and core transition needed by the story.
  - [x] Add an independent sample application implementing the interface directly.
  - [x] Execute the application action and corresponding workflow advance in one transaction.
  - [x] Capture actual events/state from the ledger and compare them with a committed golden.
  - [x] Include a successful action, an unauthorized attempt, and a failed application action with unchanged workflow state.
  - [x] **Acceptance:** Chapter 1's example runs against Canton; changing a relevant expectation causes a real regression failure.

### Commit 05 — `feat: prove an adapter for an unchanged source DAR`

- [x] Validate the second participation path before developing the generator.
  - [x] Package a source application without Harmonia interface implementations and record its artifact identity.
  - [x] Hand-write a typed adapter/binding against that unchanged artifact.
  - [x] Execute through the same core action model and verify equivalent externally meaningful behavior.
  - [x] Exercise authorization and application-failure cases across the adapter boundary.
  - [x] Record the package dependency graph, interface ownership, consuming behavior, and supported binding limits in an architecture decision record.
  - [x] **Acceptance:** both paths run successfully, their application boundaries remain explicit, and the original source DAR identity is unchanged.

### Commit 06 — `feat: render the first recorded stories in the book`

- [x] Deliver the first readable, interactive working edition.
  - [x] Validate the Scala.js browser target and choose compatible UI dependencies; share only the models needed by both targets.
  - [x] Add the book shell and Chapters 1–2 using the real artifacts from the preceding commits.
  - [x] Render a workflow graph, action stepper, contract details, and expected/actual differences.
  - [x] Link each demonstration to its input, expectation, source, and execution provenance.
  - [x] Label recorded observations and committed expectations distinctly.
  - [x] **Acceptance:** a reader can inspect a passing story and a deliberately divergent result without running a ledger; a walkthrough verifies the rendered differences.

### Commit 07 — `feat: enforce and demonstrate multi-party authority and privacy`

- [x] Prove role and visibility boundaries using separate participants.
  - [x] Model explicit role bindings and narrowly scoped authorization/consent where required.
  - [x] Separate private application state from shared progression and required results.
  - [x] Run parties on separate participant nodes with a stated common-synchronizer topology.
  - [x] Capture party-specific queries and events, including permitted visibility and forbidden disclosure.
  - [x] Add wrong-actor, missing-authority, and disclosure-boundary goldens plus Chapter 3's participant views.
  - [x] **Acceptance:** positive controls prove each observation channel works; unauthorized execution fails and private payloads remain outside the prohibited party's observed scope.

### Commit 08 — `feat: persist sequential progression and explicit wait states`

- [x] Extend the core beyond a single transition.
  - [x] Persist versioned definitions, current/completed steps, enabled actions, and relevant outputs.
  - [x] Add sequential steps, explicit waiting, and human/system submitter examples.
  - [x] Require trusted publisher authority for instance creation; reject direct forged completion of process and shared state.
  - [x] Define retry and command-deduplication behavior without double-advancing state.
  - [x] Reconnect the client after a handoff and recover the current state from the ledger.
  - [x] Add stories for premature execution, repeated submission, waiting, and resumed progression; extend Chapter 4.
  - [x] **Acceptance:** restarting the client loses no workflow progress, and both interrupted and uninterrupted runs have the expected application effects.

### Commit 09 — `feat: add exclusive branches and bounded joins`

- [x] Specify and implement the first branching/joining subset.
  - [x] Define exclusive branch selection, branch merge behavior, and explicit all-required-prerequisite joins where supported.
  - [x] Define which prerequisites can be active together and how completion is recorded; avoid ambiguous BPMN interpretations.
  - [x] Reject malformed definitions, impossible joins, unsupported cycles, and unsupported execution shapes before use.
  - [x] Add approve/reject, unselected-branch, premature-join, and repeated-join stories.
  - [x] **Acceptance:** Chapter 4 demonstrates every supported branch/join shape and explains the rejected shapes.

### Commit 10 — `feat: resolve and record reproducible application DAR inputs`

- [x] Establish the package-source boundary needed by examples and the builder.
  - [x] Support a local DAR and one selected external package source with explicit artifact identities.
  - [x] Record dependencies, required compatibility, digests, and source provenance.
  - [x] Demonstrate build-time retrieval for a reference project and repeat execution using the recorded inputs.
  - [x] Verify what package/DAR retrieval the chosen participant APIs actually support; document any reconstruction or administrative-access boundary.
  - [x] **Acceptance:** an unavailable or incompatible package gives an actionable failure; successful reproduction does not silently select a newer artifact.

### Commit 11 — `feat: compose financing and offer workflows through verified results`

- [x] Deliver the first full reference workflow across independently owned applications.
  - [x] Package financing and offer behavior independently and integrate through the direct interface path.
  - [x] Bind approval evidence to the correct issuer, buyer, application, and permitted continuation.
  - [x] Preserve bank, buyer, buyer-agent, and seller-agent ownership and relevant private state.
  - [x] Add approval-to-offer, rejection, wrong-subject, invalid-issuer, and reused-evidence stories.
  - [x] Add Chapter 5 with a visual handoff between the two applications.
  - [x] **Acceptance:** a valid financing result enables the separate offer process; invalid evidence cannot produce a purchase proposal through that continuation.

### Commit 12 — `feat: demonstrate bounded atomic four-party transfer`

This item is split into consecutive subcommits: **12a** separates the reference input models; **12b** implements and proves the atomic transfer. The first step prevents custody-specific data from accumulating in the financing model.

- [x] **12a — `refactor: separate reference story models and script inputs`**
  - [x] Give financing and purchase their own typed input models and Daml Script inputs behind the common runner contract.
  - [x] Preserve all nineteen existing golden results without changing their expectations.

- [ ] Deliver the second full reference workflow.
  - [ ] Model Buyer, Seller, source custodian, and destination custodian explicitly.
  - [ ] Bind the Settler role to one of those four parties, with the required authority and disclosure.
  - [ ] Record agreement, locked-position eligibility, and destination readiness as staged prerequisites.
  - [ ] Execute the eligible final path atomically and record its actual asset/workflow effects.
  - [ ] Add missing-approval, missing-lock, failed-final-leg, and successful-settlement stories plus Chapter 6.
  - [ ] **Acceptance:** a deliberately failing final operation produces no partial settlement; the chapter distinguishes prior staged commits from the atomic completion transaction.

### Commit 13 — `feat: generate typed bindings from explicit application mappings`

- [ ] Turn the proven hand-written adapter pattern into build-time tooling.
  - [ ] Implement package inspection and generation as Scala slices, with deterministic generation functions and explicit `IO` for package/filesystem operations.
  - [ ] Define a mapping schema for source templates/choices, typed arguments, results, roles, and eligible step kinds.
  - [ ] Accept a source DAR and authored mapping; validate supported types and known integration boundaries.
  - [ ] Generate readable binding code, project configuration, and a runnable example/test entry point.
  - [ ] Explain which authority and disclosure conditions still require runtime validation.
  - [ ] Reject unsupported mappings with concrete diagnostics; preserve source DAR identity.
  - [ ] **Acceptance:** generated output compiles without manual fixes and reproduces the hand-written adapter's supported business behavior; Chapter 7 shows the generated code.

### Commit 14 — `test: establish direct and generated integration parity`

- [ ] Make both participation paths part of the regular regression suite.
  - [ ] Run equivalent stories through direct implementations and generated bindings.
  - [ ] Compare shared business outcomes, permissions, and visibility; keep legitimate adapter-specific events explicit.
  - [ ] Check deterministic code generation for identical input packages and mappings.
  - [ ] Record supported package-version combinations and fail clearly on mismatches.
  - [ ] **Acceptance:** CI catches a deliberate generated-binding regression, and Chapter 7 makes the common behavior and path-specific mechanics visible.

### Commit 15 — `feat: connect the viewer to live participant-scoped execution`

- [ ] Add live interaction to the existing evidence viewer.
  - [ ] Read actual current workflow state and submit actions using configured party credentials.
  - [ ] Show pending, committed, rejected, disconnected, and stale-view states accurately.
  - [ ] Reuse the story/result format when retaining live demonstration evidence.
  - [ ] Expose actor, application, current/next steps, completion, and execution history in the inspection view.
  - [ ] Keep live identity enforcement separate from recorded-demo perspective switching.
  - [ ] **Acceptance:** two participant sessions can complete a handoff; UI bypass cannot authorize an invalid action and reconnecting recovers the committed state.

### Commit 16 — `feat: compose supported workflows and provide DAR inputs`

- [ ] Complete the bounded evaluation composer.
  - [ ] Let a user define a small workflow from supported actions and bind its roles and inputs.
  - [ ] Validate the definition and submit its authorized instantiation through the core.
  - [ ] Provide local DAR upload and the validated participant/package-source retrieval path to the builder.
  - [ ] Show useful diagnostics for unsupported mappings and workflow shapes.
  - [ ] Add Chapter 8's create, authorize, run, and inspect walkthrough.
  - [ ] **Acceptance:** a reader defines and executes a supported example without editing generated Daml; the result uses the same ledger rules and observable states as the scripted examples.

### Commit 17 — `test: harden workflow bounds and failure behavior`

- [ ] Close remaining execution-boundary gaps after the feature-level tests.
  - [ ] Define measured, documented limits for graph size, join prerequisites, atomic work, and artifact payloads.
  - [ ] Add targeted cases for stale contracts, competing advances, invalid continuation, missing disclosure, and exceeded bounds.
  - [ ] Verify observation/transport failures cannot masquerade as expected business rejection.
  - [ ] Exercise a fully generated reference project from a fresh build environment.
  - [ ] **Acceptance:** the supported capability matrix links each boundary to an executable check, and every failure leaves documented ledger effects and useful evidence.

### Commit 18 — `docs: complete the book and evaluation walkthroughs`

- [ ] Complete the reader's path through the working release.
  - [ ] Finish Chapter 9, glossary, setup troubleshooting, architecture decisions, and extension guidance.
  - [ ] Pair every chapter with matching stories, evidence, source links, and an experiment.
  - [ ] Review layout, keyboard access, legibility, mobile/narrow-screen behavior, and non-color indicators.
  - [ ] Prepare a developer walkthrough and runnable demo script using the actual release artifacts.
  - [ ] **Acceptance:** a fresh-reader walkthrough identifies no missing step needed to understand, run, inspect, and adapt the reference examples.

### Commit 19 — `build: package a reproducible software and book release`

- [ ] Assemble one versioned evaluation bundle.
  - [ ] Package the core, interfaces/bindings, references, builder, book, and required metadata.
  - [ ] Bundle recorded demo evidence with provenance and include commands for fresh local execution.
  - [ ] Verify a clean build, required golden suite, both reference workflows, and direct/generated participation paths.
  - [ ] Produce a release manifest, compatibility statement, changelog, and known limitations.
  - [ ] **Acceptance:** software, chapters, examples, and evidence identify the same source revision and reproduce the documented behavior.

### Commit 20 — `docs: record release acceptance and adopter handoff`

- [ ] Record the completed implementation and its evaluation evidence.
  - [ ] Attach the actual commit/release identifiers and evidence paths to completed checklist items.
  - [ ] Review the release checklist below and distinguish completed technical work from external acceptance obligations.
  - [ ] Prepare the public release description, demonstration outline, and evaluator feedback template.
  - [ ] Document maintenance boundaries and an actionable next-step guide for an external application team.
  - [ ] **Acceptance:** the handoff accurately states what is implemented, tested, published, and still externally pending; no adoption or publication is claimed without evidence.

### Conditions for every implementation commit

- [ ] Its stated behavior is implemented and the relevant existing behavior still works.
- [ ] New behavior has the appropriate golden or focused verification; checks do not merely mirror implementation details.
- [ ] Meaningful negative cases accompany authority, privacy, evidence, and atomicity claims.
- [ ] Ordinary validation leaves committed expectations untouched.
- [ ] Its story, chapter, or setup explanation matches the delivered version.
- [ ] Authored Markdown/YAML remains intuitive, concise, and free of unnecessary duplication; independent expectations remain explicit.
- [ ] Scala changes follow feature ownership, pure/effect boundaries, and explicit resource lifecycles described in the architecture note.
- [ ] The reader-visible result is demonstrated and evidence is retained.
- [ ] Changes are scoped and the commit message explains intentional behavior/baseline changes.

## 9. Release acceptance checklist

- [ ] **Working composition**
  - [ ] Both integration paths execute through the same core-managed model.
  - [ ] At least two independently packaged source applications and two complete reference workflows are available.
  - [ ] Role ownership and application boundaries remain explicit.
  - [ ] Staged progression, continuation, and bounded atomic completion are each demonstrated.
- [ ] **Executable specifications**
  - [ ] Every release story has a committed Markdown input and expected result.
  - [ ] A reader can explain the scenario from those files without decoding infrastructure metadata or following layers of inheritance.
  - [ ] The suite checks observed behavior and fails on meaningful divergence.
  - [ ] Golden changes are explicit and readable in Git.
  - [ ] Actual outputs, diffs, and provenance are retained on failure.
  - [ ] Normalization preserves meaningful identity, ordering, values, and permissions.
- [ ] **Authority and privacy**
  - [ ] Wrong actors cannot execute source actions through Harmonia.
  - [ ] Evidence is validated for its claimed purpose and cannot be reused where prohibited.
  - [ ] Separate-participant observations substantiate visibility claims.
  - [ ] Missing observation data cannot produce a passing privacy result.
  - [ ] A failed atomic block leaves no partial application or workflow effects.
- [ ] **Readable, interactive delivery**
  - [ ] Each chapter includes a story, visual explanation, relevant code, and a reproducible experiment.
  - [ ] The reader can inspect expected and actual results and see their differences.
  - [ ] Recorded demonstrations work without a ledger; fresh execution has documented setup.
  - [ ] Live identity and authorization are enforced independently of the display.
  - [ ] The composer stays within the documented workflow and integration vocabulary.
- [ ] **Reproducible release**
  - [ ] Versions, packages, source provenance, commands, and environment requirements are documented.
  - [ ] A clean environment reproduces both reference workflows and generated integration.
  - [ ] All included local links and demo assets resolve.
  - [ ] Public-source and licensing requirements are satisfied before claiming a public release.
  - [ ] Known limits and deployment topology are explicit.

## 10. Open decisions and evidence gates

These are design questions to resolve at the indicated point. They do not require repeated user confirmation for routine implementation choices. Material changes to agreed product scope should be discussed before adopting them.

| Question | Working assumption | Resolution point |
| --- | --- | --- |
| Who reads the opening chapters? | Business-process-aware technical evaluator, with deeper developer chapters | Before finalizing the first interactive edition, Commit 06 |
| Which SDK/runtime versions? | A pinned compatible set supporting the proven integration paths | Commit 02; re-check during Commit 05 |
| Which Scala and browser libraries? | Scala 3, Cats Effect `IO`, JVM services/tools, and Scala.js browser code; exact versions and UI library remain open | JVM baseline in Commit 02; browser compatibility in Commit 06 |
| Where do interfaces live? | Small shared interface package with acyclic imports | Compile and execute both paths; decision record in Commit 05 |
| Can unchanged DARs participate as proposed? | Typed adapter contracts exercise eligible existing choices | Positive and negative ledger evidence in Commit 05; revise the design if disproven |
| How is private progress represented? | Minimal shared state plus domain-owned contracts and disclosed results | Separate-participant visibility evidence in Commit 07 |
| What exactly is a join? | Explicit prerequisites and branch-aware completion; no implied generic BPMN semantics | Definition validation and stories in Commit 09 |
| Which package source is usable? | Local inputs plus one real, documented source; Catalyx is illustrative in the proposal | Retrieval/access experiment in Commit 10 |
| How much can the composer configure without rebuilding? | Structure, roles, and arguments over already supported compiled actions | Prove the boundary before Commit 16; new adapters may require a build |
| How much work fits one atomic block? | An explicit limit established on the supported runtime | Initial bound in Commit 12, measured/documented in Commit 17 |
| Where is the public repository and what license applies? | Public development is a proposal requirement; destination and license are not yet selected | Prepare during Commit 01; resolve before publication or claiming lifecycle compliance |

## 11. Relationship to the proposal

The imported proposal describes six consecutive engineering milestones, followed by separately evaluated adoption milestones. This commit sequence is an implementation order, not a new contractual calendar or a replacement for committee acceptance criteria. Early experiments deliberately investigate later dependencies before committing to a full implementation.

| Proposal milestone | Main evidence supplied by this plan |
| --- | --- |
| M1: design, prototype, dapp baseline | Commits 01–05 and the documented visual baseline leading into Commit 06 |
| M2: first workflow and core hardening | Commits 04 and 06–10, including progression, branches/joins, and initial package sourcing |
| M3: two-app direct composition | Commit 11, with UI/book support continuing through Commits 15–16 |
| M4: generated integration | Commits 13–14, building on the early unchanged-DAR experiment |
| M5: hardening and extension boundaries | Commit 17 and the completed integration/viewer/composer boundaries |
| M6: complete technical release | Commits 18–20 plus all release acceptance checks |

If a funded delivery calendar applies, reconcile this order and UI milestones with that calendar before execution; this PRD does not silently amend it.

### External obligations tracked separately

- [ ] Establish the public development location and record actual publication history.
- [ ] Obtain documented feedback, an integration-target conversation, or equivalent evidence from at least one external Canton/Daml evaluator team.
- [ ] Deliver the required developer walkthrough and coordinate the public demo/workshop and announcement when authorized.
- [ ] Track M7's two qualified external teams using Harmonia and the required confirmations in its acceptance window.
- [ ] Track M8's additional qualified adoption and confirmations under the original proposal's conditions.

External contacts and announcements require explicit authorization before messages are sent. These obligations cannot be satisfied by a local test or a checked implementation box. Funding amounts, payment conditions, and adoption schedules remain in the [original proposal](harmonia.md).

### Technical references for the early design work

The documentation below informed the initial discussion. They are versioned Digital Asset references; behavior must be checked against the versions actually selected for implementation.

- [Daml interfaces](https://archived.docs.digitalasset.com/build/3.5/tutorials/smart-contracts/interfaces.html): interface implementation and separation of interface and application packages.
- [Parties and authority](https://archived.docs.digitalasset.com/build/3.5/tutorials/smart-contracts/parties.html): authorization context and the limits of authority propagation.
- [Compose choices](https://archived.docs.digitalasset.com/build/3.4/tutorials/smart-contracts/compose.html): transaction composition, atomicity, and disclosure of consequences.

**Current implementation step:** see the [progress record](docs/progress.md). Completed checkboxes require the corresponding implementation and evidence; publication and external adoption remain separately tracked.
