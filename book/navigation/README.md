# Proposed reading paths

[plan.json](plan.json) is the content proposal. [index.html](index.html) shows all
four paths at once, with existing screenshots as visual references. It has no
interactive controls and works without JavaScript. This is a discussion document;
the approved product and book UI, styles and navigation are unchanged.

The proposed audiences are **User, Investor, Architecture and Implementation**.
Original-source citations accompany the relevant claim within these paths.
They do not create a fifth path. Listed pages run in order and stop within their
own branch. The address pattern in JSON is proposed, not an existing route.

Each page records its purpose through `show` and `message`, with named source
references. A selected recording can also identify exact observed step IDs.
Existing workflow exports in `book/showcase/documents` remain factual snapshots
of current behavior; this proposal does not rewrite their navigation history.

Edit JSON, then regenerate or check the static HTML:

```sh
node book/navigation/render.mjs
node book/navigation/render.mjs --check
```

The renderer verifies source paths, recording step IDs and HTML reproducibility.
Its output is an overview for discussing content, not a new visual direction.

Browser review verified four paths, twelve page summaries, four loaded reference
images, no interactive controls and no horizontal overflow on desktop and phone.
Both full-page screenshots were inspected. This overview scrolls as a document;
it does not change the workflow UI's viewport rules.
