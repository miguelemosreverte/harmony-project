# One visual language, four ways to understand Harmonia

The approved infographic is the interaction standard for the product and book.
The reader should recognize people, applications, artifacts, and the next handoff
before seeing implementation detail. A readable screen is more valuable than a
screen containing every available fact.

## Screen contracts

| Reader | Primary question | Primary view | Secondary evidence | Intended result |
| --- | --- | --- | --- | --- |
| Developer | What code exists, and how do its vertical slices work? | Expandable source tree beside highlighted, line-addressable code | File annotations and a diagram of the selected slice | Follow an operation without searching the repository |
| Reviewer | Does this implementation agree with the proposed architecture? | An end-to-end diagram from package manifest through contracts, service, and browser | Actual paths, exact source citations, recorded checks, and explicit gaps | Identify a mismatch without mistaking a drawing for proof |
| Original author | Where did each original requirement go? | Read the original document on the left with a selected passage | Reused workflow, diagram, code, or explanation on the right | Trace a passage to evidence or an explicitly unimplemented requirement |
| Technical investor or user | What does this provide, and what has actually been demonstrated? | Narrated workflow scenes, suitable for a presentation | Daml choices, authorization, DARs, Canton participants, privacy, and transaction boundaries | Explain the mechanism and distinguish live, recorded, and planned capability |

These are four entry points into the same material. They are not four independently
maintained accounts of the product. Keep author quotation coverage separate from
implementation and external adoption. A new annotation does not prove a feature.

## Shared visual rules

- One scene and one main conclusion at a time. Details belong to a named secondary view.
- Measure connectors between rendered node boundaries; reserve visible arrow clearance.
  Completed handoffs remain blue. Refused and unavailable transitions stay distinguishable.
- Use native HTML/SVG, the approved illustration assets, neutral panels, and blue emphasis.
  Reflow diagrams vertically when a horizontal arrow no longer has enough room.
- Use the same typed, reusable diagram renderer for book explanations, workflow plans,
  package stages, and live composition. Domain owners supply observed state.
- Use plain captions: who acts, what changes, who can rely on the result. State whether
  a scene is live, recorded, or explanatory. A diagram cannot create ledger authority.
- Every durable selection belongs in the URL: reader, file and line, diagram and node,
  source passage and companion view, scenario and scene, presentation mode.
- Keyboard, touch, Back/Forward, direct links, offline reading, and printing remain useful.
  Auto-advance is explicitly started, stops for reduced motion, and never submits a command.

## Entering the live workspace

A shared task URL must be usable in a fresh tab. Without a participant session,
show the entry screen: explain Bank, Buyer and Reviewer, accept a private link
from the local launcher, and offer the recorded handoff and book. Do not poll an
unauthenticated API or present Reconnect as a remedy for missing access. A 401
stops polling and asks for a current link, preserving any unconfirmed request.
Entering through the form keeps the selected task and appearance in the URL;
the capability is consumed into tab-local storage and removed from the address.

The [session-entry browser checks](session-entry-browser.json) exercise fresh tabs,
expired links, the three participant identities, book navigation and narrow layouts
against the running server without submitting ledger commands.

## Source documentation contract

The source browser is generated from an explicit allowlist of repository text files.
It excludes build outputs, credentials, private runtime state, and generated copies.
Each entry retains its original text, SHA-256, line count, and owner. Syntax highlighting
is presentation only. It is never used to infer a Scala type, a Daml choice, or a dependency.

A small custom documentation block on important source files supplies `@book` fields:
`slice`, `role`, and `summary`. The rest of the file remains ordinary ScalaDoc/Daml
comments. Slice documentation supplies the narrative, explicit file references,
relationships, source citations, and evidence links. Missing referenced files, duplicate
annotations, unknown slice names, stale exports, and invalid graph references fail the build.
Unannotated files receive explicitly labeled context from their documented group; generated metadata
must never invent a line-by-line explanation of code it has not documented.

## Linear implementation

- [x] 1. Repair the existing scenes.
  - [x] Add measured connectors and cumulative handoff colors.
  - [x] Check Sofia clearance, Proposal → Ben, portrait spacing, and narrow layouts.
- [x] 2. Introduce the reusable typed diagram component.
  - [x] Support ordered steps, branches, joins, node selection, and explicit observed states.
  - [x] Apply it to composition and package journeys as well as book explanations.
- [x] 3. Generate a source and slice catalog.
  - [x] Add meaningful source annotations and curated slice narratives.
  - [x] Export exact source, syntax colors, hashes, line counts, and validated relationships.
  - [x] Fail generation when a referenced source or annotation is inconsistent.
