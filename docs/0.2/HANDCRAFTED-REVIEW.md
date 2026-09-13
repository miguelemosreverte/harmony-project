# Entry and illustrated stories: reader.8

This is the historical reader.8 review. The [moment review](MOMENT-REVIEW.md)
supersedes its fixed-image and scroll-based presentation checks.

The home screen no longer asks anyone to obtain or paste a participant link.
The local sandbox offers Alice's workspace and Northbank's workspace. Selecting
one opens that character's view; it does not submit a ledger command. Returning
home shows the same two choices. A copied workspace URL retains its selected
character without including a credential.

The earlier portrait pairs repeated the same composition across unrelated stories.
They have been replaced with 32 separately directed illustrations, one per
recorded workflow, plus four illustrations for authored explanations. Characters
belong to situations: preparing an offer, comparing property models, reviewing a
private file, returning to a saved plan, or preparing a receiving position.

| Story | What the supporting image explains |
| --- | --- |
| Missing purchase evidence | An empty document sleeve interrupts an otherwise prepared offer. |
| Wrong property | Alice and Sofia compare two different houses on a kitchen plan. |
| Private approval | An office partition separates the bank's papers from the shared workspace. |
| Resumed sequence | Alice returns to an evening desk and a bookmarked notebook. |
| Missing transfer readiness | The destination custodian prepares a receiving drawer while the source waits. |

The existing infographic remains the main stage. Its renderer and the CSS before
the support section are unchanged from reader.7. Below it, the scene stays fixed
through the steps while two short HTML lines explain actual observations. Neither
the image nor a golden expectation declares that a command succeeded. The support
section introduces no controls. Desktop places the dialogue beside the image;
mobile places it underneath.

## Visual review

All 36 images were inspected individually or in contact sheets. Two were corrected
to keep Alice as the buyer and give the bank adviser a distinct appearance. All
32 recorded workflows have desktop and mobile browser captures. The review also
covers chapter introductions, the developer view, reviewer relationships, original
document companions, and the live financing, composition, and package screens.

The final live review exposed a white label on a white package action. Its CSS
specificity is corrected, and both viewport captures were repeated. Navigation
and the upper infographic were left in their established positions.

- [Browser screenshot gallery](../../design/0.2/handcrafted-review/index.html)
- [All illustration compositions](../../design/0.2/infographic/handcrafted-v2/artwork.html)
- [Exact generation and edit prompts](../../design/0.2/infographic/handcrafted-v2/prompts.json)
- [Saved image paths, masters, dimensions and hashes](../../design/0.2/infographic/handcrafted-v2/assets.json)

The project assets are in `product/scene/site/assets/stories-v2/`. They were made
with the built-in image generator using the approved desktop study as a visual
reference and an explicit setting, action, and composition for each workflow.
Generation provenance is preserved. These are static supporting illustrations;
the existing interactive infographic and observed dialogue carry step changes.
Screenshots establish the reviewed appearance, not the user's artistic acceptance.

## Validation

| Scope | Result |
| --- | --- |
| Service authorization and sandbox entry | 5 tests pass; self-service disabled outside explicit sandbox mode |
| Shared scenes and recorded narration | 16 tests pass; expectations cannot change narration |
| Original documents and source catalog | 28 checks pass; original quotations and committed goldens preserved |
| Every recorded observation | 130 observations at 1280, 390 and 320 CSS pixels; 3,809 layout checks |
| Stable carousel | 408 checks; 30 captured frames; no empty frame |
| Anonymous, deep-link and expired entry | 23 checks; expired access stops retrying and offers ordinary workspace entry |
| Final live views and casting corrections | 77 checks; six live captures; no ledger commands submitted |
| Served book | 396 source fingerprints verified; product links resolve to the separate live service |
| Standalone export | 14 checks, including keyboard navigation, retained diagrams, static prose and print layout |

Machine-readable reports are `handcrafted-scenes.json`, `handcrafted-delivery.json`,
`sandbox-entry.json`, `stable-carousel.json`, `quiet-delivery.json` and
`quiet-export.json` beside this review. The gallery contains 92 recorded/authored
captures; the same directory holds six live captures, two home captures and six
art contact sheets. Live captures record current sandbox state rather than promise
that a live URL replays an earlier ledger state.

## Ownership and operation

The shared scene package owns typed illustration identifiers and retained DOM
rendering. Product features select their scenes. The book owns its recording map
and chapter choices; production code has no chapter dependency.

The explicit local sandbox provisions role access internally. Configured services
keep their existing authorization boundary. This pass does not invent a production
account system. A small attached service reuses the existing Canton participants
and renews their short-lived demo credentials internally; the ledger was not reset
and no additional Canton process was started. See the reconnect command in the
[implementation plan](HANDCRAFTED-SCENES.md#reconnecting-a-long-running-sandbox).
