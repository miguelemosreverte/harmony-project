# A continuous, visual reader

The infographic carries the explanation. A reader turns one frame at a time,
with two familiar directions and no moving controls. The four reading paths
remain distinct; exact original quotations and recorded observations remain intact.

## Implementation sequence

- [ ] Keep the shared scene objects alive during updates.
  - Reconcile diagram cards and measured arrows by identity.
  - Paint arrows with their cards, without a blank intermediate frame.
- [ ] Keep the book shell alive during navigation.
  - Load local chapters in the current document; preserve native links as fallback.
  - Give each mounted page an explicit lifetime and clean up its listeners.
  - Preserve query addresses, browser history, modified clicks and print output.
- [ ] Make navigation a carousel edge, in a fixed place.
  - Two consistent previous/next targets; keyboard and horizontal swipe do the same work.
  - Keep the binary choice cards themselves clickable; add no selector panel.
- [ ] Let the diagrams speak.
  - Remove repeated scene mechanisms and lists that restate every diagram node.
  - Replace the author's reloading companion with the shared renderer.
  - Keep supplied drawings in the original archive; explain them with our infographics.
  - Keep original quotations and explicit evidence limitations readable.
- [ ] Sort displayed JSON object keys recursively; retain array order and values.
- [ ] Apply stable navigation and action placement to the separate live application.
- [ ] Verify desktop and mobile: screenshots, control positions, retained DOM,
  complete routes, history, keyboard, touch, source fidelity and live actions.
- [ ] Commit and push the verified delivery.

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
