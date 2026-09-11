# Infographic and live integration

This increment replaces repeated explanation and button-driven simulation with a
scene the reader can follow. The generated [visual reference](../../design/0.2/infographic/concept.png)
and [prompt](../../design/0.2/infographic/prompt.md) establish the composition.
The final scene is responsive HTML/CSS, rendered by a small typed Scala.js component.
It is not a bitmap background.

## Experience contract

- Show who owns each application, the artifact being handed off, and the observed
  consequence. One caption explains the current moment. Do not restate it in a
  heading, status panel, paragraph, and timeline simultaneously.
- Purchase: private documents stay in financing; approval enables Alice's proposal,
  Ben relays it, and Sofia receives it. Receipt is the endpoint, not a property sale.
- Transfer: agreement and preparation precede a bounded final settlement. Locked
  quantities and rollback come from the recorded observations.
- Present the normal business journey first. Deliberate unauthorized probes remain
  in the evidence inspector. A refusal scenario is an intentional alternative.
- Use a swipeable, keyboard-operable carousel with a named current moment, small
  position controls, and no automatic advance. Respect reduced motion.
- Keep source quotations and raw expected/observed values in one evidence area.
  Preserve the existing quotation denominator and exact audit text.
- Live scenes expose actual eligible operations, never disabled buttons inviting
  the wrong participant to act. A waiting scene names whose responsibility comes
  next. Session credentials determine authority; visual exploration does not.
- Shareable URLs preserve the reading selection. Live links preserve the selected
  screen and appearance, not credentials or a frozen promise about mutable state.

## Implementation sequence and owners

- [x] 1. Establish this contract and preserve the generated reference and prompt.
- [x] 2. Add a small reusable scene renderer under `product/scene/`.
  - [x] Typed scene data and semantic HTML; CSS owns responsive geometry and motion.
  - [x] The product browser and exported field guide use the renderer, never the reverse.
  - [x] A narrow exported renderer lets the static field guide use the same scene.
- [ ] 3. Replace the book's repetitive use-case view with an infographic carousel.
  - [ ] `book/edition-0.2/pages.py`, `chapters/`, and `design/0.2/book.js` own the
    narrative and recorded progression; shared scene code only draws supplied data.
  - [ ] Preserve URL replay, original sources, static/print reading, and refusals.
  - [ ] `book/export/` packages the designed guide and injects validated recordings
    from the requested run; the existing detailed laboratory remains reachable.
- [ ] 4. Integrate the scene into the actual Scala.js product browser.
  - [ ] `product/web/.../financing/` maps `FinancingState` to the scene and submits
    the existing typed commands through `LiveApp` and `LiveApi`.
  - [ ] `product/web/.../live/` uses one task surface, URL navigation, and secondary
    composer/package/history views without discarding drafts during polling.
  - [ ] `product/server/.../http/` serves the actual compiled assets and an optional
    exported book. Book mounting is a file boundary, not a product dependency on harness.
  - [ ] The harness provisions the local evaluation and private session launcher.
- [ ] 5. Prove the integration with real observations and handoffs.
  - [ ] Compile and test changed Scala projects sequentially with bounded heaps.
  - [ ] Exercise actual HTTP/ledger commands, actor boundaries, observed completion,
    refresh, and browser state. Preserve independent golden expectations.
  - [ ] Review desktop/mobile screenshots, carousel gestures, source rendering,
    offline export, and print. Record exactly which paths are live or recorded.
  - [ ] Commit the verified slice and provide the working entry point.

## Existing backend scope

The service already supports the private financing handoff, bounded composition,
and package operations. This increment connects to those commands. The four-person
purchase and four-party transfer remain recorded examples until their live service
operations are implemented; a visual scene cannot manufacture those operations.
No grant, release, independent-adopter, or external-adoption milestone is silently
accepted by this UI integration.

No new ledger contract semantics are required for the existing live handoff.
Use one owned Canton environment at a time, bounded JVM heaps, and sequential builds.
