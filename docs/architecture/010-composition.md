# ADR 010: bounded composition with explicit partner consent

Status: accepted for the local evaluation. The ledger foundation, recorded proof, and browser editor are implemented; package input follows in the same PRD step.

A bank proposes a named, ordered workflow with one to four actions, unique step names, and roles bound consistently to the bank or buyer. The buyer accepts the exact ledger draft. Acceptance atomically creates the application contracts, publishes a core definition, and starts its instance. The bank can cancel an unaccepted draft. Sources do not exist before acceptance. The workspace retains at most eight unique proposal references, including cancelled proposals.

The available typed actions are direct financing approval, review confirmation, and approval through the generated legacy adapter. Each step has an independently owned source contract and progresses through `Harmonia.Process.Engine`. This composer exposes sequential workflows; the broader core's branch/join behavior remains covered by its explicit stories. Adding a new compiled action requires rebuilding and starting a fresh evaluation.

The two parties can both see these public evaluation sources. Private financing remains a separate signed-result handoff; placing its payload in public composition would change its disclosure boundary. Proposal and acceptance are ledger-enforced choices. UI eligibility is only guidance.

Scala owns strict input validation, authenticated command submission, and projections of actual contracts. The Daml templates repeat the authority and structural invariants needed when bypassing HTTP. Projection uses actual role assignments, source bindings, prerequisite completion, and replacement contracts. Golden observations are derived from authenticated state and retain each participant's actual event stream.

The bank is the trusted definition publisher and process owner for this tutorial. Application choices enforce source authority. This is not an assertion that an untrusted publisher cannot fabricate its own progress records. See the core architecture and capability matrix for that boundary.
