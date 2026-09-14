# Text before diagrams

Recorded: 2026-09-14 UTC. Previous artifact: version 7 at `ab03761`.

Miguel asked to begin with a concise, structured Markdown chapter of roughly
500 words, explaining the architecture to a software engineer who has no project
context. Only after reviewing the prose should we decide whether individual
passages benefit from inline diagrams. No new image, diagram or HTML redesign
belongs in this pass.

The draft is [architecture.md](architecture.md). It establishes the purpose,
execution boundary, contract mechanism, integration paths and composition limits.
Technical names are introduced after their meaning. It describes the architecture
proposed by the original documents; it does not claim that current implementation
coverage has been audited. The original documents were reread for this draft.

Length: 442 words including headings, 463 including source references. Counts
use rendered Markdown text, excluding standalone punctuation. Source links and
Markdown formatting were checked. No diagrams were added.

User review: pending.

## Browser edition

The user subsequently asked to read the chapter in the browser. [architecture.html](architecture.html)
renders the same Markdown in a single reading column, preserving the established
white/navy palette. It adds no diagrams, navigation menus or extra content.
The source citations remain the only two links. Rebuild and open with:

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-text.py
open 'http://127.0.0.1:56202/book/navigation/explorations/architecture-structure/architecture.html'
```

Checked desktop and mobile: all five sections render, both source links resolve,
the final paragraph is reachable, and there is no horizontal overflow or browser
error. Desktop screenshot visually inspected. Screenshots and checks are saved
under `.artifacts/architecture-text-review/`. Reading uses ordinary page scrolling.
