# UX contract: one purpose, two possible actions

The approved infographic sets the visual and interaction standard. Every page
answers one question, presents the information needed to answer it, and offers
at most two interactable things. Links, inputs, disclosures and diagram nodes
count. The whole repository file tree is the single explicit composite exception.
A panel containing many controls does not satisfy this rule.

The [screen contracts and implementation plan](QUIET-READER.md) and
[page walkthrough](WALKTHROUGH.md) describe the current design. Earlier design and
reader reports are historical checkpoints, not permission to restore their controls.

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
- Prose, citations, diagrams, progress counts and line numbers are passive.
  No tabs, drawers, selector collections, clickable dots, hover menus or node actions.
- Give the public story one useful caption per observation. A short evidence limit
  follows the scene; do not repeat its mechanism at every step.
- Keep application interfaces one question at a time. A text field and Continue
  use the entire budget; a choice page offers two direct alternatives.
- A review page shows the complete proposed plan before submission. Ledger
  consent and observed execution remain separate steps.
- A connection failure receives its own recovery state. Further tasks return only
  after the connection is restored. An absent session asks for its provisioned link.

## Visual and responsive rules

Use the approved bank, document, house and character assets, quiet neutral panels,
blue progression, generous gaps, and measured arrows. Blue indicates observed
completion; refused, skipped and pending states have different labels and styles.
Never infer a completed action from a submitted request or its golden expectation.

At a narrow width, stack choice cards and code/document work areas. Keep diagrams
legible by reflowing their nodes; arrows must terminate outside portraits and cards.
Code and original-document panes may scroll natively. They do not gain extra controls.
Original drawings remain in the source archive. Explanatory diagrams are authored
in the shared infographic language; do not embed the supplied SVG styling.

Native links and forms support the keyboard. Every carousel supports arrow keys and
horizontal touch swipes. Previous and next stay in fixed viewport positions.
Chapter turns retain the shell; diagrams retain their cards and arrows. Binary
decisions use two directly clickable cards, not ambiguous previous/next arrows. Text remains selectable and annotated source retains its
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
