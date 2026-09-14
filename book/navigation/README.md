# The content sheet

[plan.json](plan.json) defines four independent reading regions on one unfolded
sheet. [index.html](index.html) is generated from that plan. Drag or scroll to pan;
pinch or Ctrl + scroll to zoom. Arrow keys pan, `+` / `-` zoom and `Home` fits the
sheet. The address preserves camera position, selected production file and line.

The file tree is the one content-selection surface. Passive headings, labels and
citations use ordinary dark text, without link styling. There are no chapter
links or selectors on the sheet.

- **User:** the previously approved illustrated journey, with its content intact.
- **Investor:** three Canton demo storyboards. Start with Harmonia's ecosystem,
  explain direct and generated participation, then demonstrate bounded settlement.
  The infographics use the Canton and Daml marks with their original proportions.
  Concrete results below them come from preserved local Canton recordings.
- **Architecture:** two reviewed illustrations based on the supplied document:
  where the workflow lives, then how a step reaches an application.
- **Implementation:** the existing `design/0.2/code.html` explorer, embedded without
  its surrounding navigation. Its production tree, syntax colors, source comments
  and vertical-slice renderer are reused. No replacement excerpt viewer is built.

This is a content proposal. It runs no ledger commands. A proposed model, a
current implementation choice and a recorded result are different kinds of
information, labeled where they appear. The approved workflow UI is unchanged.

## Illustrated architecture

The source is [harmonia-architecture.html](../../docs/proposal/harmonia-architecture.html):

1. **Component map:** off-ledger / on-ledger split, build and runtime paths,
   component responsibilities and application participation.
2. **Contract model:** definitions, instances, assignments, bindings,
   continuations, interfaces and application templates.

The HTML presents the exact images reviewed in
[architecture-v2](explorations/architecture-v2/README.md). Their existing headlines
and captions tell the story; the page does not add duplicate headings or prose.
Each figure has an accessible description. Intrinsic dimensions reserve the full
image before loading, and asset fingerprints prevent stale versions being shown.
The same mounted sheet supports pan, zoom, shared camera URLs and printing.

These are PNG illustrations embedded in semantic HTML. Their internal labels are
not independently editable or reflowed on phones; readers pan and zoom the sheet.
The source's exhaustive SVG diagrams remain available in the original HTML, and
the previous translated presentation is preserved at commit `389d133`.

The source identifies the contract names as illustrative; the second image
retains that qualification. Full fields, special paths and open design questions
remain in the source reference. The images explain a selected view of the
proposal and do not assert implementation coverage.

The original input files remain under `docs/proposal/`. Investor infographic
definitions live separately in [figures.json](figures.json), rendered by
[figures.mjs](figures.mjs). Brand provenance is in
[assets/README.md](assets/README.md).

## Reused source explorer

The explorer keeps its existing lazy-loaded source exports and annotation
pipeline. At this revision, 122 production files (8,354 lines) are eligible.
Every exported production fingerprint was checked against the checkout.

`design/0.2/reader/code.js` filters the tree and its slice nodes to `product/`,
excluding `src/test`. `state.js` applies the same restriction to source-page URLs,
so an old deep link cannot expose a book or harness file. The full atlas remains
available to other book readers; it is not rewritten to invent a new catalog.

The selected file stays in the same mounted explorer. A small same-origin message
passes its file and line to the sheet URL and restores them on reload or history
navigation. The existing page generator fingerprints the explorer's changed
assets; the sheet fingerprints its own assets and embedded page. This prevents a
cached, unfiltered explorer from surviving a refresh.

## Generate and review

After changes to the explorer's CSS or scripts, regenerate its existing page:

```sh
.artifacts/book-tools/bin/python - <<'PY'
from pathlib import Path
import sys
sys.path.insert(0, 'book/edition-0.2')
from atlas.pages import code
Path('design/0.2/code.html').write_text(code() + '\n')
PY
node book/navigation/render.mjs
node book/navigation/render.mjs --check
```

The renderer validates source paths, image identities, recording action IDs,
original diagram citations and production-only references. Each demo's recorded
`actual` must match its committed `expected`. The environment and displayed
values are read from that recording; these checks do not rerun Canton.

Use the existing Chrome debugging endpoint and source preview:

```sh
node book/navigation/check.mjs \
  http://127.0.0.1:61322 \
  http://127.0.0.1:56202/book/navigation/index.html
```

The checker creates and closes its own temporary tab. Ten check groups and twelve
screenshots cover the four regions, both illustration close-ups, phone gestures, camera and
source URL restoration, production filtering, annotations, syntax colors, text
bounds and print visibility. Results go to `.artifacts/navigation-sheet-review/`.
Screenshots are inspected as well as measured. Phone testing uses Chrome
emulation. The browser verifies the displayed images against the reviewed assets
by SHA-256, checks intrinsic dimensions and aspect ratios, and fits both complete
illustrations in phone views. No new browser, JVM or ledger process is needed.
