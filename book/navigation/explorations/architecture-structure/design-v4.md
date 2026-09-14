# Architecture version 4 — three views of one system

Status: generated, inspected in the browser, ready for user review.
Recorded: 2026-09-14 UTC. Baseline: `2288af1`.
This is an image prototype of the proposed architecture, not a code audit.

---

## 1. Review and intended understanding

Version 3 removed text without making the architecture sufficiently clear.
Miguel questioned the electrical-plug metaphor and the apparently one-way chain.
His suggestion was to compose several diagrams into one explanation. The agreed
questions are: where does it live, who governs what, and how do applications join?
The full sheet remains visible beside concise, rendered [Markdown](read-v4.md).

The desired takeaway: Harmonia coordinates workflows on Canton while
participating applications retain their contracts and authority.

---

## 2. Source facts

| View | Exact source quotation | Visual consequence |
| --- | --- | --- |
| Location | “composition state stays on-ledger” — [original §1, line 202][ledger] | Canton encloses Core and both application packages; Dapp remains outside. |
| Location | “interaction surface · not the orchestration layer” — [original §1, line 132][dapp] | Dapp submits and reads; two-way relationship. |
| Responsibility | “workflow state” and “step-exec rules” — [original §1, lines 215–219][core] | Core contains state and rules rather than a generic plug icon. |
| Responsibility | “own authorization + ownership” — [original §1, line 261][app] | Both applications contain their own contracts and authority. |
| Participation | “authored by” / “app project”, “emitted by” / “harmonia-builder” — [original §1, lines 241–248][binding] | Two alternative build routes reach the same workflow model. |
| Participation | “existing DAR · unchanged” — [original §1, line 266][existing] | Builder consumes the existing DAR; it is not shown rewriting it. |

[ledger]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L202
[dapp]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L132
[core]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L215-L219
[app]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L261
[binding]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L241-L248
[existing]: https://github.com/miguelemosreverte/harmony-project/blob/2288af1/docs/proposal/harmonia-architecture.html#L266

“Application A/B” are illustrative architectural participants, not business
scenarios. The two applications in the first views do not represent the two
integration routes in the third. Either application can use either route.
“Authored binding” abbreviates application-authored participation declarations;
“Generated binding” abbreviates the Binding DAR and associated generated code.
Neither is a runtime service. Integration connectors describe participation,
not a resolved package-import graph. The source leaves binding mechanics open.
The map does not depict a physical Canton network, global visibility, every step
kind, or a promise that arbitrary cross-application actions execute atomically.

---

## 3. Composition pass

One landscape sheet has three shallow horizontal views, each with one question.
Core stays toward the middle and applications on the right in the first two.
The first establishes the ledger boundary. The second opens the same structure
to reveal responsibility. The third explains the two build-time routes into that
model. Numbers order the explanation, not execution steps.

Keep the white, pale-blue and navy visual language. Use subtle depth on the
containers and legible labels; remove electrical plugs, keys, gear decorations,
anonymous document icons and unnecessary connector routing. A workflow drawing
inside Core can express state. Boundaries and contents carry the other meanings.

The few relationship labels must convey something specific and use normal-sized
text. “Submit · read” makes the Dapp relationship bidirectional. “Authorized
actions” qualifies Core's relationship with application contracts. The build-time
arrows express generation and convergence, not a runtime request pipeline.

---

## 4. Ablation pass

Remove the detailed contract schema, source field lists, use cases, package
manager, sample-app catalog, and generic interface badge from this overview.
They do not help answer the three selected architectural questions. Their source
material remains intact. Do not optimize an arbitrary word count: preserve the
words needed to distinguish responsibilities and relationships. Check that the
three views compose and do not look like three consecutive runtime stages.

---

## 5. Exact image prompt

