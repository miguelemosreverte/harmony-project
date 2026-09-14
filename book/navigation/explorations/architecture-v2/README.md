# Architecture: clarity exploration

The design measure is how quickly a reader forms a correct mental model.
The existing architecture presentation is preserved at `389d133`; these images
are concepts for discussion on `design/architecture-clarity-v2`.

## The reading order

1. **Where does the workflow live?**
   [Ledger overview](01-ledger-overview.png) makes the on-ledger responsibilities
   dominant: Core holds workflow state and rules, Binding DAR describes
   participation, and applications retain their own authority. The Dapp and
   build-time path are visually secondary.
2. **How does a step reach an application?**
   [Step execution](02-step-execution.png) follows one path from definition to
   instance, assigned step, interface and application choice. The actor and
   application authorization appear at the points where they matter.

Each image has one message, a clear reading direction and meaningful grouping.
Connections carry relationships; size and contrast carry priority. Captions
identify essential responsibilities. No interaction is needed to follow either
view.

## Source fidelity

Both derive from [the supplied architecture](../../../../docs/proposal/harmonia-architecture.html),
sections 1 and 2 respectively. The overview's Dapp-to-Core interaction also
matches the Container Diagram in [the proposal](../../../../docs/proposal/harmonia.md).
The visual reference is the approved
[workflow composition](../../../../design/0.2/infographic/narrative-v1/desktop-reference.png).

The overview's horizontal connections mean participation, not resolved package
imports. Its bank and property pictograms illustrate independent applications;
they do not restrict Harmonia to those domains. The step view uses the original's
illustrative contract names. It does not claim those exact names are implemented.

Field lists, multiplicities, reference examples, metadata retrieval, continuation,
branching, joins and atomic blocks remain in the full source diagrams. They need
their own focused explanations; they are not removed from the product scope.
The source's open binding, interface-location and import questions remain open.

## Reader check

These are proposed acceptance criteria, not measured usability results:

- After a brief look at the overview, can the reader point to where workflow
  state lives and distinguish the Dapp from the ledger?
- Can they identify which application retains authority over a choice?
- Can they follow the execution path without consulting a legend?
- Does every visible label help answer the image's question?

## Generation and review

Generated with the built-in image generation tool. The complete initial prompts
are in [prompts.json](prompts.json); the overview's final edit is recorded in
[overview-refinement.txt](overview-refinement.txt).

Both selected images were visually reviewed. The first overview candidate placed
the Ledger API arrow over Binding DAR; the selected refinement connects it to
Core and removes an unnecessary prose annotation. The existing website and
production code are unchanged. This exploration does not introduce a new product
release or replace the approved interactive diagrams.
