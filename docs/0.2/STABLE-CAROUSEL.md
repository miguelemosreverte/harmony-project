# Stable workflows and direct navigation

This revision follows the request for a visible bottom carousel. It supersedes the earlier prohibition on clickable progress markers: a workflow's named steps and branch paths now belong together in that carousel. Root audience choices remain separate reading entrances.

## Design contract

- Keep the infographic mounted when inspecting another step or outcome of the same workflow.
- Put direct step selection, progress, and explicit alternative paths in the bottom dock. Keep the previous and next edges at fixed coordinates.
- Use blue for the selected step, completed marks for preceding observations, amber for refusal, and neutral outlines for unselected steps. Recorded playback is not live execution.
- Each stop must reveal a particular actor, action, object, or refusal. Never manufacture a ledger state change to make a verification attempt look more dramatic.
- Give every actual control hover, focus, and pressed feedback without translating or resizing it. Passive diagrams must not pretend to be clickable.
- Preserve `story` and `step` in shared URLs, including alternate outcomes, browser history, and direct loads.
- Interpret the requested 50-pixel budget as positional drift of persistent landmarks within a workflow. Measure it separately from the fraction of screenshot pixels that change. A meaningful state change should change pixels; it should not rearrange the stage.

## Implementation sequence

- [ ] Shared presentation (`product/scene`)
  - [ ] Typed carousel entries and paths; retained controls; shared progress and hover styles.
  - [ ] Visible actor focus and observed-event treatment; fixed custody artifact positions.
- [ ] Recorded reader (`book/browser`, `design/0.2/book.js`)
  - [ ] Direct access to each recorded observation, with informative labels.
  - [ ] Approved and refused paths on the same stage; remove outcome-card detours.
  - [ ] Show attempted actions inside diagrams, including unchanged or rejected observations.
- [ ] Reading routes (`design/0.2/reader`, edition builders)
  - [ ] Direct carousel navigation through reviewer relationships and original passages.
  - [ ] Preserve old outcome URLs and reduce repeated action captions.
- [ ] Live workflows (`product/web`)
  - [ ] Use the same carousel for inspecting live handoffs and composition steps.
  - [ ] Keep ledger actions explicit and authorized; inspection never submits a command.
  - [ ] Keep the draft's diagram visible while editing, with direct question navigation.
- [ ] Verification and delivery
  - [ ] Desktop and mobile: direct jumps, branches, back/forward, hover, stable landmarks, and screenshot changes.
  - [ ] All recorded observations remain accessible; original sources and expectations stay pinned.
  - [ ] Compile and run relevant checks with one build JVM; publish the checked revision.

## What each featured stop communicates

| Story | Stop | Visible information |
| --- | --- | --- |
| Purchase | Start | Bank holds the private financing application. |
| Purchase | Bank decision | Approval becomes usable, or the decision is refused. |
| Purchase | Proposal | Alice uses the financing result to create the offer. |
| Purchase | Relay | Ben carries the proposal onward; his connector changes. |
| Purchase | Receipt | Sofia receives the proposal; the path is complete. |
| Transfer | Agreement | Seller accepts, with quantities unchanged. |
| Transfer | Lock | Available quantity moves into the reserved quantity. |
| Transfer | Source and destination preparation | The acting custodian is highlighted and its observation is shown. |
| Transfer | Settlement | Receipt changes the destination quantity, or refusal leaves the source lock visible. |
| Evidence | Every recorded attempt | Actor, attempted operation, and observed outcome are visible on the stage; exact sorted comparisons remain below. |

The same diagram may represent several verification attempts. A fixed observation strip identifies what was tested; a highlighted actor and outcome mark distinguish the attempt without changing the underlying facts.
