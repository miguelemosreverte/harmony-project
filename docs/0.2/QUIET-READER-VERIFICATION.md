# Reader.3 verification

This checkpoint applies the two-interaction contract to the book and live product.
The [authored walkthrough](WALKTHROUGH.md) explains each screen, its purpose and its
next destination. The [book captures](PAGE-REVIEW.md) and
[live captures](LIVE-PAGE-REVIEW.md) show actual browser rendering.

## Verified behavior

| Evidence | Result |
| --- | --- |
| Product boundary, runner, recording, reader and scene Scala tests | Passed; product cannot compile against book or harness classes |
| Source and quotation build | 27 checks passed; every original unit appears once and unchanged |
| Book desktop/mobile capture pass | 18 page types, 36 captures, at most two interactions and no horizontal page overflow |
| Complete reading routes | Both purchase outcomes, both transfer outcomes, chapter/sandbox branches, 14 reviewer frames, 26 original passages |
| Recorded playback | All 32 registered stories and 130 observed moments; finite ending |
| Navigation | Cold file and composition URLs, Back/Forward, native links, keyboard and touch; passive author companions |
| Real product walkthrough | Bank approval, Buyer continuation, observer privacy, consented proposal and both assigned actions committed |
| Real package journey | Participant DAR retrieved, mapping inspected, adapter compiled, nonempty ZIP downloaded |
| Recovery | Offline connection exposes recovery; reconnect restores the unfinished task |
| Standalone export | Two-action Scala reader at desktop/mobile widths, readable no-JavaScript prose, working Next |
| PDF | A4 chapter exported; the actual PDF was rasterized and visually inspected |

The workflow recordings remain historical evidence with their original revision,
timestamp, input, expectation and observation fingerprints. The live checks ran on
a disposable local Canton network. They do not establish external integration,
production adoption or the unimplemented broad workflow scope.

## Reproduce

After `scripts/build` for the initial toolchain setup:

```sh
scripts/build-design
scripts/start-sandbox
```

The launcher prints distinct product and book addresses and the private participant
launcher path. Keep one disposable network running. Ctrl-C closes its owned resources.

Use an owned local Chrome debugging endpoint for these scripts:

```sh
node design/0.2/checks/quiet-reader.mjs http://127.0.0.1:CDP_PORT
node design/0.2/checks/quiet-routes.mjs http://127.0.0.1:CDP_PORT
node design/0.2/checks/quiet-live.mjs http://127.0.0.1:CDP_PORT PATH_TO_PRIVATE_SESSIONS_JSON
node design/0.2/checks/quiet-export.mjs http://127.0.0.1:CDP_PORT EXPORTED_BOOK_URL
node design/0.2/checks/quiet-delivery.mjs http://127.0.0.1:CDP_PORT BOOK_URL PRODUCT_URL
```

The first two default to the local `scripts/design-preview` address. The live script
mutates the disposable example; use a fresh sandbox for a complete walkthrough.
Participant capabilities are read locally and omitted from reports. The delivery
check downloads every catalogued source from the served export and compares its hash.

The JSON reports alongside this document list individual assertions and observed
browser errors. Older `reader-*` and `reference-*` reports retain their historical
meaning and describe the controls present at those earlier checkpoints.
