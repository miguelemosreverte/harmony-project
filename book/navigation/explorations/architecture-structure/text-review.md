# Text before diagrams

Recorded: 2026-09-14 UTC. Previous artifact: version 7 at `ab03761`.

Miguel asked to begin with a concise, structured Markdown chapter of roughly
500 words, explaining the architecture to a software engineer who has no project
context. Only after reviewing the prose should we decide whether individual
passages benefit from inline diagrams. No new image, diagram or HTML redesign
belongs in this pass.

The draft is [architecture.md](architecture.md). It establishes the purpose,
execution boundary, contract mechanism, integration paths and composition limits.
Technical names are introduced after their meaning. It describes the architecture
proposed by the original documents; it does not claim that current implementation
coverage has been audited. The original documents were reread for this draft.

First draft length: 442 words including headings, 463 including source references. Counts
use rendered Markdown text, excluding standalone punctuation. Source links and
Markdown formatting were checked. No diagrams were added.

## Second text draft

Miguel rejected the first draft's sequence of terminology definitions: it had
the visual structure of a textbook without using its definitions to explain the
system. He asked for a substantive explanation that builds from high-level
structure to lower-level behavior, at a similar length.

The rewrite follows actual responsibilities and one request through the browser,
Scala service and Daml execution. Unlike the first draft, it explains the
service's role, the atomic relationship between a supported application step and
its recorded completion, recovery from an uncertain submission, and why a shared
interface keeps Core independent of application implementations. Integration
setup precedes runtime use conceptually; their distinct roles are explicit.
Terms such as DAR, choice and workflow instance are omitted from the introduction
because their names are unnecessary to explain those relationships.

The original proposal and actual product source were both read. This chapter
describes the inspected execution path, not every proposed feature or a fresh
runtime verification of the whole product. The single-step transaction claim is
about Core's application-step execution, not arbitrary multi-step workflows.

| Claim | Inspected source |
| --- | --- |
| Browser → HTTP → Scala service → ledger | `product/README.md`; `product/server/src/main/scala/harmonia/app/http/LiveServer.scala`; `harmonia/app/workspace/Workspace.scala` |
| Request reconciliation and unconfirmed outcome | `Workspace.state`; `harmonia/ledger/client/SubmitChoice.scala` |
| Core checks actor, enabled step and application subject | `product/ledger/core/daml/Harmonia/Process/Engine.daml`, `checkRequest` and `AdvanceStep` |
| Application action and progress recorded in one Daml update | `Engine.daml`, `AdvanceStep` and `record`; `product/ledger/core/daml/Harmonia/Workflow.daml`, `Advance` |
| Shared interface, direct implementation or adapter | `product/ledger/interfaces/daml/Harmonia/Action.daml`; `product/ledger/bindings/daml/FinancingBinding.daml` |
| Reviewed mapping, metadata checks, compiled adapter, unchanged source | `product/server/src/main/scala/harmonia/bindings/generate/GenerateBinding.scala` |
| Result issuer, consumer and authority checked at handoff | `product/ledger/core/daml/Harmonia/SharedProgress.daml`, `Continue` |

No diagrams or new interface controls are added. The browser typography and layout
are retained. Review of the rewritten content is pending.

The browser edition was checked at desktop and mobile sizes, including the
execution section and both source links. Screenshots were visually inspected;
no horizontal overflow, missing sections or browser errors were found. Evidence
is in `.artifacts/architecture-text-second-review/`. The revised chapter was
opened with `open` at `architecture.html?revision=2`.

## Third text draft — begin with Canton

Miguel proposed a friendlier textbook sequence: Canton and banks, the problem of
coordinating different contracts, then the shared interface and the architecture
that uses it. He explicitly invited inline image generation for this revision.
The chapter now follows that sequence and uses a single illustration to support
the opening. Its prompt, provenance and review are in
[textbook-illustration.md](textbook-illustration.md).

The chapter is 478 words including headings, caption and project-source note
(rendered text, counting word tokens and excluding image alt text). It retains
the existing reading layout. Desktop and mobile screenshots, image loading and
aspect ratio, section presence, overflow and browser errors were checked under
`.artifacts/architecture-textbook-review/`. Production code is unchanged.

### External source audit, checked 2026-09-14

- [Canton documentation](https://docs.canton.network/overview/understand/what-is-canton):
  selective disclosure, Daml authorization and visibility, and participant nodes
  retaining relevant contract data. The illustration is not a node topology.
- [Lloyds announcement, 20 January 2026](https://www.lloydsbankinggroup.com/media/press-releases/2026/lloyds/lloyds-tokenisation.html):
  a government-bond purchase with tokenised deposits on Canton. The bank calls it
  a pilot. The chapter preserves that qualification and makes no Harmonia
  adoption or endorsement claim.
- [Digital Asset's Canton launch announcement, 9 May 2023](https://blog.digitalasset.com/press-release/new-global-blockchain-network-of-networks-for-financial-market-participants-and-institutional-assets):
  independently developed Daml applications can interoperate and exchange assets
  in atomic transactions. Canton already provides the transaction foundation.

### Original proposal and implementation

The proposal's Abstract describes applications participating in a workflow
"without bespoke pairwise integration". Section 2, Core Layer, calls for
"workflow state that can be queried and inspected" and
"explicit identification of the actor that may execute each step".
These are the reasons for the interface and engine in the chapter, rather than
a claim that multiple contracts are inherently a Canton defect.

`Harmonia/Action.daml` verifies the exact StepAction view and ExecuteAction call.
`Harmonia/Process/Engine.daml` verifies actor/subject/step checks and the atomic
application action plus workflow-progress update. `FinancingBinding.daml`
verifies the separate adapter path. Builder's supported-shape restriction remains
explicit. The second draft's source table records the runtime and handoff sources.

The reading page has two near-claim primary-source links. This audit retains the
broader provenance without adding more interactions to the chapter. Review of
the revised teaching sequence is pending.

## Browser edition

The user subsequently asked to read the chapter in the browser. [architecture.html](architecture.html)
renders the same Markdown in a single reading column, preserving the established
white/navy palette. It adds no diagrams, navigation menus or extra content.
The source citations remain the only two links. Rebuild and open with:

```sh
.artifacts/book-tools/bin/python book/navigation/explorations/architecture-structure/render-text.py
open 'http://127.0.0.1:56202/book/navigation/explorations/architecture-structure/architecture.html'
```

Checked desktop and mobile: all five sections render, both source links resolve,
the final paragraph is reachable, and there is no horizontal overflow or browser
error. Desktop screenshot visually inspected. Screenshots and checks are saved
under `.artifacts/architecture-text-review/`. Reading uses ordinary page scrolling.
