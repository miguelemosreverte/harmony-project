# Carousel walkthrough

The body keeps the explanation visible. Its dock contains named stops and paths;
selecting a stop changes the observation or inspection, rather than opening a
choice page. The four audience entrances remain separate.

| Screen | Reader's question | What changes on selection | Next move |
| --- | --- | --- | --- |
| Purchase | How does financing become an offer? | Focused actor, approval badge, proposal connectors and observed decision | Select any named stop in either outcome path, or continue to transfer |
| Transfer | Where are the assets, and what can fail? | Acting party, reserved/received amounts and observed attempt | Compare settlement with its refusal on the same stage |
| Evidence laboratory | What exactly was tried and observed? | The observation strip and applicable diagram state; sorted expected/actual values below | Select any observation directly; arrows connect the 32 recordings |
| Reviewer | Which relationships does the code implement? | Reviewed manifest, dependency or execution graph and cited evidence | Select the named relationship in the rail |
| Original author | Where did this passage come to fruition? | Selected original passage and its explanatory companion | Scroll the original or choose a passage directly |
| Developer | What is this source file responsible for? | Highlighted exact source and annotated vertical slice | Choose a file in the tree |
| Live financing | What can I observe and do here? | Selected actor's focus, with current facts kept intact | Inspect any stage; submit only the eligible labeled action |
| Plan editor | What am I agreeing to? | The draft remains visible; the focused question changes | Select a question, answer its branch in the dock, then review and propose |
| Consented plan | Which party owns each action? | Inspection highlight and current observed status | Inspect freely; execute the action assigned to this participant |
| Package workspace | What has inspection or compilation established? | The same graph shows input, mapping, compilation and availability | Inspect any stage; run the separately labeled operation |

## Captures

Captures are in [the review directory](../../design/0.2/carousel-review). Their
filenames distinguish desktop (1280) and mobile (390). They include purchase,
transfer, live financing, draft editing, package inspection, consented completion,
reviewer relationships, original passages and the product explanation.

The mobile editor uses compact diagram cards so the current input can sit beside
its context. The dock has an opaque background; body text cannot bleed through
its controls. Long documents and diagrams retain native vertical scrolling.

## Verification

- `stable-carousel.json`: directly traversed all 130 recorded observations; checked
  retained portraits/stages, fixed arrow targets, visible selected stops, cold
  addresses, browser history, hover feedback, and old outcome URLs.
- `carousel-pixels.json`: actual PNG differences for 40 consecutive stage pairs,
  with changed area and positional drift measured separately.
- `stable-live.json`: inspection and unsubmitted draft navigation on the real
  service; no ledger commands were submitted by those inspection checks.
- `carousel-ledger.json`: real approval, continuation, consented execution and
  package compilation through the new controls on a disposable Canton network.
- `quiet-export.json`: 14 checks of the standalone export, retained diagrams,
  direct keyboard navigation, JavaScript-free prose, and PDF output.
- Scala projection tests verify that expectations cannot paint observed success,
  and that a committed decline is not rendered as an approval.
- Edition checks retain all 736 original quotation units unchanged and keep source
  fingerprints and committed golden expectations intact.

Earlier reports that enforce two arrow buttons total are historical. The current
contract explicitly permits named stops and branch choices in the shared dock.
