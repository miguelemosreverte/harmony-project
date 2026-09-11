# UX contract — second pass

This document governs the book, its sandbox, the application mockups, and their
later production implementation. The design starts with the reader's task.
Baseline under review: `v0.2.0-design.1`. Original source files remain unchanged.

## Readers and their journeys

| Person | Question on arrival | Route | Successful exit | Intended feeling |
| --- | --- | --- | --- | --- |
| Maya, product explorer | What does this provide? | Product → Alice's purchase → try the story | Explain why separate applications need a shared workflow | Oriented, curious, unhurried |
| Luis, proposal author | Did my requirements reach the product? | Coverage → source passage → interpretation → evidence or open gap | Locate any original passage and distinguish quoted from implemented | Respected and able to audit |
| Erik, senior developer | How does the working system fit together? | Product → integration → evidence → original input and code | Follow one action across application, core, service, and reader | Confident without reverse engineering |
| Priya, investor or sponsor | Who benefits and what is proven? | Product → real use case → release/adoption status → proposal context | Separate working local evidence, remaining scope, and external adoption | Informed without inflated claims |
| Nina, workflow operator | What am I supposed to do next? | Workspace → current actor's task → observed outcome → handoff | Complete or understand a refusal without searching the book | Calm and in control |

These names describe reading perspectives. In the use cases, retain the existing
example characters: Alice (buyer), Northbank (financing), Ben (buyer's agent),
Sofia (seller's agent), and the four transfer parties. Do not collapse the actual
four-party purchase into a misleading three-person ledger story.

## Screen contracts

| Screen | Primary goal and action | Secondary goal and object | Information revealed now | Deferred information | Exit / next action |
| --- | --- | --- | --- | --- | --- |
| Welcome | Choose why I am here; Continue | Change perspective using one radio group | One-sentence product value and five reader intentions | Chapters, metrics, code, provenance | One recommended route with a visible first stop |
| Route overview | Begin the first relevant chapter | View all chapters in a disclosure | Three or four ordered stops and why each matters | Unrelated audiences' tasks | Start this route |
| Product chapter | Understand the user's problem | Meet the people in the example | Problem, outcome, ownership, narrow boundary | Architecture detail and funding terms | Follow Alice's purchase |
| Use-case reading | Follow one person's goal through a narrative | Open the source claim via a citation | Characters, trigger, handoff, consequence | Full expected/actual objects | Try this example |
| Use-case sandbox | Advance one actual recorded action or choose a refusal scenario | Inspect input, expected, observed, or provenance on demand | Current actor, attempted action, observed outcome, next step | All unrelated receipts and stories | Back/Next step; return to reading |
| Source review | Read the original passage correctly | Inspect exact source text in a separate disclosure | Proper paragraphs, lists, tables, original diagram, source location | Raw Markdown, PlantUML, HTML tokens | Return to the chapter's explanation |
| Coverage | Locate a requirement and its destination | Inspect counting method and raw report on demand | Two source totals and chapter destinations | The full claim/evidence table and business appendices | Open the relevant source passage |
| Application workspace | Do the current actor's next task | Inspect history or source application in tabs | Who acts, what they do, why it is enabled, resulting owner | Testing controls and implementation internals | Advance or show a refusal with recovery |
| Builder | Inspect one input and choose its integration path | Read supported shapes after a refusal | Current stage, relevant source choice, next action | Future stages' fields and compiler internals | Select → inspect → map → build boundary |
| Release/adoption | Understand readiness | Open original funding/maintenance context | What works locally, what is pending, external evidence status | Full financial and historical reference text | Return to the relevant product story |

Each page identifies its location, purpose, and one primary next action. Secondary
actions are ordinary links or disclosures. Do not place two competing versions of
the same chapter in the navigation. Read, Try, Evidence, and Sources are modes of
one chapter, not disconnected destinations.

## Navigation and language

- Ask “What brings you here?” once on entry. A direct deep link remains usable
  without answering. An explicit “Change reading path” always returns to the chooser.
- Show only the chosen route's stops in the main rail; keep all chapters available
  in a secondary disclosure. A visible route label explains the current perspective.
- A chapter ends with one named next stop. “Back to reading” remains adjacent to
  sandbox controls. Never make the reader guess whether a button changes a chapter,
  advances a workflow, opens evidence, or submits a command.
- Prefer product language in the primary content. Build history and implementation
  status live in evidence/disclosures. Keep simulation/recorded/live labels explicit.
- Show the consequence immediately after an action. A refused action preserves
  progress and explains what is missing. The selected actor is not authentication.
- Keep comparison images and design review tools out of the reader's main route.

## Color, type, spacing, and accessibility

The first pass's olive brand accent was an assistant design choice. The original
architecture uses `#0b0f14` and `#111823` backgrounds, cyan `#4cc9f0` for off-ledger,
green `#4ade80` for on-ledger, and amber `#f0a04c` for external systems. Those colors
describe component roles; they do not require green buttons throughout the book.

- Default to a neutral light surface and a restrained blue action color. Offer a
  dark appearance derived from the original architecture and a warm paper option.
- Use green for an explicitly successful/ledger state, with a text label as well.
- Place Appearance and Share controls at the top right. Appearance contains theme
  and text size; do not show six equally prominent switches on every screen.
- Provide compact, standard, and large text sizes. Keep narrative lines near
  65–72 characters, body text at least 16px, and touch targets at least 44px.
- Use a modest type hierarchy, consistent spacing, visible focus, labelled controls,
  semantic headings/tables, and keyboard-operable tabs and disclosures.
- Reflow at narrow widths and browser zoom. Scroll wide diagrams/tables locally,
  with an explicit caption; never make the whole page overflow horizontally.
- Printing produces a light, readable document with the narrative, current recorded
  step, and source passages. Navigation and transient controls do not print.

## URL is the view contract

Shared URLs reproduce semantic state from the URL alone. Do not let localStorage,
an earlier visit, random state, or an in-memory step counter override a shared URL.

| Parameter | Meaning |
| --- | --- |
| `audience` | explorer, author, developer, investor, operator |
| `theme` | light, dark, paper |
| `text` | compact, standard, large |
| `view` | Chapter/workspace mode, validated for that screen |
| `story` | One of the chapter's supported recorded scenarios |
| `step` | Bounded recorded action or workspace progress index |
| `actor` | Illustrated perspective, validated against the selected story/workspace |
| `tab` | Selected evidence inspector tab |
| `open` | Named source/disclosure sections currently expanded |
| `panel`, `nav` | Appearance panel and narrow-screen navigation state |
| `state` | Workspace sample ready/pending/refused/stale/disconnected/rejected state |
| `package`, `phase` | Builder sample and current stage |

Canonicalize invalid, repeated, unknown, or out-of-range values to documented
defaults, visibly retaining usable content. Preserve audience/theme/text across
navigation, reset unrelated per-screen state, and give meaningful interactions
browser Back/Forward behavior. Share copies the canonical absolute URL with an
accessible fallback. A source deep link opens its containing disclosure. Legacy
chapter links lead to the single canonical chapter.

Viewport size, pointer hover, keyboard focus, and ordinary scroll position are
environment/transient state. The same URL preserves selected content and controls;
responsive layout can differ between devices. No credentials go into URLs.

## Evidence and render integrity

- Use a real Markdown parser for readable source rendering. Raw original lines
  remain available separately for exact quotation auditing; do not display Markdown
  markers or diagram program text as ordinary prose.
- Preserve original HTML diagrams as diagrams. Use labelled companion drawings
  where useful; do not claim the missing original BPMN/SVG attachments were recovered.
- Keep all 736 original quotation units and their fingerprints. Quotation coverage
  remains independent of runtime coverage and editorial quality.
- The sandbox should inspect actual exported ledger recordings where available,
  including failed actions. Label these as recorded runs, not new live submissions.
  A mock workspace remains explicitly a simulation until wired to the real service.
- The static edition must remain readable without JavaScript. The interactive
  edition adds exploration. Print/PDF cannot be the only way to understand a story.

## Implementation and acceptance sequence

- [x] 1. Record the reproduced problems and these screen/reader contracts.
- [x] 2. Replace accumulated style overrides with explicit tokens and components;
  add the URL state model, shared appearance controls, and persona routes.
- [x] 3. Render source Markdown correctly, retain exact quotation audits, restore
  original diagrams, and establish one canonical route per chapter.
- [x] 4. Rewrite chapters around the existing characters and use cases; connect the
  sandbox to preserved recorded input/expected/actual data with provenance.
- [x] 5. Make workspace and builder mock state reproducible, with visible next
  actions, recovery, Back/Forward, and reset behavior.
- [x] 6. Verify rendering, navigation, and evidence independently: source-render
  assertions; cold URL replay and browser history; screenshots at narrow, intermediate,
  and desktop widths, dark/light/paper, larger text, expanded sources, and print.
- [x] 7. Record real results, retain the first design checkpoint, commit the second
  pass, and provide one obvious entry URL.

Acceptance requires more than no horizontal overflow. Inspect actual paragraphs,
table structure, diagram labels, intended control visibility, focus behavior,
direct links, failed actions, and print output. Keep a log of observed fixes and
remaining limitations; do not claim an absence of all possible bugs.

Acceptance evidence: [second-pass verification](verification-2.md),
[browser results](browser-results-2.json), and [captured screens](../../design/0.2/review.html).
