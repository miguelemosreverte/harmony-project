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

- [ ] Generate and retain a visual concept with its exact prompt.
- [ ] Build four HTML cards using a shared layout and deliberately drawn SVG scenes.
- [ ] Keep each scene's meaning and evidence in `cards.json`.
- [ ] Check every stop, direct URLs, history and keyboard/touch navigation.
- [ ] Capture and inspect complete desktop and phone screenshots.
- [ ] Verify print output and that routes remain within their own card.
- [ ] Open the result for review; commit and push only this study.

`cards.json` owns content and source references. `drawings.js` owns the sixteen
pictures. `cards.js` owns selection and URL state. `cards.css` owns the page and
print layout. The four small HTML entry files each identify one branch.

The product card describes the local implementation's mechanism; it does not
claim independent adoption. The workflow depicts the recorded property example,
including Ben and Sofia; the current live financing UI has a narrower scope.
The architecture pictures describe successful command handling and do not imply
that every submitted command is accepted. The code card references real files.
