# Architecture version 5 — show the composition mechanism

Status: rendered and inspected; user review pending.
Recorded: 2026-09-14 UTC. Baseline: `65a712c`.

---

## 1. What failed

Version 4 repeated the same Core-to-Application-A/B structure. Those names did
not explain what Harmonia adds, what constrains execution, or how composition
works. The user explicitly stopped image generation. This version uses authored
SVG and HTML, preserving the white, navy and pale-blue visual direction.

---

## 2. Architectural message and sources

The main subject is the source's contract model: an on-ledger workflow instance
assigns an actor and target action; applicability declarations identify eligible
application choices; application authority remains necessary. Results affect
workflow state, and continuation transfers outputs to another definition.

| Architectural fact | Original source | Picture treatment |
| --- | --- | --- |
| “without bespoke pairwise integration” | [Proposal, abstract][abstract] | Explain reusable composition in the prose, rather than inventing named applications. |
| Definition specifies allowed paths; instance holds state and party bindings | [Contract model, lines 339–373][state] | Definition, instance and assignment sit inside one Core boundary. |
| “actor : Party” and “target : templateId · choice” | [Contract model, lines 394–411][assignment] | Assigned step identifies its acting party and target choice. |
| Applicability carries template, choice, step kind and role | [Contract model, lines 426–433][binding] | Binding DAR shows its declaration contents, attached to the step/choice relationship. |
| “authority : required from app” | [Contract model, line 405][authority] | Application choice retains its controller requirement; step assignment cannot supply missing authority. |
| Applications retain signatories and observers | [Contract model, lines 435–453][app] | The choice is inside the source application's contract boundary. |
| “continuation feeds the next definition” | [Contract model, line 471][continuation] | Instance outputs reach a Core continuation and then another workflow. |
| Dapp submits transactions and reads state | [Component map, lines 295–298][dapp] | One small off-ledger interaction surface above Core. |
| Binding authored by the application or emitted by Builder | [Component map, lines 241–251][builder] | One subordinate build-time annotation feeds the Binding DAR. |

[abstract]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia.md#L13-L17
[state]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L339-L373
[assignment]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L394-L411
[binding]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L426-L433
[authority]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L405
[app]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L435-L453
[continuation]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L471
[dapp]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L295-L298
[builder]: https://github.com/miguelemosreverte/harmony-project/blob/65a712c/docs/proposal/harmonia-architecture.html#L241-L251

This remains a proposed logical architecture, not proof of what the current
code implements. It depicts choice-execution steps, not every supported step
kind. The arrows describe semantic relationships within Daml execution, not
network RPCs or mandatory separate transactions. A result can advance workflow
state in the same transaction where supported. Atomic execution depends on the
workflow structure and required authorization. Binding/import/interface-placement
questions remain open in the input documents.

---

## 3. Visual decisions

- One connected map replaces the repeated component maps.
- The centre of attention is assigned step → application choice, with the
  declaration that makes that choice eligible and the controller that authorizes it.
- The return path explicitly connects execution results to workflow state.
- Core encloses instance, assignment and continuation. The source application
  encloses its own contract and choice. Canton encloses all on-ledger elements.
- A separate, lower relationship connects outputs to another workflow, showing
  the continuation mechanism rather than a financing or transfer scenario.
- The Dapp and build tooling remain small supporting elements outside Canton.
- No Application A/B placeholders, plugs, gears, anonymous document icons,
  carousel, new buttons or generated image assets.
- Real text in SVG remains sharp under zoom. Source Markdown remains the source
  of the left explanation, rendered through the existing Markdown library.

---

## 4. Implementation and inspection

The source is [05-architecture.svg](05-architecture.svg), authored directly and
displayed inline in [v5.html](v5.html). No image-generation tool or raster asset
is involved. The original component and contract diagrams were reread before
the composition was written. Source data is summarized, not copied as a field
catalog. The assignment inset represents the instance's assigned work; it is not
a claim that the source's separate StepAssignment template is an embedded field.

The browser page reuses the established paired review layout and pan/zoom code.
`review-v5.css` sets the vector sheet's dimensions and print sizing. The existing
renderer now accepts an SVG source, retaining support for the earlier image
reviews. The left column is rendered from `read-v5.md` with actual Markdown
headings, emphasis and quotations.

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-review.py 5
open 'http://127.0.0.1:56202/book/navigation/explorations/architecture-structure/v5.html'
```

### Visual review

Inspected the actual browser screenshots, including 1440 × 900 desktop and
390 × 844 mobile. The desktop view fits the complete diagram and prose. Text and
connector hierarchy are visible; the declaration's dashed attachment no longer
crosses the action label. Application choice, instance result and continuation
are connected, with no repeated generic application map. Supporting labels use
24 SVG pixels, approximately 17.5 screen pixels at the inspected laptop fit.

Mobile provides a whole-sheet overview with pinch/zoom for details. The vector
text remains sharp when enlarged, but the full sheet's text is too small to read
at mobile fit. A separate mobile detail capture shows the application contract
and its controller requirement. This is a pan/zoom review canvas, not a completed
mobile presentation or a working backend demonstration.

### Checks

The browser run verified desktop and mobile fit, two interaction surfaces,
formatted Markdown, no body overflow, stable diagram DOM while panning, shared
camera restoration within 0.05 pixels, restored prose scroll, touch pinch,
Home-to-fit and one uncropped PDF page. No failed requests or browser exceptions
were recorded. SVG checks found no overlapping text bounds, no text outside the
sheet, and confirmed all 24 labels associated with a component remain inside
that component. Continuation is inside Core, and Choice is inside the source
application. There are no raster elements in the diagram.

Evidence is saved in `.artifacts/architecture-structure-v5-review/`:
`desktop-complete.png`, `laptop-complete.png`, `desktop-shared-camera.png`,
`mobile-complete.png`, `mobile-choice-detail.png`, `complete.pdf`, `checks.json`
and `native-geometry.json`. Existing Chrome and Python were reused.

Assistant assessment: this candidate explains the eligibility/authority
distinction and the state/continuation relationships that the previous generic
maps omitted. It still abstracts the complete contract catalog, package imports,
interface visibility and binding implementation. Its usefulness to the reader
remains a user-review question; rendering checks alone do not establish it.

The design and source citations were first committed and pushed at `972ff11`.

---

## 5. User review

Recorded 2026-09-14, after reviewing version 5 at `0d218ea`: Miguel asked for
simpler words and fewer words. Version 6 keeps the mechanism and layout while
shortening the labels and adjacent prose. This feedback is not final approval.
