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
- **Architecture:** the two SVGs from the supplied architecture document, with
  their original content and layout in the shared white-and-blue style.
- **Implementation:** the existing `design/0.2/code.html` explorer, embedded without
  its surrounding navigation. Its production tree, syntax colors, source comments
  and vertical-slice renderer are reused. No replacement excerpt viewer is built.

This is a content proposal. It runs no ledger commands. A proposed model, a
current implementation choice and a recorded result are different kinds of
information, labeled where they appear. The approved workflow UI is unchanged.

## Translating the original architecture

The source is [harmonia-architecture.html](../../docs/proposal/harmonia-architecture.html):

1. **Component map:** off-ledger / on-ledger split, build and runtime paths,
   component responsibilities and application participation.
2. **Contract model:** definitions, instances, assignments, bindings,
   continuations, interfaces and application templates.

[original-diagrams.mjs](original-diagrams.mjs) reads those two SVGs directly.
It preserves every label, field, choice, connector and coordinate. It scopes
marker IDs and replaces the original styles with
[original-diagrams.css](original-diagrams.css): readable sans-serif text, white
surfaces, pale-blue ledger components and blue arrows. The second diagram's
shared arrow marker is included so it renders independently of the first.
Both carry their original filename and section number. Pan and zoom reveal the
full diagrams without a separate navigation interface.

The source identifies the template and choice names as illustrative. Its open
binding and package-import questions remain visible as such. These diagrams
describe the supplied architecture; they are not a claim that every proposed
template name exists in the checkout.

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

The checker creates and closes its own temporary tab. Ten check groups and ten
screenshots cover the four regions, both original diagram close-ups, phone gestures, camera and
source URL restoration, production filtering, annotations, syntax colors, text
bounds and print visibility. Results go to `.artifacts/navigation-sheet-review/`.
Screenshots are inspected as well as measured. Phone testing uses Chrome
emulation. The browser compares all 200 SVG text elements and 303 geometric
elements with the original HTML and checks all 25 arrowheads, including legend
arrows. No new browser, JVM or ledger process is needed.
