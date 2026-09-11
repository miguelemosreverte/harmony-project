# Harmonia 0.2 — from proposal to an understandable product

Status: design and source traceability checkpoint complete. Runtime work remains planned. Base: fourth draft, `bef8ac5`.
This plan changes the product evaluation surface and makes the remaining product
work explicit. It does not treat historical test results as fresh 0.2 results.

## Principles

1. Begin with a person, an application they own, and the next action they need.
2. Keep execution and source authority on the ledger. Use typed Scala with Cats
   Effect `IO`/`Resource` for clients and services; keep pure decisions separate.
3. Preserve production, book, and harness ownership. The product must compile
   without any quotation, documentation, recording, or design tooling.
4. Prefer explicit small types and functions. Compiler structure defines business
   meaning; text extraction is confined to document quotation tooling.
5. Keep input and independent expected output in readable Markdown. New product
   behavior receives new golden stories; existing expectations remain the baseline.
6. Quote the original documents exactly and link back to their location. Distinguish
   the proposal's wording, our interpretation, current evidence, and future work.
7. Count unique source coverage. Repeated quotations do not increase the score.
   Documentation coverage never counts as a working feature or external adoption.
8. Design the complete experience before wiring new runtime behavior. Every
   proposed principal screen has an image and adjacent executable HTML mockup.
9. Make the book welcoming to stakeholders and useful to developers. Start with
   purpose and a concrete story; reveal contracts, inputs, and observations on demand.
10. Use one browser for review and no ledger for design work. Run future Scala and
    ledger checks sequentially within the existing memory limits.

## What this execution delivers

A preserved and numbered Git history; a product scope decision record; generated
UI concepts and adjacent HTML; an interactive chapter prototype; exact quotations
from both originals; a reproducible coverage report; and a file-level runtime plan.
The mockups use explicitly labelled sample state. Live runtime implementation is
the subsequent phase described below, matching the requested design-first sequence.

## Linear commit plan

- [x] 01 — Establish the 0.2 product baseline.
  - [x] Preserve five drafts as numbered archive branches and annotated tags.
  - [x] Fast-forward `main` to draft four and branch `version/0.2.0` from it.
  - [x] Record the branch mapping in `docs/versions.md`; set `build.sbt` to
    `0.2.0-SNAPSHOT`; introduce this plan and the product contract.
  - [x] Update the root and book entry points to identify the new design work.
- [x] 02 — Design the book and application before runtime work.
  - [x] `design/0.2/book-overview.png` and `.html`: audience, journey, chapter entry.
  - [x] `design/0.2/book-chapter.png` and `.html`: quoted claim, interactive story,
    participant view, input/expectation/observation, and source inspection.
  - [x] `design/0.2/application.png` and `.html`: workspace, current actor, source
    owners, enabled next action, timeline, and package/binding entry point.
  - [x] `design/0.2/application-builder.{png,html}`: the integration journey.
  - [x] `design/0.2/coverage.{png,html}`: the source coverage and evidence distinction.
  - [x] Keep the exact image prompts and original generated files alongside the
    implementation. Capture HTML screenshots and document fidelity limitations.
  - [x] Share a small local style sheet and simple mock interaction code; avoid
    introducing a production design framework or a new dependency tree.
- [x] 03 — Prove document traceability and prototype behavior.
  - [x] `book/edition-0.2/coverage-map.md`: readable source ranges assigned once to
    use-case chapters or clearly named context appendices.
  - [x] `book/edition-0.2/chapters/`: original narrative, user questions, evidence
    links, interpretation, and explicit limitations for each destination.
  - [x] `book/edition-0.2/build.py`: fingerprinted document extraction,
    exact quote rendering, source links, and deterministic coverage output.
  - [x] `book/edition-0.2/check.py`: stale source, missing assignment, duplicate
    range, altered quote, incomplete HTML extraction, and missing destination checks.
  - [x] `design/0.2/coverage.html`: visible per-document quotation totals and
    separately listed product claims with their evidence status.
  - [x] Review all screens at desktop and narrow widths; exercise controls and
    source links; store results under `docs/0.2/verification.md`.
  - [x] Commit only after this design/coverage slice demonstrably works. Do not
    mark any of the runtime commits below complete from prototype evidence.

The completed second UX pass is governed by [the UX contract](UX.md), with
[its own verification](verification-2.md). It adds readable
source rendering, reader routes, deterministic URLs, and four preserved recordings
to the static design edition. Step 07 below still refers to integration into the
Scala book and portable runtime export; these HTML improvements do not complete it.

First-pass evidence: [verification](verification.md), [browser results](browser-results.json), and
[concept/render comparison](../../design/0.2/review.html).

