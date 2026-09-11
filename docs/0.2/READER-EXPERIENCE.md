# Four separate ways to understand Harmonia

The current contract is [one purpose, two possible actions](UX.md). Follow the
[authored walkthrough](WALKTHROUGH.md) for the page sequence and
[desktop/mobile page review](PAGE-REVIEW.md) for captured screens.

| Path | Entry | Material | End |
| --- | --- | --- | --- |
| Product | Understand → Product | Alice's purchase; approval/refusal; custody transfer; ten chapters | Readiness, original context and quotation boundary |
| Developer | Understand → Code | One file tree; exact colored source; annotations; passive slice diagram | Explicit return to reading paths |
| Reviewer | Verify → Architecture | Six reading maps, five parsed manifest relationships, three reviewed execution paths | Coverage and implementation boundary |
| Author | Verify → Original documents | All 26 source passages; selected quote beside reused explanation | 736/736 exact quotation units and explicit implementation limits |

The separate recording route visits all 32 registered examples and 130 recorded
observations. Every observation uses its actual recorded data; the committed
expectation appears beside it. Playback cannot submit a ledger command. The final
observation leads to coverage, without a carousel wrap.

## Source ownership

`product/scene/` owns typed scene and diagram renderers, measured connectors and
shared presentation. The product browser supplies observed financing, composition
and package state. It does not depend on book pages or chapter identifiers.

The book's `edition-0.2/atlas/` owns source extraction, reading maps and quotations.
Generic `@module.slice`, `@module.role` and `@module.summary` comments describe product
responsibilities independently of the book. Book-owned sources may use `@book`.
The extractor recognizes documentation syntax only: it does not infer Scala types,
Daml choices or dependencies from text matching. Daml dependencies are parsed as
manifest data; reviewed execution relationships carry exact source fingerprints.

The generated source catalog excludes build outputs and runtime credentials. It
retains each file's exact text, hash and line count. Files without a specific authored
annotation receive labeled package context, without invented line explanations.

## Running and verifying

After the initial setup, `scripts/start-sandbox` builds the renderers and the book,
then starts one disposable Canton network, one product server, and a separate book
server. The private participant launcher and both public local addresses are printed.
Ctrl-C closes all owned services. The product's `/book/` returns 404.

For ledger-free recorded reading, run `scripts/design-preview`. The designed book
is also exported as ordinary files; readable chapters support PDF printing.

Current browser evidence:

- `quiet-reader-browser.json`: desktop/mobile interaction counts and screenshots.
- `quiet-reader-routes.json`: finite routes, all recordings, source/history restoration and PDF.
- `quiet-live.json`: actual financing, consent, composition, package compilation and recovery.

The older `reader-*`, `reference-*` and `verification-*` reports describe their
named historical checkpoints. Their drawers, selectors, tabs and mounted book
routes have been replaced.

Presentation URLs still select a recorded scene with `step` and `present=1`.
Arrow keys or touch advance it. There is no autoplay control. The existing
`book/edition-0.2/presentation.mjs` captures four named HTML scenes for an MP4 without
submitting commands; it requires an owned local browser and ffmpeg.
