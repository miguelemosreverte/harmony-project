# The content sheet

[plan.json](plan.json) is the content proposal. [index.html](index.html) unfolds
all twelve page summaries into four regions on one large sheet. UI frames,
Canton demos, engineering diagrams and production excerpts are visible together. No page turns,
selectors or content buttons are needed. This remains a discussion document;
the approved product and book UI, styles and navigation are unchanged.

The proposed audiences are **User, Investor, Architecture and Implementation**.
Original-source citations accompany the relevant claim within these paths.
They do not create a fifth path. Read numbered material left to right within each
region. Regions have their own conclusions and do not redirect into one another.

- **User:** the existing illustrated journey, preserved without content changes.
- **Investor:** three Keynote demo storyboards for the Canton ecosystem. Establish
  the environment, invite a prediction, perform an action, then inspect the
  recorded outcome. Private approval, a compiled adapter and atomic settlement
  each have a model diagram and concrete results. These are recorded local Canton
  runs, not live transactions performed by the sheet or Global Synchronizer demos.
- **Architecture:** six engineering diagrams: deployment, package dependencies,
  submission and observation, uncertain outcomes, private proof consumption, and
  the final settlement transaction boundary. No user-scene screenshots.
- **Implementation:** production Scala and Daml only. Follow the financing slice
  from its web panel and API model into choice selection, IO and ledger authority.
  Book code, scripts, generators, tests and golden fixtures are excluded.

Drag or scroll to pan. Pinch or use Ctrl + scroll to zoom around the pointer.
Arrow keys pan, `+` / `-` zoom, and `Home` / `0` fit the whole sheet. The URL stores
the sheet's center (`x`, `y`) and scale (`z`); reopening it restores that view,
subject to the receiving viewport's bounds. No controls are drawn over the sheet.

Each page records its purpose through `show` and `message`, with named source
references. `frames` selects existing images by identity; `excerpts` selects exact
lines from repository files. `files` names production owners and their purposes.
`diagrams` selects the native SVG figures authored in [figures.json](figures.json)
and rendered by [figures.mjs](figures.mjs). `demo` selects actual action IDs and
result fields from the committed Canton recordings in `book/edition-0.2/recordings`.
Existing workflow exports in `book/showcase/documents` remain factual snapshots
of current behavior; this proposal does not rewrite their navigation history.

Edit JSON, then regenerate or check the static HTML:

```sh
node book/navigation/render.mjs
node book/navigation/render.mjs --check
```

The renderer verifies source paths, recording step IDs, image references, excerpt
bounds, production-only Implementation references and HTML reproducibility. Each
demo's recorded `actual` must equal its committed `expected`; its environment and
displayed results come from that recording. This checks stored evidence without
rerunning a ledger. The sheet reuses six existing images in User and contains nine
native vector diagrams across Investor and Architecture. Image bytes are not
copied into this folder; diagrams use the established blue visual language.

Canton terminology follows Digital Asset's SDK 3.4 documentation:
[Synchronizer](https://archived.docs.digitalasset.com/subnet/3.4/overview/index.html)
and [Protocols on One Synchronizer](https://archived.docs.digitalasset.com/overview/3.4/explanations/canton/protocol.html).
The corresponding production sources and recorded topologies are named in JSON.

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

The checker creates and closes its own temporary tab. Results and nine screenshots
go to `.artifacts/navigation-sheet-review/`, or an optional third output argument.
It checks fit, pointer-anchored zoom, drag, wheel movement, touch pinch, keyboard
navigation, shared URLs, history, malformed addresses and print. It also checks
that text stays within its own page, SVG labels fit their nodes, the expected demos
and diagrams are present, Implementation lists production files, and moving the
camera retains the DOM.

Review captures include the desktop overview, all four region views, Investor and
Architecture close-ups, and phone overview/detail. Sheet-specific excerpt styles
wrap unaltered source text. Gesture checks use Chrome desktop and phone emulation,
not physical-device testing. The eight browser check groups passed with no
JavaScript errors; screenshots were also inspected for spacing and legibility.
