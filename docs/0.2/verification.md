# 0.2 design and quotation verification

Verified 2026-09-11. Implementation commit: `562849d`, based on fourth draft
`bef8ac5`. The following verification commit records these results and marks only
the design-stage plan complete. It does not change runtime behavior.

## What works in this checkpoint

| Artifact | Result |
| --- | --- |
| Original proposal quotations | 386 / 386 nonblank source lines; 7,138 / 7,138 source words |
| Original architecture quotations | 350 / 350 visible body text nodes; 972 / 972 source words |
| Total quotation inclusion | 736 / 736 units; 8,110 / 8,110 words; 100% of the declared textual denominator |
| Reading destinations | Eight product chapters and two context appendices |
| Principal screen concepts | Five original generated PNGs, five adjacent HTML pages |
| Visual evidence | Five desktop HTML captures at 1536 × 1024; two mobile captures at 390 × 844 |
| Automated document checks | 12 passing tests, including deliberate corruptions |
| Desktop browser suites | Six passing suites covering overview, chapter, workflow progression, refusal, builder, and browser quotation audit |
| Narrow viewport | 17 reader/application/source pages passed menu and overflow checks at 390 × 844 |
| Comparison page | Screen selection and narrow layout passed separately |

The Python checks establish that each rendered source unit appears exactly once
and retains its original text. Deliberate omissions, duplication, altered quotes,
source drift, a missing chapter, and an inflated word count are rejected. Generated
pages must equal a fresh deterministic build. Local links and source anchors resolve.

The browser independently parses the original architecture using `DOMParser`,
walks the actual body text nodes, and compares all 350 resulting strings with the
chapter quotations. Its separate Unicode word count is 972, matching the report.
This provides an independent check of the Python HTML extraction boundary.

## Interaction evidence

The chapter advances and resets, stops at completion, changes participant
explanations, and supports tab navigation with arrow keys. The workspace disables
actions for the wrong illustrated actor and for pending, refused, stale, or
disconnected state. Approval hands work from bank to buyer to seller; rejection
does not create an offer. History follows the simulated actions.

The builder rejects the unsupported sample shape and exposes the inspection,
mapping, and build/export boundaries. Its messages explicitly state that no actual
archive inspection or compilation is performed by the prototype.

Machine-readable results are in [browser-results.json](browser-results.json).
The exact reviewed file fingerprints are in [verified-files.json](verified-files.json).
Image origin, prompt, and rendered-file fingerprints are in
[provenance.json](../../design/0.2/provenance.json).

## Visual assessment

The five HTML screens were compared with their generated concepts. Their layout,
navigation, primary controls, diagrams, palette, and reading hierarchy follow those
concepts. Native font rendering, control rendering, some wrapping, and small
spacing differences remain. They are not asserted to be pixel-identical.
The [comparison page](../../design/0.2/review.html) exposes both images side by side.
Specific intentional corrections are recorded in [the design document](ui-design.md).

## Git, scope, and resource checks

- `main` equals fourth draft `bef8ac5`.
- All five archive branches resolve to the same commits as their corresponding
  annotated `v0.1.N-draft` tags. The fifth draft remains preserved separately.
- `product/`, `examples/`, `harness/`, and `docs/proposal/` match `main` exactly.
  The build version changes to `0.2.0-SNAPSHOT`; no existing golden is rewritten.
- The preview binds to `127.0.0.1:56202` and starts no JVM or ledger. Its observed
  Python process used approximately 27 MiB RSS. The pre-existing book JVM remains.
- Scala/ledger suites were not rerun for this documentation and prototype change.
  Historical runtime acceptance is not relabelled as fresh 0.2 evidence.

## What the score does not establish

100% textual quotation inclusion is not 100% explanation quality, product feature
coverage, or milestone acceptance. HTML styling/markup, missing original diagram
attachments, and the contents of linked external works are outside the denominator.
The 12 product claims retain their separately authored baseline assessments.

New runtime capabilities, the finished live UI, external evaluation, publication,
and adoption remain in the later unchecked portion of [the plan](PLAN.md).
`v0.2.0-design.1` identifies this design checkpoint; it is not a stable product release.
