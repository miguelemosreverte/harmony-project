# The content sheet

[plan.json](plan.json) is the content proposal. [index.html](index.html) unfolds
all twelve page summaries into four regions on one large sheet. Existing UI
frames, source excerpts and their references are visible together. No page turns,
selectors or content buttons are needed. This remains a discussion document;
the approved product and book UI, styles and navigation are unchanged.

The proposed audiences are **User, Investor, Architecture and Implementation**.
Original-source citations accompany the relevant claim within these paths.
They do not create a fifth path. Read numbered material left to right within each
region. Regions have their own conclusions and do not redirect into one another.

Drag or scroll to pan. Pinch or use Ctrl + scroll to zoom around the pointer.
Arrow keys pan, `+` / `-` zoom, and `Home` / `0` fit the whole sheet. The URL stores
the sheet's center (`x`, `y`) and scale (`z`); reopening it restores that view,
subject to the receiving viewport's bounds. No controls are drawn over the sheet.

Each page records its purpose through `show` and `message`, with named source
references. `frames` selects existing images by identity; `excerpts` selects exact
lines from repository files. A selected recording can identify observed step IDs.
Existing workflow exports in `book/showcase/documents` remain factual snapshots
of current behavior; this proposal does not rewrite their navigation history.

Edit JSON, then regenerate or check the static HTML:

```sh
node book/navigation/render.mjs
node book/navigation/render.mjs --check
```

The renderer verifies source paths, recording step IDs, image references, excerpt
bounds and HTML reproducibility. The JSON references fifteen existing images;
image bytes are not copied into this folder. There is no new illustration style.

`sheet.css` arranges the paper; `sheet.js` moves one mounted DOM surface. Without
JavaScript the document remains natively scrollable. Print removes the camera
transform and exposes all content. The sheet has no dependency on a live ledger.

## Review

Use an existing local Chrome debugging endpoint and the source preview:

```sh
node book/navigation/check.mjs \
  http://127.0.0.1:61322 \
  http://127.0.0.1:56202/book/navigation/index.html
```

The checker creates and closes its own temporary tab. Results and seven screenshots
go to `.artifacts/navigation-sheet-review/`, or an optional third output argument.
It checks fit, pointer-anchored zoom, drag, wheel movement, touch pinch, keyboard
navigation, shared URLs, history, malformed addresses and print. It also checks
that text stays within its own page and that moving the camera retains the DOM.

The desktop overview, all four region views, and phone overview/detail screenshots
were inspected. Source excerpts initially inherited the source explorer's minimum
line width and overflowed their columns. Sheet-specific line styles now wrap the
unaltered text; the browser check guards against a recurrence. Gesture checks use
Chrome desktop and phone emulation, not physical-device testing.
