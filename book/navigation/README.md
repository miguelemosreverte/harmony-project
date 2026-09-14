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
- **Architecture:** the supplied component and contract models are the starting
  point. Six diagrams pair those proposals with actual package imports,
  concept-to-code correspondence, scoped continuation and atomic settlement.
- **Implementation:** the existing `design/0.2/code.html` explorer, embedded without
  its surrounding navigation. Its production tree, syntax colors, source comments
  and vertical-slice renderer are reused. No replacement excerpt viewer is built.

This is a content proposal. It runs no ledger commands. A proposed model, a
current implementation choice and a recorded result are different kinds of
information, labeled where they appear. The approved workflow UI is unchanged.

## Original diagrams and implementation choices

The earlier sheet emphasized participant topology and HTTP submission. That
omitted the logical architecture that the supplied documents actually propose.
The new component map preserves the off-ledger / on-ledger split and the named
Dapp, Builder, Package Manager, Core, Binding DAR, two application paths and
References. The contract model preserves all eight proposed entities and their
relationships. Each redraw carries its original filename and line range.

The adjacent implementation mapping explains the concrete choices:

| Proposed concept | Current representation |
| --- | --- |
| WorkflowDefinition | `Definition` data and `PublishedDefinition` contract |
| WorkflowInstance | `ProcessInstance` for the bounded process engine |
| RoleBinding / StepAssignment | `Role`, `StepSpec`, `ActionBinding` within the process |
| HarmoniaStepAction | `StepAction` interface |
| Continuation | In the private-financing example, `VerifiedResult` consumed by `SharedProgress` |
| AppliesTo | Build-time mapping and a compiled application binding |

These are correspondences, not a claim that every conceptual template exists.
The source leaves import direction open; the current package diagram shows the
separate shared-interface DAR. Both are visible without switching pages.

The original input files are preserved under `docs/proposal/`. Diagram content and
geometry live in [figures.json](figures.json); [figures.mjs](figures.mjs) renders
native SVG and verifies cited source ranges. Marks and their primary-source
provenance are recorded in [assets/README.md](assets/README.md).

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

The checker creates and closes its own temporary tab. Nine check groups and nine
screenshots cover the four regions, diagram close-ups, phone gestures, camera and
source URL restoration, production filtering, annotations, syntax colors, text
bounds and print visibility. Results go to `.artifacts/navigation-sheet-review/`.
Screenshots are inspected as well as measured. Phone testing uses Chrome
emulation. No new browser, JVM or ledger process is needed.

The existing annotation-wrapping, comment-lexing and source-coloring tests also
passed. Production code and User content were not changed by this refinement.
