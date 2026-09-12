# A continuous, visual reader

The [stable carousel revision](STABLE-CAROUSEL.md) supersedes this checkpoint's
restriction to two arrow controls. The persistent rendering and URL guarantees
below remain in force.


The infographic carries the explanation. A reader turns one frame at a time,
with two familiar directions and no moving controls. The four reading paths
remain distinct; exact original quotations and recorded observations remain intact.

## Implementation sequence

- [x] Keep the shared scene objects alive during updates.
  - Reconcile diagram cards and measured arrows by identity.
  - Paint arrows with their cards, without a blank intermediate frame.
- [x] Keep the book shell alive during navigation.
  - Load local chapters in the current document; preserve native links as fallback.
  - Give each mounted page an explicit lifetime and clean up its listeners.
  - Preserve query addresses, browser history, modified clicks and print output.
- [x] Make navigation a carousel edge, in a fixed place.
  - Two consistent previous/next targets; keyboard and horizontal swipe do the same work.
  - Keep the binary choice cards themselves clickable; add no selector panel.
- [x] Let the diagrams speak.
  - Remove repeated scene mechanisms and lists that restate every diagram node.
  - Replace the author's reloading companion with the shared renderer.
  - Keep supplied drawings in the original archive; explain them with our infographics.
  - Keep original quotations and explicit evidence limitations readable.
- [x] Sort displayed JSON object keys recursively; retain array order and values.
- [x] Apply stable navigation and action placement to the separate live application.
- [x] Verify desktop and mobile: screenshots, control positions, retained DOM,
  complete routes, history, keyboard, touch, source fidelity and live actions.
- [x] Commit and push the verified delivery.

## Screen contract

| Screen | What the reader sees | What the reader does |
| --- | --- | --- |
| Reading fork | Two short, illustrated destinations | Choose one card |
| Recorded story | People, application boundaries and the current handoff | Turn the carousel or swipe |
| Reviewer | One relationship in the shared diagram style, with its evidence limit | Previous or next relationship |
| Original author | Highlighted original text beside its matching infographic | Scroll the source or turn the passage |
| Developer | One source tree and annotated, colored source | Select a file or finish |
| Evidence | Observed infographic followed by consistently ordered comparison data | Previous or next observation |
| Live task | The current observed state and its eligible action | Perform that action or continue the task |

Only the file tree is a composite interaction. There are no hidden control banks.
The carousel controls stay in the same viewport positions, including when captions
wrap. Reduced-motion readers receive the same content without motion. A pending
navigation retains the current page until its replacement is ready. Network errors
retain a usable page and its original native destination.

## Verification

The approved scene geometry remains shared by the book and live product. The
written explanation chapters now contain 403 words, down from 1,586 at reader.3;
this comparison counts the eight Markdown chapters, not their new diagram labels.
The opening reviewer screen contains 99 visible words, down from 216.

| Evidence | Result |
| --- | --- |
| Shared renderer and reader tests | Valid diagrams for all 130 observed moments; expectations cannot alter the observed scene |
| Source and quotation build | 28 checks; 736 original units preserved, including readable original diagram labels |
| Complete reading routes | 674 assertions; 14 reviewer frames, 26 passages, 32 recordings and 130 observations |
| Continuous reader | 35 assertions; retained objects, fixed control rectangles, failed-request recovery and offline history |
| Visual walkthrough | 36 desktop/mobile captures; two interactions and no horizontal page overflow |
| Live walkthrough | 73 assertions and 30 captures; financing, consented execution and adapter compilation |
| Live navigation | 11 assertions; retained document, stable draft controls and history, without ledger mutations |
| Delivered source | All 384 catalogued files match their served SHA-256 fingerprints |
| Standalone export | Two-control playback, readable no-JavaScript chapters and an inspected A4 PDF |

See [the screen review](PAGE-REVIEW.md), [continuous reader checks](continuous-reader.json),
[live navigation checks](continuous-live.json), and [source delivery checks](quiet-delivery.json).
The recording provenance remains historical. The final disposable sandbox is fresh:
Bank has a pending case, and Buyer and Reviewer do not receive private Bank details.

To reproduce the stability checks against an owned local Chrome debugging port:

```sh
node design/0.2/checks/continuous-reader.mjs http://127.0.0.1:CDP_PORT BOOK_EDITION_URL
node design/0.2/checks/continuous-live.mjs http://127.0.0.1:CDP_PORT PRIVATE_SESSIONS_JSON
```

The live stability script follows the completed plan from `quiet-live.mjs` and only
edits an unsubmitted draft. Use a separate `scripts/harmonia export-book` directory
for `quiet-export.mjs`; the sandbox's book host opens the curated edition directly.

The original component and contract-model passages have their own redrawn proposal
diagrams. Their original names are checked independently of the general product
overview; current implementation limits remain alongside them.
