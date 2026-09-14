# Architecture version 5 — show the composition mechanism

Status: implementation and browser review in progress.
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

Pending. The source diagram will be `05-architecture.svg`, displayed inline in
`v5.html`. Version 4 remains available for comparison. Browser opening uses the
macOS `open` command.

---

## 5. User review

Pending. Direction to implement is not approval of this candidate.