```text
Use case: infographic-diagram.
Create a carefully composed architecture presentation for Harmonia: THREE COMPLEMENTARY VIEWS OF ONE SYSTEM, all visible together on ONE landscape 3:2 sheet, 1536 by 1024 or higher at the same ratio. Use the supplied version-3 image ONLY as a style reference: retain its white background, pale icy blue enclosures, navy sans-serif typography, restrained luminous-blue connections, soft dimensional card surfaces and generous spacing. Change its content and layout completely as specified. This is for senior engineers. Aim for lucid editorial hierarchy and a calm, precise technical presentation.

COMPOSITION: three horizontal bands stacked top to bottom, approximately equal height, with restrained whitespace between them. No outer decorative page frame. Each band has a short large heading at its upper left. These are three views, NOT three steps in runtime execution. Do NOT draw arrows between the bands or add carousel controls. The exact headings are "1  Where it lives", "2  Who governs what", "3  How applications join". Under each heading, give the diagram ample width. Everything remains visible and uncropped. Use large consistent labels, minimum about 25 pixels at 1536-pixel image width, headings about 33 pixels. Do not insert tiny captions. No overall headline. No paragraphs.

VIEW 1 — LOCATION. On the left, a modest browser-shaped panel labeled "Dapp". To its right, ONE pale-blue enclosure labeled "Canton · Daml" contains "Core" toward the middle and two separate equal application cards stacked at the right, labeled "Application A" and "Application B". Draw a single bidirectional Dapp/Core connection crossing the Canton boundary, with the readable phrase "Submit · read" above it. The arrow tips meet the Dapp and Core cards. Core connects by a quiet simple branch line WITHOUT ARROWHEADS to both application cards; this establishes related components, not temporal order. Core can contain a small elegant three-node workflow motif. Application cards need no decorative icon. The Dapp is clearly outside Canton. Both applications and Core are clearly inside it. No Builder in this view.

VIEW 2 — RESPONSIBILITY. This enlarges the SAME Canton arrangement. Use one pale-blue enclosure labeled "Canton · Daml". Core is again toward the middle-left and two separate application cards stacked on the right, matching view 1's relative arrangement. Core is a larger card labeled "Core", containing two short readable lines "Workflow state" and "Step rules" and a small workflow motif with a highlighted current node. Each application card is labeled respectively "Application A" and "Application B", and each contains the short phrase "Contracts · authority". A simple branch connection from Core reaches each application's card. Above the shared segment, in normal readable type, put "Authorized actions". No arrowheads on these structural connections: these are relationships between Daml contracts executing on the same ledger, not messages between remote servers. Application boundaries and their contents communicate retained authority. Do not depict a key, lock, electrical socket or plug. Leave the leftmost space available for the larger Core card; the Dapp need not be repeated in this close view.

VIEW 3 — PARTICIPATION. Clearly label the left portion "Build time". Show TWO ALTERNATIVE horizontal preparation routes stacked within that portion, with sufficient spacing for all text. Upper route: "App project" leads by an arrow to "Authored binding". Lower route: "Existing DAR" leads by an arrow to "Builder", then by another arrow to "Generated binding". The existing DAR is an input to Builder, not an output. Keep both binding outputs vertically aligned. The two routes converge into ONE right-hand destination card labeled "Same Core model", with a small version of the same workflow motif. Put this destination within a pale-blue enclosure labeled "Canton · Daml". The binding outputs remain build artifacts outside this destination boundary. Both routes are equally valid alternatives; do not draw an arrow between them. Do not connect the Authored binding through Builder. Keep the two connectors entering the same destination clear and uncrossed. No Application A/B labels in this view: the routes are options for either application. Use only simple package/document-shaped outlines where helpful; never electrical plugs or decorative gear icons. The connection into the destination represents participation in the common model, not a package-import claim or a runtime service pipeline.

TEXT: render only the exact labels specified above, each at readable size. Do not add a slogan, footer, legend, source citation, definition/instance/continuation schema, financing or transfer scenario, avatars, money, buildings, service stacks, API badges, electrical symbols, padlocks or small arrow annotations. Use precisely drawn containers, clean alignment and whitespace to do most of the explaining. Preserve the visual language of the reference while making the architecture more understandable. None of the three views should overpower the others or introduce an unrelated visual style.
```

Reference: [03-structure.png](03-structure.png), visual style only.
These four refinement passes are steps in this working session, not separate
user review sessions.

---

## 6. Generation and assistant inspection

### First image

Built-in image generation, using the section 5 prompt verbatim and version 3
as a style reference. Started 2026-09-14 06:44:31 UTC; returned 06:45:10 UTC.
Saved unchanged as [04a-structure.png](04a-structure.png), 1536 × 1024.
Original output: `~/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/exec-f6b63d47-9f77-4b56-ba10-3eab62a713d8.png`.

Assistant inspection: all three views are present, the Dapp connection is
bidirectional, and Core/application connections are structural branches. Both
integration routes reach the same model. However, “Authorized actions” became
a smaller connector label, repeating the readability weakness Miguel identified.
Move that meaning into the application cards, next to the responsibility it
describes, using the same readable supporting-text size. This is a targeted
revision, not another change of visual direction.

### Exact refinement prompt

