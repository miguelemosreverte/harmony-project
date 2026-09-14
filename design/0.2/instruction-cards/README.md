# Four instruction cards

An isolated visual study using the language of airplane instruction brochures:
large drawings, numbered actions, short captions and an obvious reading order.
The four topics follow the newer local Product / Workflow / Architecture / Code
pages. Existing uncommitted branch and brochure pages are outside this study.

## Reader contract

| Card | Question | Four pictures | Finish |
| --- | --- | --- | --- |
| Product | What does Harmonia provide? | Independent owners; agreed plan; local authority; persistent progress | Explain the product in one sentence |
| Workflow | What happens in the purchase? | Northbank approves; Alice proposes; Ben relays; Sofia receives | Recognize what moved and what stayed private |
| Architecture | Where does a command go? | Browser; Scala service; authorized Daml choice; observed state | Distinguish requesting from committing |
| Code | Where should I start reading? | A feature; its typed request; its ledger choice; its golden story | Know which file answers which question |

Each card has its own URL and finite four-stop rail. It contains no links into
another card. There is no default transition into an unrelated scenario. The
circles in the bottom rail are the only controls; arrow keys and a touch swipe
select the adjacent stop. Selection never submits a ledger command.

The complete four-picture card is visible on a wide screen and on paper. A narrow
screen displays one picture at a time in a stable frame. The URL's `step` parameter
restores the chosen picture. Browser Back and Forward restore the same state.
Wide screens omit the explanatory captions; phones and paper retain one short
sentence per picture. The drawings and reading order remain the same.

## Open a card

Serve the repository root with any static HTTP server. These pages fetch their
content from `cards.json`, so open them through HTTP rather than `file://`.

| Card | Interactive | Printable |
| --- | --- | --- |
| Product | [Four pictures](product.html?step=0) | [One-page PDF](review/product.pdf) |
| Workflow | [Four pictures](workflow.html?step=0) | [One-page PDF](review/workflow.pdf) |
| Architecture | [Four pictures](architecture.html?step=0) | [One-page PDF](review/architecture.pdf) |
| Code | [Four pictures](code.html?step=0) | [One-page PDF](review/code.pdf) |

Read the numbers in order. Select a circle to focus a picture; on a phone, swipe
across the picture. The final checked circle is the end, with no next branch.

## Visual direction

Use navy line drawings, pale blue application boundaries and the small orange
number discs found in the existing brochure study. Keep every caption to one
short sentence. Each drawing has a different composition appropriate to its
meaning. The selected picture gets a border and a small motion cue; nothing
changes its position. Reduced-motion preferences suppress the cue.

Do not add a dashboard, audience selector, action toolbar, long explanatory text
or buttons inside the pictures. Do not modify the previously approved workflow
infographic. This is an authored explanation, with evidence references in the JSON
and this document rather than additional screen controls.

## Build and review

- [x] Generate and retain a visual concept with its exact prompt.
- [x] Build four HTML cards using a shared layout and deliberately drawn SVG scenes.
- [x] Keep each scene's meaning and evidence in `cards.json`.
- [x] Check every stop, direct URLs, history and keyboard/touch navigation.
- [x] Capture desktop and phone screenshots; inspect all sixteen illustrations.
- [x] Verify print output and that routes remain within their own card.
- [x] Open the result for review and preserve unrelated local edits.

`cards.json` owns content and source references. `drawings.js` owns the sixteen
pictures. `cards.js` owns selection and URL state. `cards.css` owns the page and
print layout. The four small HTML entry files each identify one branch.

The [generated concept](concept.png) was made with the built-in image-generation
tool. Its [generation record](generation.json) retains the exact prompt and master
location. The interactive cards are a native HTML/SVG interpretation of that
direction, not a pixel-identical rendering of the raster image.

## Review evidence

[The browser report](review/checks.json) records 48 states across all four cards
and four stops, including desktop and two phone viewport sizes. The run produced
32 screenshots and four single-page A4 landscape PDFs. It found no JavaScript
exceptions or page overflow in those states. Navigation stays in the same card;
panel and progress-rail bounds stay fixed when the selection changes. Reload,
Back/Forward, arrow keys and synthetic touch swipes restore the expected stop.

Visual inspection covered all sixteen drawings on desktop and phone. Captions
initially overlapped SVG artwork; explicit picture bounds fixed that, and the
complete browser run was repeated. The Product PDF was also rasterized and
inspected. The check uses desktop Chrome emulation, not physical phone testing.

Reproduce using a local Chrome debugging endpoint and an HTTP preview:

```sh
node design/0.2/instruction-cards/check.mjs \
  http://127.0.0.1:61322 \
  http://127.0.0.1:56202/design/0.2/instruction-cards/
```

The checker opens and closes its own temporary tab. Substitute current local
ports when necessary. It does not start a JVM, submit ledger commands or rebuild
the book.

The product card describes the local implementation's mechanism; it does not
claim independent adoption. The workflow depicts the recorded property example,
including Ben and Sofia; the current live financing UI has a narrower scope.
The architecture pictures describe successful command handling and do not imply
that every submitted command is accepted. The code card references real files.
