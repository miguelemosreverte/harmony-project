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
Unannotated files are visibly described by their owning directory; generated metadata
must never invent a line-by-line explanation of code it has not documented.

## Linear implementation

- [ ] 1. Repair the existing scenes.
  - [ ] Add measured connectors and cumulative handoff colors.
  - [ ] Check Sofia clearance, Proposal → Ben, portrait spacing, and narrow layouts.
- [ ] 2. Introduce the reusable typed diagram component.
  - [ ] Support ordered steps, branches, joins, node selection, and explicit observed states.
  - [ ] Apply it to composition and package journeys as well as book explanations.
- [ ] 3. Generate a source and slice catalog.
  - [ ] Add meaningful source annotations and curated slice narratives.
  - [ ] Export exact source, syntax colors, hashes, line counts, and validated relationships.
  - [ ] Fail generation when a referenced source or annotation is inconsistent.
- [ ] 4. Build the developer and reviewer views.
  - [ ] Tree → code/annotations → slice diagram, with line and file URLs.
  - [ ] Manifest → Daml interfaces/choices → Scala service → browser, with review questions.
- [ ] 5. Build the original-author comparison.
  - [ ] Full original passages, selected highlight, citations, and source location.
  - [ ] Reuse the diagram, workflow, code, and chapter views in the companion pane.
- [ ] 6. Revise the book entry and chapters.
  - [ ] Route readers by their actual questions; add visual explanations to the chapters.
  - [ ] Explain Daml/Canton mechanisms and distinguish demonstrated behavior from gaps.
  - [ ] Provide deterministic presentation URLs and a controllable slideshow for recording.
- [ ] 7. Verify and deliver.
  - [ ] Check graph/reference integrity, source export drift, and preserved golden expectations.
  - [ ] Exercise reader routes, files, citations, diagram selection, sharing, and live controls.
  - [ ] Inspect desktop/mobile screenshots, arrow bounds/colors, and PDF output.
  - [ ] Commit the verified work, document remaining product gaps, and leave one usable entry.
