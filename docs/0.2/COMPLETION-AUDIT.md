# Reader.2 acceptance against the complete request

Historical checkpoint. The current UX supersedes its controls and mounted book
routes; see [the current walkthrough](WALKTHROUGH.md) and [UX contract](UX.md).

The approved infographic is now shared by the featured stories, the complete recording
laboratory, live financing, live composition, package stages, and the book's diagrams.
This audit covers the complete reader request, rather than only the four featured stories.

## Completed implementation

- [x] Repair and reuse the approved visual language.
  - [x] Preserve Sofia's arrow clearance and the blue Proposal → Ben handoff.
  - [x] Render ordered actions, branches, joins, skipped work, and refusals with shared components.
  - [x] Route connectors around intermediate cards in both horizontal and vertical layouts.
- [x] Include every registered recording in the standard book.
  - [x] Preserve all 32 examples, 11 example kinds, and 130 observed moments.
  - [x] Keep expectations independent of the state drawn by the diagram.
  - [x] Bundle original evidence for offline inspection; retain dates, revisions, and hashes.
  - [x] Restore story, step, node, evidence file, appearance, navigation, and presentation from URLs.
- [x] Give each reader a direct, useful entrance.
  - [x] Developer: actual source tree, colored code, line links, annotations, and vertical slices.
  - [x] Reviewer: distinct reading maps, parsed manifest dependencies, and reviewed execution handoffs.
  - [x] Original author: original passages and highlights beside reused workflows, code, and diagrams.
  - [x] Technical investor or user: narrated workflows and explicit Daml/Canton mechanisms and limits.
- [x] Verify the delivered experience.
  - [x] Exercise desktop, narrow layouts, keyboard/touch navigation, shared links, and offline evidence.
  - [x] Execute live financing, consented composition, and package compilation through the actual UI.
  - [x] Inspect screenshots and the printed diagram; record a 24-second HTML presentation.
  - [x] Verify the final mounted source and leave one fresh sandbox with no submitted jobs.
  - [x] Commit and push the working checkpoints to the private GitHub repository.

## Evidence

| Check | Result | Evidence |
| --- | --- | --- |
| Scala suites | 63 passed | Scene, reader, service, runner, and book exporter suites |
| Source and quotation checks | 27 passed | `book/edition-0.2/check.py` |
| Original quotations | 736/736 units, unchanged | `design/0.2/coverage.json` |
| Complete recording library | 792 browser checks | [Recording report](laboratory-browser.json) |
| Existing reader experiences | 119 browser checks | [Reader report](reader-browser.json) |
| Relationships and package context | 133 browser checks | [Relationship report](relationships-browser.json) |
| Real commands and live UI | 40 browser checks | [Live report](reader-live.json) |
| Final mounted delivery | 12 read-only checks; 380 exact source files | [Delivery report](reader-delivery.json) |
| Printable diagrams | Inspected A4 output with measured connectors | [Manifest review PDF](../../design/0.2/reader/review/manifest-review.pdf) |
| Offline export | All source/document, chapter, recording, and browser-asset links passed | `scripts/harmonia book-links` |

The source catalog has 24 file-specific annotations and 29 documented groups. Group
explanations are explicitly shared context. Five dependency views come from 14 current
production Daml manifests. Three execution maps are authored relationships tied to
reviewed source fingerprints. Six reading maps explain a useful order through the code.
These three kinds of diagram are labeled separately.

The 32 historical recordings come from the preserved fourth-draft export with SHA-256
`887ed02645f2287d847f96165dc9309611f98cae969e535a355f2aba3267dc82`.
All input and expectation hashes still match the committed golden files; the original
four featured recordings remain unchanged. Historical recordings are not presented as
fresh submissions. The live report records actual new commands separately, and the
final delivery report verifies a fresh sandbox without submitting commands.

## Product scope

The live financing handoff runs from bank to buyer. The complete property offer and
custody transfer are recorded examples. The live composer supports bounded sequential
plans; recorded examples also demonstrate branches and joins. A generated package,
a compiled DAR, and typed registration remain distinct stages. Quotation coverage
measures inclusion of original text, separately from implementation and adoption.
This is a reader checkpoint on `version/0.2.0`, not a stable release or a promotion to `main`.
