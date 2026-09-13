# Carousel walkthrough

The infographic summarizes the active person, changed state, and application
boundaries. Connected circles below show progress and explicit forks. Names appear
on hover or focus; selecting a stop keeps the scene mounted. The four audience
entrances remain separate. See [the compact graph contract](GRAPH-NAVIGATION.md).

| Screen | Reader's question | What changes on selection | Next move |
| --- | --- | --- | --- |
| Purchase | How does financing become an offer? | Focused actor, approval badge, proposal connectors and observed decision | Select any stop in either outcome path, or continue to transfer |
| Transfer | Where are the assets, and what can fail? | Acting party, reserved/received amounts and observed attempt | Compare settlement with its refusal on the same stage |
| Evidence laboratory | What exactly was tried and observed? | Actor badge or observation strip and diagram state; sorted expected/actual values below | Select any observation directly; arrows connect the 32 recordings |
| Reviewer | Which relationships does the code implement? | Reviewed manifest, dependency or execution graph and cited evidence | Select a relationship in the rail |
| Original author | Where did this passage come to fruition? | Selected original passage and its explanatory companion | Scroll the original or choose a passage directly |
| Developer | What is this source file responsible for? | Highlighted exact source and annotated vertical slice | Choose a file in the tree |
| Live financing | What can I observe and do here? | Selected actor's focus, with current facts kept intact | Inspect any stage; submit only the eligible labeled action |
| Plan editor | What am I agreeing to? | The draft remains visible; the focused question changes | Select a question, answer its branch in the dock, then review and propose |
| Consented plan | Which party owns each action? | Inspection highlight and current observed status | Inspect freely; execute the action assigned to this participant |
| Package workspace | What has inspection or compilation established? | The same graph shows input, mapping, compilation and availability | Inspect any stage; run the separately labeled operation |

## Captures

Captures are in [the review directory](../../design/0.2/carousel-review). Their
filenames distinguish desktop (1280) and mobile (390); summary captures also
include the narrow 320px layout. They include purchase,
transfer, live financing, draft editing, package inspection, consented completion,
reviewer relationships, original passages and the product explanation.

The mobile editor uses compact diagram cards so the current input can sit beside
its context. The dock has an opaque background; body text cannot bleed through
its controls. Long documents and diagrams retain native vertical scrolling.

## Verification

- `stable-carousel.json`: directly traversed all 130 recorded observations; checked
  retained portraits/stages, fixed arrow targets, visible selected stops, cold
  addresses, browser history, hover feedback, and old outcome URLs.
- `carousel-pixels.json`: actual PNG differences for 26 consecutive stage pairs,
  with changed area and positional drift measured separately.
- `compact-graph.json`: shared starts, fork addresses, keyboard navigation,
  44px targets, compact labels, and badge spacing at 1280, 390, and 320px.
- `stable-live.json`: inspection and unsubmitted draft navigation on the real
  service; no ledger commands were submitted by those inspection checks.
- `carousel-ledger.json`: prior `v0.2.0-reader.5` evidence of real approval,
  continuation, consented execution and package compilation. This presentation
  revision preserves that ledger behavior; its live checks inspect without submitting.
- `quiet-export.json`: 14 checks of the standalone export, retained diagrams,
  direct keyboard navigation, JavaScript-free prose, and PDF output.
- Scala projection tests verify that expectations cannot paint observed success,
  and that a committed decline is not rendered as an approval.
- Edition checks retain all 736 original quotation units unchanged and keep source
  fingerprints and committed golden expectations intact.

Earlier reports that enforce two arrow buttons total are historical. The current
contract uses accessible names on connected circles and keeps input answers
visible in the shared dock.
