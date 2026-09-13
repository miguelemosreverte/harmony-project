# From repeated pictures to a visible sequence

Reader.8 assigned one picture to each workflow and validated the lower scene after
scrolling to it. That missed both the viewport requirement and the story. This pass
allocates the screen to the complete moment and directs the illustration from the
observed action. It does not change the ledger contract or golden expectations.

## What a reader follows

| Journey | Visible consequence below the stable diagram |
| --- | --- |
| Purchase | Bank signs → offer opens → proposal is prepared → Ben relays → Sofia receives |
| Declined financing | Alice leaves the bank without approval → Ben returns the proposal |
| Four-party transfer | Agreement → assets locked → source ready → receipt prepared → destination ready → assets arrive |
| Refused final transfer | Destination refuses; the earlier source lock remains |
| Branch and join | Choose the path → finance and review separately → bring both results together |
| Composition | Propose the plan → buyer consents → each assigned action runs in order |
| Retry and reconnect | Find the existing record → keep the pending work → return to finish |
| Package input | Inspect the DAR → identify its mapping → compile the adapter, or show the specific refusal |

The public purchase and transfer chapters follow the main handoffs and expose
alternative endings in the bottom graph. The laboratory retains every observation,
including refused shortcuts. Its header opens the exact recorded and expected
values. The author and source-code paths retain their document panes.

The artwork uses varied compositions: a document handoff, a custody case, a
receiving tray, a pair of converging folders, a workshop, and a filed receipt.
The asset names describe these moments. Selection is based on recorded results;
changing a golden expectation cannot paint a successful outcome.

## Layout and verification

- 31 new illustrations, with prompts, original paths and hashes in
  [the generation record](../../design/0.2/infographic/moments-v3/assets.json).
- 55 distinct illustrations used in the recorded presentation.
- 580 actual carousel selections: all 130 observations plus the featured paths,
  at 1280×900, 1024×768, 390×844 and 320×568.
- No page overflow, illustration/dock overlap, missing image, or consecutive
  repeated image within the full recordings. Images use `contain`.
- 290 full viewport captures in [the review gallery](../../design/0.2/moment-review/index.html).
  No `scrollIntoView` is used to make a workflow screenshot pass.
- The diagram and navigation rectangles do not move between observations. The
  illustration area changes by at most 36 CSS pixels when a short caption wraps.
- 36 live selections checked against the current Bank workspace. The existing
  composition is awaiting buyer consent. These checks submit no ledger commands.
- 45 checks of authored chapters, reviewer/source/author surfaces, and the
  standalone export, including its evidence switch, pass at three screen sizes.
- All 28 edition checks pass. The served book matches all 396 source-file
  fingerprints; export links, keyboard navigation, no-JavaScript prose and print
  checks also pass.
- Six shared-scene tests and twelve reader tests pass, including missing evidence,
  expectation independence, purchase handoffs, and settlement versus rollback.

The live pass found an older fixed footer covering the dialogue; its height now
matches the action dock. The compact layout also gives branching diagrams enough
space on short phones. The standalone export uses the same viewport layout and
keeps evidence behind its header link. Keyboard navigation targets the actual
previous/next controls, without also advancing the focused carousel.

These are desktop Chrome checks at emulated viewport sizes. They do not substitute
for testing Safari on physical phones. Large text and source documents intentionally
remain scrollable. Illustrations clarify recorded events; they are not independent
evidence of ledger behavior, nor a claim of external deployment.

Raw results: [recordings](viewport-stories.json), [live inspection](viewport-live.json),
and [reader surfaces](viewport-readers.json). The earlier
[handcrafted review](HANDCRAFTED-REVIEW.md) describes reader.8 and is historical.
