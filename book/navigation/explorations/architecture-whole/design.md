# Harmonia architecture — complete presentation

Status: complete candidate 03 ready for Miguel's review.
Started: 2026-09-14 UTC.
Baseline: `0336a2a` on `design/architecture-clarity-v2`.

## Review contract

Miguel's instruction:

> But why do I need to focus on just one thing? Let's try to have broader iterations, okay? L.C. is difficult for me to evaluate. I need the entire architecture thing, whatever you want to show me.

Every candidate contains the entire architecture story. One browser page shows
all regions together. Pan and zoom reveal detail without changing the content.
The review concerns the original proposed architecture, not a claim that every
part of that proposal has been implemented. The existing book is a separate
artifact until this candidate is reviewed.

## Source evidence

The two supplied diagrams are the primary structure. The proposal's execution
examples make their relationships concrete. References are pinned to the input
revision; displayed names are explanatory unless the original diagram names them.

| Region | Exact original wording | Translation |
| --- | --- | --- |
| Runtime | “interaction surface · not the orchestration layer”; “composition state stays on-ledger” [S1] | Dapp outside the ledger; Core and application packages inside Canton. |
| Participation | “direct path · app DAR implements Harmonia interfaces”; “builder path · existing app DAR + generated binding” [S2] | Two equally visible routes into one common workflow model. |
| Builder | “internal tooling · BUILD TIME ONLY”; “output → Binding DAR + generated project/code” [S3] | Build tooling and its artifact are distinct from runtime services. Package Manager is an external DAR source. |
| Application boundary | “which step kind a choice fulfils”; “authority : required from app”; “own signatories”; “own observers” [S4] | Binding as a declaration; execution still needs application authority. |
| Contract model | “WorkflowDefinition”; “WorkflowInstance”; “RoleBinding”; “StepAssignment”; “Continuation”; “HarmoniaStepAction”; “AppliesTo”; “AppTemplate” [S5] | Show definition, live instance, assigned actor, interface and application choice; include role bindings, applicability and continuation as supporting relationships. |
| Continuation | “If financing approval is obtained, the buyer creates a purchase proposal that enters a separate offer workflow.” [S6] | Financing outcome and buyer action precede a distinct offer domain. Rejection does not enter the offer path. |
| Atomicity | “bounded atomic completion only where Canton authorization and workflow structure allow it.” [S7] | Four parties prepare a transfer; conditional atomic completion follows readiness. Settler is one of those four. |
| Reference package | “at least two source-application DARs that prove the model in practice” [S8] | The two examples explain the reference package's role; it is not an integration prerequisite. |
| Scope | “sequential flow”; “XOR branch”; “join”; “bounded atomic block” [S9] | State the bounded supported vocabulary once. |
| Open design | “Package-import direction”; “templateId representation”; “Binding mechanism”; “Template & choice names in §2” [S10] | Put uncertainty in a small, explicit footer. Draw logical relationships without asserting final package imports or implemented identifiers. |

[S1]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L116-L132
[S2]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L178-L179
[S3]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L135-L160
[S4]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L394-L453
[S5]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L335-L482
[S6]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia.md#L133-L140
[S7]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia.md#L120-L128
[S8]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia.md#L90-L101
[S9]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L539-L559
[S10]: https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L569-L575

