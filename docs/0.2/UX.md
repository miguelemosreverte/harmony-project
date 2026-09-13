# UX contract: stable infographics and direct paths

The approved infographic sets the visual standard. Each page answers one question.
Its workflow stays on screen while a bottom graph shows connected stops,
progress, and alternative outcomes. Names appear on hover or keyboard focus. Every step must communicate a visible change
or a specific observed attempt. Selection never substitutes for ledger execution.

The [compact graph contract](GRAPH-NAVIGATION.md) refines the
[stable carousel contract](STABLE-CAROUSEL.md), which supersedes the earlier ban on
clickable progress markers. Outside the workflow dock, keep at most two primary
interactions. The repository file tree remains one composite interaction. This is
not permission to add toolbars, selector collections, or control drawers.

## Four reading paths

| Reader | Goal | Primary material | Secondary material | Intended feeling |
| --- | --- | --- | --- | --- |
| Product user or technical investor | Explain the value and mechanism | People and application handoffs | Daml choices, Canton authority, observed outcomes and limits | Oriented, with a concrete example to follow |
| Developer | Understand the implementation | Actual source beside a file tree | Module purpose, source identity and passive slice diagram | Able to read without reverse engineering |
| Reviewer | Challenge agreement with the design | One manifest, reading or execution diagram | Cited source identities, recorded evidence and explicit gaps | Able to disagree precisely |
| Original author | Follow the supplied requirements | Highlighted original passage | Reused explanation or diagram beside it | Respected, with visible traceability |

Entry uses two decisions: Understand/Verify, then Product/Code or
Architecture/Original documents. There are no audience selectors after entry.
A deep link opens its selected material directly.

## Page rules

- Lead with a question or a concrete conclusion. Keep the primary action beside
  the relevant scene or above a long work area.
- Name the next destination: a scene, outcome, chapter, passage, or transaction.
  Previous/Next never wraps automatically. The final item says where it ends.
- A deliberate return to the entrance is an exit, not an automatic playback cycle.
- Prose, citations, diagram objects and line numbers are passive. Actual controls
  visibly respond to hover, keyboard focus, and pressing without moving.
- Connected circles are directly selectable, with 44px touch targets and accessible
  names. Paths share their common beginning and fork from left to right. A ring
  identifies the inspected stop; color and endpoint symbols identify outcomes.
- Workflow branches belong in the dock. They never replace the infographic with
  large choice cards. Root audience entrances remain separate from workflows.
- The featured infographic summarizes who acts, what changes, and what stays
  private. Keep the approved infographic intact; add a separate illustrated
  exchange below it, as specified in [Workflow support](WORKFLOW-SUPPORT.md). Keep application boundaries,
  document names, and necessary quantities. Avoid duplicate captions, counters,
  path labels, or revision footers. Recorded context stays in the header; exact
  run provenance stays in evidence.
- Keep application editing to one question at a time, with the plan visible.
  Questions can be reached directly in the carousel; answer alternatives appear
  in its branch row. Submission remains an explicit action after review.
- A review page shows the complete proposed plan before submission. Ledger
  consent and observed execution remain separate steps.
- A connection failure receives its own recovery state. Further tasks return only
  after the connection is restored. An absent session asks for its provisioned link.

## Visual and responsive rules

Use the approved bank, document, house and character assets, quiet neutral panels,
blue progression, generous gaps, and measured arrows. Blue emphasizes the selected
stop and completed marks identify preceding observations. Refused, skipped and
pending states have different labels and styles.
Never infer a completed action from a submitted request or its golden expectation.

At a narrow width, stack choice cards and code/document work areas. Keep diagrams
legible by reflowing their nodes; arrows must terminate outside portraits and cards.
Code and original-document panes may scroll natively. They do not gain extra controls.
Original drawings remain in the source archive. Explanatory diagrams are authored
in the shared infographic language; do not embed the supplied SVG styling.

Native links and forms support the keyboard. Every carousel supports arrow keys and
horizontal touch swipes. Previous and next stay in fixed viewport positions.
Chapter turns retain the shell; diagrams retain their cards and arrows. Branches
stay visible in the dock. On mobile the rail scrolls horizontally; selecting a
step brings it into view without moving the diagram or the navigation edges. Text remains selectable and annotated source retains its
exact original bytes. Appearance can be specified through `theme=light|dark|paper`
and `text=compact|standard|large`; these do not add a toolbar.

## Addresses and truthfulness

The URL identifies the page and selected file/line, passage, relationship or recorded
moment. Back/Forward and a cold shared URL restore that selection. A composition
address also preserves its unsubmitted typed plan and current question. A package
address identifies its source step or inspected package.

A live URL shows the receiving participant's currently visible ledger state. It
cannot freeze mutable state or grant another participant's permissions. Capability
links are consumed into tab-local storage and removed from the ordinary address.

Label the distinction between live execution, historical recording, authored
explanation and original quotation. The 736/736 quotation-unit count measures exact
inclusion; it is not a claim that all proposed features or external adoption exist.

## Ownership and evidence

The product has no book routes, chapter links or book configuration. Product
ScalaDoc uses generic module responsibility; the book owns its reading maps.
The development launcher runs a separate book host and product host.

Review screenshots at desktop and mobile sizes, traverse the actual choices,
check interaction counts including embedded content, and verify real commands on
one disposable network. Record the purpose and destination of each screen in
Markdown. A screenshot is visual evidence, not proof of ledger behavior; a green
unit test is not a visual review.

The [continuous reader implementation](CONTINUOUS-READER.md) records page lifetimes,
source fidelity, comparison ordering and the visual stability checks. JSON objects
use consistent recursive key ordering; arrays preserve their observed order.

The positional budget within one workflow is at most 50 CSS pixels for persistent
landmarks. Screenshot changed area is measured separately: state changes should
be noticeable, while stage geometry stays stable. The browser checks and pixel
comparison in `design/0.2/checks/stable-carousel.mjs` and `carousel-pixels.py`
record both quantities. Old two-arrow-only interaction reports are historical.
