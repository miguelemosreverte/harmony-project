# Harmonia architecture — structure before detail

Status: one generated candidate prepared for joint review; known connector ambiguity recorded below.
Recorded: 2026-09-14 UTC.
Baseline: `3590bee` on `design/architecture-clarity-v2`.

---

## 1. What this review is about

Miguel's content direction:

> Right now, the colors, this font size, and so on that you're using look pretty good, but that doesn't mean that the content is good.

The previous candidate used financing and transfer examples where architectural
structure should have been. This candidate follows the original component map
and contract model. Both views appear together in one image and one browser tab.
White, pale blue, navy text and restrained dimensional icons remain the visual
language. The content hierarchy changes.

The desired reading is: identify the ledger boundary and major responsibilities,
understand how applications participate, then inspect the contract relationships.
These are design objectives, not a claim of measured reader comprehension.

Miguel requested a sectioned Markdown proposal leading to an exact prompt, a
single generated candidate, and a joint review. The sections below are stages of
the design document prepared in one working session, not separate meetings.

---

## 2. What the originals establish

The original supplied HTML contains two diagrams. These short exact quotations
anchor the presentation to that document. Links are pinned to the baseline.

| Architectural fact | Original wording | Source |
| --- | --- | --- |
| Dapp is the interaction surface | “interaction surface · not the orchestration layer” | [§1, line 132][dapp] |
| Workflow state is on-ledger | “composition state stays on-ledger” | [§1, line 202][ledger] |
| Core owns the workflow model | “workflow state”; “persisted · queryable”; “step-exec rules”; “actor + authority” | [§1, lines 215–219][core] |
| Builder operates at build time | “internal tooling · BUILD TIME ONLY”; “output → Binding DAR + generated project/code” | [§1, lines 135–149][builder] |
| Binding declares participation | “which step kind a choice fulfils” | [§1, lines 230–250][binding] |
| Two participation paths share the model | “both paths → one Core-managed model” | [§1, line 251][paths] |
| Applications keep authority and visibility | “own signatories”; “own observers”; “authorization preserved”; “privacy preserved” | [§2, lines 435–453][app] |
| Assignment requires application authority | “authority : required from app” | [§2, line 405][assignment] |
| Continuation links definitions through outputs | “continuation feeds the next definition” | [§2, lines 470–471][continuation] |
| Atomic execution is bounded and conditional | “bounded atomic execution block”; “collapse steps when authorizations co-exist” | [§1, lines 224–225][atomic] |
| Some packaging decisions remain open | “Package-import direction”; “Binding mechanism”; “Interfaces: core vs Binding DAR” | [§6, lines 569–575][open] |

[dapp]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L132
[ledger]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L202
[core]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L205-L228
[builder]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L135-L149
[binding]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L230-L250
[paths]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L251
[app]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L435-L453
[assignment]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L394-L411
[continuation]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L470-L471
[atomic]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L224-L225
[open]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia-architecture.html#L569-L575

The Markdown proposal qualifies atomic execution further: workflow structure and
required authorizations must allow it ([Core section, lines 66–74][atomic-proposal]).

[atomic-proposal]: https://github.com/miguelemosreverte/harmony-project/blob/3590bee/docs/proposal/harmonia.md#L66-L74

---

## 3. Select the architectural information

| Original element | Treatment in this image |
| --- | --- |
| Dapp, Core and source applications | Main component relationships, each with a short responsibility label. |
| Canton Validator / Super Validator | Explicit external host of the on-ledger Daml boundary. |
| Binding DAR | A document artifact describing participation, visually distinct from an application service. |
| Builder | Smaller build-time tool; its generated artifact is explicit. |
| Two source application paths | Two compact packages: direct implementation and existing DAR with generated binding. |
| Package Manager | Small external DAR-source annotation beside Builder. Metadata retrieval remains in the reference document. |
| Users and DAR providers | Dapp and Builder entry points imply these roles; no avatar row. |
| harmonia-references | A quiet reference-package note. Its existence is architectural context; its use-case walkthroughs remain in the user branch. |
| WorkflowDefinition / WorkflowInstance | Definition creates instances; an instance owns state and role bindings. |
| StepAssignment / HarmoniaStepAction / AppTemplate | Assigned actor and target, interface relation and application choice; show ownership boundaries. |
| RoleBinding | Instance data: role → party, not another executable step. |
| AppliesTo | A secondary applicability annotation linking the interface and eligible choice. |
| Continuation | A secondary Instance → Continuation → Next definition relationship. |
| Atomic blocks | Conditional capability of Core; no transfer story and no blanket atomicity claim. |
| Template fields and complete choice catalogs | Retained in the source reference; omit from the overview. |
| Scope exclusions and unresolved technical details | Keep the full list in the source; a short open-design footer prevents false certainty. |