## Following runtime commits

These remain planned. Their scope must follow the product decisions, not expand
silently into arbitrary dynamic composition or a general workflow studio.

- [ ] 04 — Define reliance on workflow evidence.
  - [ ] `docs/architecture/004-persisted-processes.md`: specify publisher/owner
    trust, what another party may infer, and how the publisher is chosen.
  - [ ] `product/ledger/core/daml/Harmonia/Process/Engine.daml`: change only if
    the agreed evidence contract requires stronger provenance or attestation.
  - [ ] `examples/stories/`: add a malicious-publisher scenario and an independently
    owned source refusal; expected results must state the agreed trust boundary.
  - [ ] `book/edition-0.2/chapters/02-roles-and-trust.md`: show both results.
- [ ] 05 — Prove another application's integration journey.
  - [ ] `product/packages/inputs.md`: pin a suitable independently developed DAR
    and document provenance, license, and the actual retrieval mechanism.
  - [ ] `product/packages/mappings/`: author one minimal applicability mapping;
    add an independent input and expected result under `examples/`.
  - [ ] `product/server/.../packages/resolve/`: introduce a Package Manager source
    only when its concrete contract is established. Keep source identity typed.
  - [ ] `product/server/.../bindings/{inspect,generate}/`: support only the needed
    eligible signature; reject unsupported signatures with useful diagnostics.
  - [ ] Keep `product/ledger/core` unchanged for this integration. Record all
    adapter, catalog, build, and application-specific work needed by the adopter.
  - [ ] `harness/`: compile the exported project outside this checkout and exercise
    real application choices with correct and incorrect authority.
- [ ] 06 — Implement the application journey using real state.
  - [ ] `product/api/.../workspace/WorkspaceSnapshot.scala`: expose the reviewed
    workflow view with explicit actor, provenance, available action, and status.
  - [ ] `product/server/.../app/workspace/Workspace.scala` and domain owners:
    project ledger observations; keep UI eligibility separate from authorization.
  - [ ] `product/web/.../live/LiveView.scala`: implement the workspace shell.
  - [ ] `product/web/.../composition/{ComposerView,CompositionEditor}.scala`:
    implement bounded composition and state-dependent controls from the mockup.
  - [ ] `product/web/.../packages/PackagePanel.scala`: implement the upload,
    inspect, map, compile, and export states, with clear installation boundaries.
  - [ ] Add live views for supported branches/joins and the four-party reference;
    retain explicit actor sessions rather than presenting a role picker as security.
  - [ ] Verify denied, pending, failed, disconnected, stale, and successful states.
- [ ] 07 — Integrate the designed book with actual recordings.
  - [ ] `book/model/.../BookChapter.scala`: add typed source citations and evidence
    references, keeping document coverage distinct from observed story outcomes.
  - [ ] `book/export/.../ExportBook.scala`: package chapter text, quotations, pinned
    source files, recordings, and the coverage report for offline reading.
  - [ ] `book/browser/.../{BookNavigation,BookView}.scala`: implement the designed
    navigation and reading hierarchy using semantic, keyboard-accessible elements.
  - [ ] `book/browser/.../reader/StoryLaboratory.scala`: drive the visual timeline
    from recorded observations; show independent goldens and honest differences.
  - [ ] `book/site/book.css`: translate the reviewed visual tokens without coupling
    the book implementation to `product/web`.
  - [ ] Replace each prototype simulation with an actual recording or an explicit
    pending state. Preserve source citations and coverage in the exported book.
- [ ] 08 — Validate and prepare 0.2 for release.
  - [ ] Run focused tests followed by the existing sequential `scripts/check`.
  - [ ] Preserve original goldens; review new expectations and intentional contract
    changes independently of generated actual outputs.
  - [ ] Validate relocated/offline book and product delivery at a named revision.
  - [ ] Record actual external evaluator feedback when it exists; do not populate
    the evaluator template with internal work or contact anyone without authorization.
  - [ ] Reconcile M1–M6, license, public destination, and publication requirements.
    Keep M7/M8 dependent on qualified external use, not internal test counts.
  - [ ] Update changelog, version, acceptance evidence, and only then tag `v0.2.0`.

## Review rules

All new production behavior must be traceable to a source requirement, an explicit
design decision, a scenario, an independent expectation, and a book demonstration.
References to `...` above identify an existing package prefix, not a request for
new flat directories. Exact implementation owners are resolved when each bounded
runtime commit begins; this design phase does not promise a preselected patch for
an unresolved trust or external-provider decision.

The original Markdown and HTML remain byte-identical. Commercial terms and
historical research claims are quoted as proposal context, not current verified
facts or runtime requirements. Missing original diagram assets remain identified.
