# Harmonia architecture — complete presentation

Status: preparing a complete visual candidate for review.
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
| Scope | “basic sequential flow”; “exclusive branching and joins”; “bounded atomic execution blocks” [S9] | State the bounded supported vocabulary once. |
| Open design | “Package-import direction”; “TemplateId representation”; “Binding mechanism”; “Template & choice names in §2” [S10] | Put uncertainty in a small, explicit footer. Draw logical relationships without asserting final package imports or implemented identifiers. |

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

Pending generation of the complete sheet.

## Review

Miguel's review of the complete candidate is pending. Assistant inspection is not
user approval. Append actual feedback here and revise the entire presentation
while keeping its context available.
