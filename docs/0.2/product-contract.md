# Product contract for the 0.2 work

The original [proposal](../proposal/harmonia.md) and [architecture illustration](../proposal/harmonia-architecture.html)
remain the source of product intent. The illustration explicitly leaves package
boundaries and binding mechanisms open. The existing acyclic interface package is
a legitimate implementation decision.

## The promise

> Harmonia lets independently developed Daml applications participate in a single multi-party workflow without bespoke pairwise integration.

— Proposal, Abstract, line 13.

A visiting team should be able to explain what remains inside its application,
which party can act, what evidence enables the next domain's work, and which
compiled integration steps are necessary. The book must let a stakeholder follow
that same journey before exposing Daml, DAR, or Scala details.

## Claims and evidence

Statuses describe the fourth-draft baseline. They are assessments of scope, not
fresh test results or formal milestone acceptance. A quotation does not prove a
claim. The 0.2 design work does not upgrade any runtime status.

| ID | Product claim | Source | Baseline assessment | Next evidence |
| --- | --- | --- | --- | --- |
| C01 | On-ledger workflow state and authorized source actions | Proposal 53–74 | Demonstrated in local references | Retain core and source refusal goldens |
| C02 | Both integration paths share a reusable core | Proposal 13–17, 90–116 | Demonstrated for bounded repository examples | Integrate an independently developed application |
| C03 | Private domains exchange only the required result | Proposal 133–141 | Demonstrated local financing/offer reference | Replay visibility and wrong-consumer cases in the new chapter |
| C04 | Four parties coordinate before one eligible atomic transfer | Proposal 120–127 | Demonstrated local reference | Expose the entire reference journey in the live UI |
| C05 | Supported branches, joins, and continuation are visible | Proposal 37, 63–74; M2–M6 | Core coverage exists; live composer is sequential | Map each supported operation to an actual live view |
| C06 | Existing DARs can produce usable compiled adapters | Proposal 103–116; M4–M5 | Bounded generator and portable examples | Prove new eligible input without changing core |
| C07 | Package Manager and participant DAR sourcing support the journey | Proposal 108–114; M2/M6 | Pinned GitHub and local admin retrieval; proposal interpretation unresolved | Choose and verify the actual provider journey |
| C08 | Progress can be relied on across independent organizations | Proposal 101, 153, 494 | Publisher/owner trust is explicit; reliance contract unresolved | Threat model and golden demonstrating the agreed guarantee |
| C09 | The viewer shows applications, state, actors, and executable steps | Proposal 407–420; M6 | Some live views plus broader recorded stories | Complete the supported live journey, including failure states |
| C10 | Another team can evaluate adoption | Proposal 410–411, 423 | Guides and templates exist; external evaluation unproven | Actual external evaluator record |
| C11 | Stable public release and knowledge transfer | Proposal 421, 466–480 | Local release evidence; publication and license undecided | Published release and actual walkthrough/workshop evidence |
| C12 | Qualified independent adoption | Proposal M7/M8 | Not established by repository evidence | Team-confirmed pilot/production usage |

## Decisions for this design

- Preserve the narrow workflow scope. General BPMN, arbitrary uploaded-code
  execution, managed hosting, and a generic studio are outside this release.
- Expose staged and atomic work differently. An animation grouping steps does
  not establish that the ledger committed them atomically.
- Label generated HTML interactions as a simulation. A role switch in the design
  is a viewing aid, not login or authorization.
- The production UI is the browser interface backed by the existing Scala
  service/proxy. It is distinct from the recorded, offline-capable book.
- Make source quotations complete and discoverable without forcing all proposal
  text into the first reading path. Commercial terms and related work get named
  context appendices with the same coverage accounting.
- Keep quote coverage, requirement mapping, executable evidence, and external
  adoption separate. No synthetic aggregate "product complete" percentage.
- Carry over fifth-draft refinements only if a later implementation needs them;
  the chosen base remains fourth draft.

## Definition of source coverage

For Markdown, the denominator is every nonblank source line, preserving Markdown
syntax, tables, links, diagram code, and headings. For HTML, it is every nonblank
rendered text node in the body, including SVG text, decoded as a browser decodes
entities; style, script, comments, and markup are excluded and reported as such.
Whole original files remain available alongside a line-numbered source view.

The coverage tool requires each unit to be assigned once and quoted completely
in a chapter or named appendix. It also reports unique source words. Both files
are pinned by SHA-256. Missing originals, unmapped text, duplicated assignments,
stale fingerprints, altered quotes, and missing chapter destinations fail the check.
An empty document can never produce a vacuous 100% score.

This proves quotation inclusion and provenance. It does not prove that the prose
explains every requirement well, that a feature works, or that the original missing
BPMN/SVG attachments have been recovered. Those need separate review/evidence.