Friendly labels in the picture map to the names above. The original says its
template and choice names are illustrative. Neither this image nor the component
connections assert final package import direction or current implementation coverage.

---

## 4. Give the image a hierarchy

**Upper view: Components and boundaries.** This occupies the larger share of the
sheet. Dapp and Builder sit above a clear Canton/Daml enclosure. The main visual
weight goes to Core and application packages inside it. Binding is a declaration
between responsibilities, rather than a large runtime gateway. Build-time
connections use thin dashed lines; component relationships are labeled.

**Lower view: Contract relationships.** Smaller, but readable and visible from the
start. The main line shows definition, instance, assignment, interface and an
application choice. The application boundary carries its own authority and
visibility. Role binding is instance data. Continuation and applicability are
supporting relationships positioned away from the main line.

**Visual limits.** One title, two view headings, no slogan, no introductory
paragraphs inside the image, no numbered carousel, no use-case names, no people,
no bank or house illustrations. Icons identify architectural roles. Most nodes
have a title and at most two short supporting lines. Relationship labels explain
connections instead of surrounding the diagram with prose.

---

## 5. Exact image prompt

The following block is the complete prompt sent to the built-in image generation
tool. References are the earlier palette/style image, then the two original
diagram captures. The first reference supplies style only; its example imagery
and content layout are not the proposed composition.

```text
Use case: infographic-diagram.
Create ONE complete landscape Harmonia architecture presentation, approximately 1800 × 1200. This is an architectural STRUCTURE diagram for senior engineers. Its two complementary views must be visible together. Preserve the approved white/pale-blue/navy visual language and readable sans-serif typography. Soft dimensional document, package and application pictograms are welcome; use subtle shadows and generous whitespace.

Input image 1 is STYLE ONLY. Input images 2 and 3 are the factual original component map and contract model. Use their responsibilities and relationships, simplified by priority. Do not copy their dense fields, tiny lettering or dark palette. Do not reproduce the previous four-panel composition. No financing, offers, transfers, example scenarios, people, bank buildings, houses, carousel steps, buttons or marketing slogan.

One modest title at the top: "Harmonia architecture".
Two sections on the SAME sheet. The upper component view is dominant, about 60% of the diagram area. The lower contract view is about 40%. Avoid repeating the same statement as a title, subtitle and caption. Use ONLY the concise labels specified below.

UPPER VIEW heading: "Components & boundaries".
At the top, OUTSIDE the ledger, a browser/app pictogram labeled "Dapp", supporting line "Inspect · compose · submit". To its right, a smaller tool labeled "Builder", supporting line "Build time only". A quiet external note beside Builder reads "DAR sources: upload · Package Manager · Validator". This describes alternative inputs, not required runtime services.
Below them, one large pale-blue enclosure labeled "On-ledger · Daml", with the smaller host label "Canton Validator / Super Validator".
Inside the enclosure, left: a prominent workflow package labeled "Harmonia Core", supporting lines "Workflow state · step rules" and "Bounded atomicity, when authorized". Right: an application area labeled "Application DARs", with one shared supporting line "Own authority · ownership · visibility". It contains two compact package glyphs: "Direct" with "New DAR · implements interfaces"; and "Generated binding" with "Existing DAR · unchanged". Use abstract software packages, not domain-specific buildings.
Between Core and the application area, a SMALL flat declaration/document pictogram labeled "Binding DAR", supporting line "Application participation". It must look like a package declaration, not a server or third runtime application. Connect it to Core with a simple relation line labeled "declares participation". Connect Direct to the declaration with a relation labeled "implements" and connect the declaration to the existing-DAR route with a relation labeled "binds". These are architectural relations, not a resolved package-import graph; simple lines without directional arrowheads are acceptable here.
Connect Dapp to Core across the ledger boundary with two opposing blue arrows or a clear bidirectional connector labeled "Ledger API" and small supporting words "Transactions / state". Connect Builder DOWN to Binding DAR with a thinner dashed blue arrow labeled "Generates binding + code". Keep build-time and runtime connectors separate, and clear of all node names and boundary labels.
Small unobtrusive note at the bottom of this view: "harmonia-references · reference packages". Small source credit: "Original architecture §1".

LOWER VIEW heading: "Contract relationships".
Use a clear left-to-right line with these nodes: "Definition" → "Instance" → "Assigned step" → a small socket "Step interface" → "Application choice". Label the first arrow "creates", the second "assigns", and the assignment-to-interface relation "exercises via". This line explains the source's choice-exercise relationship, not every supported step kind.
Enclose Definition, Instance and Assigned step in a subtle shared area titled "Core workflow model". Definition has the supporting line "Flow rules". Instance has two short INTERNAL lines "State · outputs" and "Role → Party". Assigned step has the line "Actor · target".
Enclose Application choice in a SEPARATE pale-blue application area titled "Application". Inside this same area, put a small key beside the short words "App authority required" and one quiet line "Own signatories · observers". The execution arrow must reach APPLICATION CHOICE, never the authority key. No authority transfer from Core is implied.
Below Instance, ONE uninterrupted thin arrow starts at the bottom border of its card and reaches a smaller document labeled "Continuation", then reaches "Next definition". Role → Party stays inside Instance, never on this connector.
Below Step interface, a small secondary note labeled "Applicability", with one line "Step kind · choice · role" and smaller attribution "Binding DAR". Thin dotted annotation lines connect this note to the interface and to the application choice; it is declaration data, not an executed step. Leave clear separation from all solid execution and continuation arrows.
Small source credit: "Original architecture §2 · illustrative contract model".

At the bottom of the complete sheet, one understated line: "Still open: package imports · binding mechanism · interface placement".
Use clear hierarchy and generous connector spacing. Do not add introductory sentences, field lists, a legend panel, extra examples, a fifth core component, decorative graphics or other visible prose. The ledger boundary and component responsibilities should be graspable before the smaller contract relationships. Preserve the meaning of the original documents while reducing simultaneous detail. All text must be legible and uncropped. This image presents the original proposal and does not certify implementation status.
```

