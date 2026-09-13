# Illustrated workflow review

Historical reader.7 review. The user rejected the repeated portrait composition.
The corrective art direction is [Handcrafted scenes](HANDCRAFTED-SCENES.md).
The checks below establish rendering behavior, not acceptance of that art direction.

The approved infographic remains the main stage. A separate two-speaker scene
below it explains the selected moment, with one short HTML sentence per speaker.
There are no new controls. The old actor badges have been removed.

## What is covered

- All 32 recorded examples and all 130 observations.
- Purchase and transfer introductions, successful paths, and refused paths.
- Live financing, composition questions, consent, execution, and package integration.
- Chapter explanations, source slices, reviewer relationships, and original-document companions.
- The standalone recording export and printed chapters.

The six illustrations were generated with the built-in image generator, using the
approved desktop study as a reference. They live in
`product/scene/site/assets/support-v1/`. Exact prompts and asset hashes are in
`design/0.2/infographic/support-prompts.md` and `support-assets.json`.

## Evidence

| Check | Result |
| --- | --- |
| Scala scene and recording tests | 16 pass; expected values cannot alter the narration |
| Original documents and source catalog | 28 pass; original quotations and golden files preserved |
| Recording support | 3,389 checks; 130 observations at 1280, 390, and 320 CSS pixels |
| Featured carousel | 408 checks, 30 captured frames, no empty animation frame |
| Compact navigation | 75 checks, including branch URLs and keyboard navigation |
| Live inspection | 65 checks; inspection submits no command |
| Served delivery | 393 source fingerprints verified; final live selection and book image checks pass |
| Standalone export | 14 checks; illustrated print layout inspected |
| Live execution walkthrough | Financing approval and continuation, partner consent, both assigned actions, package compilation |

`workflow-support.json` contains each inspected moment and its two lines.
`workflow-support-live.json` records the real sandbox walkthrough. The live run
created the uniquely named “Illustrated agreement” plan; it did not reset the
ledger. Capabilities are not included in the reports or screenshots.

Screenshots are in `design/0.2/support-review/`: 92 recorded and authored views,
plus 18 views from the real live walkthrough. All recorded and authored captures
were visually inspected in desktop and mobile groups. Each workflow has desktop and
mobile captures. Long pages are captured after naturally scrolling the support
scene into view, so its conversation can be checked above the fixed navigation.
The gallery links to reproducible recorded URLs. Live screenshots preserve the
state at capture time; a live URL always reads current participant-visible state.

## Review decisions

- Keep the same portrait positions and speech-bubble footprint across steps.
- Use HTML dialogue so mobile wrapping, selection, and accessibility remain native.
- Give the two custodians the transfer dialogue; do not introduce a different
  visual identity for the seller in the unchanged infographic.
- Distinguish an application decline from an unauthorized attempt, a duplicate
  request from another execution, and final-transaction rollback from the earlier lock.
- Separate “ready to compile,” “compiled,” and “available live.”
- On a completed live plan, default to its last completed action; explicit URL
  inspection still takes precedence.
- Keep the diagrams, quotations, source text, and golden comparisons independently
  reviewable. Authored conversation is explanation, never an original quotation.

The shared `harmonia.scene.support` package contains only presentation values and
the retained renderer. Recorded explanations belong to the book; live explanations
belong to their product slices. No book dependency enters the production service.

Measured infographic landmark movement peaked at 5.88 CSS pixels; the support
portraits moved at most 7.79 pixels across recorded steps. Navigation targets stayed
fixed. These are geometry measurements; changed-image area is reported separately
in `carousel-pixels.json`.