```text
Use case: precise-object-edit.
Edit this Harmonia architecture sheet with ONE targeted correction to VIEW 2, "Who governs what". Preserve the entire image composition, exact dimensions, pale-blue/navy/white style, all three views, all headings, every component position, boundaries, connections, and all content in views 1 and 3.

In view 2 ONLY: remove the small "Authorized actions" text sitting over the middle connection. Leave that blue connection intact and completely unlabeled. Inside EACH of the two application cards in view 2, keep the existing "Application A" or "Application B" title. Replace the single "Contracts · authority" supporting line with TWO centered lines: "Own contracts" and "Authorized actions". Use clear navy-blue sans-serif supporting type at least 25px tall at this image's 1536px width, the same size as or slightly larger than Core's supporting lines. Fit the two lines comfortably inside the existing card; modestly increase the card height only if essential, preserving whitespace between the two cards and the Canton enclosure. Do not add icons, arrow labels, new words, boxes, connectors or a footer. Keep every other pixel as close to the input as possible. Output the complete uncropped 1536 × 1024 sheet.
```

Input: `04a-structure.png` as edit target, already visually inspected.

### Final image and provenance

The refinement used built-in image generation, started 2026-09-14 06:45:48 UTC
and returned 06:46:30 UTC. It removed the small connector label and placed
“Own contracts” and “Authorized actions” within each application. The rest of
the composition remains visually consistent with the first candidate.

| Artifact | Dimensions | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| [04a-structure.png](04a-structure.png), first candidate | 1536 × 1024 | 1,476,460 | `4e7e99c890a1e48da098567deeafbc1fb5e960e5122c87fad5067befe59165b2` |
| [04-structure.png](04-structure.png), proposed candidate | 1536 × 1024 | 1,519,222 | `b7438b6f88efb1df7bb89efaf75e8d562f36c65689e241a84c7c76fb8afac7a5` |

Final original output:
`~/.codex/generated_images/01a08cd4-eaa1-7bd2-83c1-bf9bf78e55de/exec-e2ba6dd2-c5c3-48a7-bb17-15c5574cbae1.png`.
Both files were copied unchanged into this directory; no programmatic image
editing was used. The initial design and exact first prompt were committed and
pushed before generation at `f6ce727`.

Both generated images, the browser page and the inspection record were committed
at [4cfdb1a](https://github.com/miguelemosreverte/harmony-project/commit/4cfdb1abd0612e4db1ea93e018b1673694f344a6),
2026-09-14 06:48:01 UTC, and pushed to `design/architecture-clarity-v2`.

### Assistant assessment

The three headings establish specific reader questions. Shared placement makes
the second view recognizable as a closer explanation of the first. The top
connection visibly supports submission and reading; the other runtime connections
have no arrowheads. The two preparation routes are separate and converge without
passing through one another. No electrical symbols or business stories remain.

The second view's empty left area could be used more effectively. The recurring
Core motif identifies the same component but does not itself explain execution.
The participation view deliberately abstracts deployment and concrete binding
mechanics. These are remaining design tradeoffs, not evidence of implementation
completeness. The user still needs to judge whether the three views add enough
understanding to justify the repeated structure.

### Browser inspection

[v4.html](v4.html) renders the actual `read-v4.md` beside the generated image.
It reuses version 3's CSS and pan/zoom code unchanged; version 3 remains intact.
Rebuild with the existing Markdown dependency:

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-review.py 4
```

Screenshots and browser checks are saved under
`.artifacts/architecture-structure-v4-review/`. At 1600 × 1050 and 1440 × 900,
all three views and the complete prose fit on screen. The 1440 × 900 screenshot
was visually inspected: typography is legible, diagram panels are uncropped,
and the prose is rendered with headings, emphasis and quotations.

At 390 × 844, the prose and image stack. The full image is a small overview;
reading its details requires pinch/zoom. The mobile screenshot was inspected
with that limitation explicit. Prose scrolls inside its own area. There is no
document overflow or added navigation menu.

The reused Chrome session passed checks for two interaction surfaces, stable
image DOM during pan/zoom, camera URL restoration within 0.05 pixels, prose
scroll restoration, touch pinch, Home-to-fit, and single-page uncropped PDF
printing. No browser exceptions or failed resources were recorded. The existing
Python server and Chrome were reused; no JVM or extra browser process was started.

Evidence: `desktop-complete.png`, `laptop-complete.png`,
`desktop-shared-camera.png`, `mobile-complete.png`,
`mobile-responsibility-detail.png`, `complete.pdf`, `checks.json`.

---

## 7. User review

Pending. The request to generate this candidate is not approval of its result.