Reference paths, in input order:

1. `book/navigation/explorations/architecture-v2/01-ledger-overview.png` — style.
2. `.artifacts/original-diagrams/component-map.png` — factual structure.
3. `.artifacts/original-diagrams/contract-model.png` — factual structure.

---

## 6. Generated candidate

- Tool: built-in image generation; one call, no image edits.
- Exact prompt: section 5 above, extracted verbatim from its fenced block.
- Started: 2026-09-14 05:46:15 UTC.
- Returned: 2026-09-14 05:47:57 UTC.
- Artifact: [01-structure.png](01-structure.png), 1536 × 1024, 1,465,388 bytes.
- Browser presentation: [index.html](index.html), using the existing book pan/zoom viewer.
- Original output: `~/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/exec-a504dea0-baf3-42f8-b8bb-414c8dc7577a.png`.
- SHA-256: `32dcc1e43455a5e8ac9fd0ae3742a53a12763524499f822cc3dbe25a19ff3de5`.

The source evidence and exact prompt were committed before generation at
[d2a24e7](https://github.com/miguelemosreverte/harmony-project/commit/d2a24e7).
The image, browser presentation and assistant inspection were first committed at
[eca791d](https://github.com/miguelemosreverte/harmony-project/commit/eca791d4e93df0d19873d8ceaab5df40892c80f5),
2026-09-14 05:49:36 UTC, and pushed to `design/architecture-clarity-v2`.

---

## 7. Assistant inspection

**What improved:** the component map takes priority and the contract model sits
below it. The two source views stay visible together. The generated image has no
financing or transfer story and no four-way product layout. Runtime and build-time
connectors differ visibly. Instance data, continuation and the application choice
are distinct, and the execution line reaches the choice rather than the key.

**Known architectural ambiguity:** the upper connector passes through the Direct
package before reaching the existing-DAR package. This can suggest that one route
depends on the other. The intended relation has two branches originating at Binding
DAR: direct implementation and generated binding. This generated connection needs
correction before the image could be adopted as an architectural reference.

**Further review points:** Binding DAR still has substantial visual weight for a
declaration artifact. The Core atomicity caption mentions authorization but should
also retain the proposal's condition about compatible workflow structure. The
assigned-step glyph uses a person symbol despite the prompt's restriction; here
it communicates actor assignment, but a document symbol would be more consistent.

**Decision:** present this single generated candidate with these limitations
recorded, as requested, for a joint review. Do not label it semantically complete
or silently generate additional rounds before that review. The image remains a
proposal; the existing book is unchanged.

Browser verification on 2026-09-14: inspected saved desktop and mobile screenshots.
Both views fit on the complete sheet. The phone's fitted view requires pinch/zoom
to read the detail. The existing viewer keeps the same DOM during pan/zoom, restores
the camera from the URL, supports touch pinch and Home-to-fit, and prints to one
uncropped page. No failed resource loads or browser exceptions were recorded.
These checks concern rendering and navigation, not the architectural ambiguity above.

Local evidence: `.artifacts/architecture-structure-review/desktop-complete.png`,
`mobile-complete.png`, `desktop-shared-camera.png`, `mobile-execution-detail.png`,
`complete.pdf` and `checks.json`, all in that same directory. The existing Python
server and Chrome instance were reused; no JVM or new browser process was launched.

---

## 8. Miguel's review

Pending review of this candidate. Append Miguel's actual feedback, the image and
commit reviewed, date recorded, and the resulting content decisions. Assistant
inspection and previous feedback do not constitute approval of this new image.