Ledger placement is also explicit in [the original component map, line 202](https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L202).
Binding participation is explicit in [lines 230–250](https://github.com/miguelemosreverte/harmony-project/blob/0336a2a/docs/proposal/harmonia-architecture.html#L230-L250).

## Four design passes in this working session

### 1. Recover the complete story

The authority image answered one valid question but hid its context. Restore both
original diagrams: component placement and contract relationships. Add both
reference workflows so the reader sees what those relationships accomplish.

### 2. Establish an order the reader can see

Four regions on one landscape sheet:

1. **Where it runs:** Dapp, ledger, Core and independent applications.
2. **How applications join:** direct implementation or build-time generation.
3. **How a step executes:** definition → instance → assignment → interface → choice,
   with role binding and application authority explicit.
4. **How domains cooperate:** continuation and conditional atomic execution,
   illustrated by the two proposed reference workflows.

The page opens with all four visible. Nothing is hidden behind slides or buttons.
Regions are reading landmarks, not approval gates or separate review sessions.

### 3. Keep the approved language and remove false implications

White background, pale blue enclosures, navy type, blue connectors, restrained
dimensional icons. No decorative people or new palette. The Binding DAR is a
participation artifact, not a runtime proxy. Package Manager is an external source,
not a promised vendor integration. Canton hosts the Daml model; no off-ledger
orchestrator is introduced. Arrows are conceptual relationships or example actions,
not claims about package import direction or successful observed transactions.

Use a short footer for scope and unresolved design choices. Preserve full evidence
in this document; the visible sheet carries small source references. No API field
dump, slogan repetition, executable-looking controls or top-level navigation menu.

### 4. Write a prompt for the complete candidate

The first prompt is saved verbatim in [01-prompt.txt](01-prompt.txt). It requests
the whole sheet in one image, not independent tiles. Its references are:

- `../architecture-v2/01-ledger-overview.png`: approved visual language.
- `.artifacts/original-diagrams/component-map.png`: factual component structure.
- `.artifacts/original-diagrams/contract-model.png`: factual contract relationships.

The original screenshots are local review captures; their source HTML is committed.
Generation uses the built-in image tool. Each actual image iteration will record
start/return time, artifact, prompt, inspection, decision and first commit below.

## Image iterations

### Round 1 — the complete architecture in context

- Tool: built-in image generation.
- Exact prompt: [01-prompt.txt](01-prompt.txt).
- References: the three images listed in design pass 4.
- Started: 2026-09-14 05:15:42 UTC.
- Returned: 2026-09-14 05:17:37 UTC.
- Artifact: [01-complete.png](01-complete.png).

**Assistant inspection:** all four regions are visible together. Both integration
routes and both reference examples are present; the ledger boundary and build-time
lane are distinguishable. However, the generated image added sentences beneath
the headings, including an inaccurate claim that every step exercises a choice.
The execution arrow arrives beside the authority key instead of the choice. The
continuation connector appears to originate from the role binding. The offer
boundary also introduces an unnecessary green accent.

**Decision:** preserve this complete candidate and refine it as a whole. Remove
the redundant descriptions and repair the two contract relationships. Keep the
same complete composition available in the second round.

### Round 2 — reduce repetition and repair execution

- Tool: built-in image generation, editing the whole `01-complete.png`.
- Exact prompt: [02-prompt.txt](02-prompt.txt).
- Started: 2026-09-14 05:18:15 UTC.
- Returned: 2026-09-14 05:19:58 UTC.
- Artifact: [02-complete.png](02-complete.png).

**Assistant inspection:** the added introductory sentences are gone. The step
interface now reaches the application choice; authority reaches it separately
from above, within the application's boundary. The offer outline uses blue.
All four regions and both examples remain visible. The role-binding tag is still
on the continuation connector, despite the requested correction.

**Decision:** make one final precise repair on the complete sheet. Place role
binding inside the Instance as data, with an uninterrupted Instance-to-Continuation
connection. Do not accept a misleading relationship just because the rest renders.

### Round 3 — finish the complete review candidate

- Tool: built-in image generation, editing the whole `02-complete.png`.
- Exact prompt: [03-prompt.txt](03-prompt.txt).
- Started: 2026-09-14 05:20:38 UTC.
- Returned: 2026-09-14 05:22:25 UTC.
- Artifact: [03-complete.png](03-complete.png).

**Assistant inspection:** role binding is now inside the Instance, and a single
uninterrupted arrow connects Instance to Continuation. The separate workflow and
authority connections still reach the application choice. All four regions,
participation routes, reference workflows, scope and open-design notes remain.

**Assessment:** this is a useful candidate for reviewing the whole architecture's
priority and coherence. It is not a complete schema listing. The execution region
illustrates a choice-exercise step; the footer names the wider supported vocabulary.
The source document's unresolved imports, interface placement and binding
mechanism are not resolved by these pictures. The illustrations are raster images
at 1448 × 1086, despite the larger size requested in the initial prompt.

**Decision:** present the entire third candidate in [one HTML page](index.html).
Use the book's existing pan/zoom implementation and deterministic camera query
parameters. Provide an accessible text equivalent and an uncropped print layout.
The four region numbers are reading order, not interactive steps or review gates.

## User review

Miguel's review of the complete candidate is pending. Assistant inspection is not
user approval. Append actual feedback here and revise the entire presentation
while keeping its context available.

## Browser validation

Reviewed the actual desktop and mobile screenshots on 2026-09-14. The desktop
opens with all four regions visible together. On a phone, the initial fit is a
map of the whole sheet; pinch and pan are required to read details. The sheet
does not reflow into unrelated cards. Fine raster labels remain the main visual
limitation at high zoom; this candidate is for composition and content review.

The existing Chrome instance and Python server were reused. No JVM or additional
browser process was started. Checks passed for full-sheet fit, one interaction
surface, no document scroll, stable DOM during pan/zoom, camera URL restoration
within 0.05 CSS pixels, Home-to-fit, touch pinch and a single uncropped print page.
No browser exceptions or failed resource loads were recorded.

Local evidence:

- `.artifacts/architecture-whole-review/desktop-complete.png`
- `.artifacts/architecture-whole-review/desktop-shared-camera.png`
- `.artifacts/architecture-whole-review/mobile-complete.png`
- `.artifacts/architecture-whole-review/mobile-execution-detail.png`
- `.artifacts/architecture-whole-review/complete.pdf`
- `.artifacts/architecture-whole-review/checks.json`

These checks establish rendering and navigation behavior, not semantic completeness
or user approval. The earlier book and the other three audience paths are unchanged.