- [x] 4. Build the developer and reviewer views.
  - [x] Tree → code/annotations → slice diagram, with line and file URLs.
  - [x] Manifest → Daml interfaces/choices → Scala service → browser, with review questions.
- [x] 5. Build the original-author comparison.
  - [x] Full original passages, selected highlight, citations, and source location.
  - [x] Reuse the diagram, workflow, code, and chapter views in the companion pane.
- [x] 6. Revise the book entry and chapters.
  - [x] Route readers by their actual questions; add visual explanations to the chapters.
  - [x] Explain Daml/Canton mechanisms and distinguish demonstrated behavior from gaps.
  - [x] Provide deterministic presentation URLs and a controllable slideshow for recording.
- [x] 7. Verify and deliver.
  - [x] Check graph/reference integrity, source export drift, and preserved golden expectations.
  - [x] Exercise reader routes, files, citations, diagram selection, sharing, and live controls.
  - [x] Inspect desktop/mobile screenshots, arrow bounds/colors, and PDF output.
  - [x] Commit the verified work, document remaining product gaps, and leave one usable entry.


## Ownership and review entry points

- `product/scene/` owns the typed scene and diagram renderers, measured arrows, and shared appearance.
- `product/web/` projects observed financing, composition, and package state into those views.
- `book/edition-0.2/atlas/` owns source extraction, documentation validation, six authored reading maps, parsed Daml dependencies, and reviewed execution handoffs.
- `design/0.2/reader/` owns reader navigation and layout. `files/` and `catalog.js` are generated.
- `design/0.2/checks/readers.mjs` verifies navigation and layout against a real browser.
- `book/edition-0.2/presentation.mjs` records the HTML purchase presentation as a four-scene MP4, using an owned local browser and two encoder threads.

Build with `scripts/build-design`; it checks exact quotations and generated source freshness.
Scala changes also require `scripts/build`. The source browser exports the checkout's actual
files; important review paths carry explicit annotations. Files without an authored annotation receive a labeled package explanation; all source files belong to a documented group.

For a presentation, open `chapters/03-financing-to-offer.html?present=1&audience=investor`.
Arrow keys and touch advance it; Play opts into six-second advancement. For a video, run:

```sh
node book/edition-0.2/presentation.mjs http://127.0.0.1:CDP_PORT
```

The default preview URL is `http://127.0.0.1:56202/design/0.2/`; a relocated chapter URL
and output directory can be supplied as the second and third arguments. Output goes to
`.artifacts/presentation/`. The video records the HTML presentation and submits no ledger commands.


## Verification

The implementation was checked with the pinned Daml build and ledger tests, Scala
formatting and 63 Scala tests, 27 source/quotation checks, desktop/mobile browser
checks, and authenticated live browser journeys. The live run included buyer consent,
execution of both assigned composition actions, and real adapter compilation. Every
exported source file was downloaded from the real server and checked against its catalog hash.

Evidence: [reader navigation and layouts](reader-browser.json),
[live commands and export checks](reader-live.json),
[desktop and mobile captures](../../design/0.2/reader/review/), and
[printable review diagram](../../design/0.2/reader/review/reviewer.pdf).

The reader does not increase the product's demonstrated scope: the live financing
handoff is bank-to-buyer, the full property offer and custody transfer are recorded,
and the composer is a bounded sequential evaluator. Source annotations explain selected
review paths; unannotated files receive shared package context without an invented line-by-line explanation.

## Complete recordings and durable navigation

The workflow library includes all 32 registered examples and 130 recorded moments.
The complete laboratory uses the same scene and diagram components, with direct story,
step, node, evidence, appearance, and presentation URLs. A public evidence artifact is
selected from the typed `RecordedArtifact` inventory; arbitrary paths cannot become
reader artifacts. Closing the evidence dialog restores focus to its source link.

Five manifest views are generated from the 14 production Daml manifests. Execution
handoffs for financing, composition, and packages are reviewed relationships whose
source fingerprints must be refreshed after an implementation change. Both are
labeled separately from the six suggested reading maps. The source browser includes
29 documented groups, with per-file annotations where a more specific explanation
has been authored.

Further evidence: [all recording surfaces](laboratory-browser.json),
[manifest diagrams and package context](relationships-browser.json), and
[the completion audit](COMPLETION-AUDIT.md).


The delivered `reader.2` book is verified in [the delivery report](reader-delivery.json).
It includes 380 catalogued source files. A fixed A4 content width keeps the measured
connectors aligned when the browser prints to PDF; the printed diagram was inspected
in addition to checking that a PDF file was produced.
