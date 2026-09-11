# Supported boundaries and their evidence

These are reference-implementation limits, not Canton platform limits. The ledger
contracts enforce business authority and atomicity. HTTP, compiler, and browser
limits bound this local evaluation's inputs and resource use.

| Boundary | Supported limit or rule | Executable evidence |
| --- | --- | --- |
| Core graph | 1–16 distinct, ordered, acyclic steps; prerequisites name earlier steps | `boundaries-check`: executes 16 actions and rejects a 17-step definition; `DefinitionTests:testDefinitions` rejects malformed graphs |
| Ordinary prerequisites | Structural ceiling of 15 earlier steps within the 16-step graph | `validDefinition` and ordered/duplicate prerequisite rejection |
| Exclusive decision | One decision, 2–4 named options, one exhaustive join | Both `branching-*` goldens and invalid definition tests |
| Join fan-in | Up to 14 conditional actions plus decision and join | `boundaries-check`: publishes and executes the 14-prerequisite join with the unselected branch skipped |
| Advance | One bound application action per core advance; changed state archives the old process | Sequence goldens and the two simultaneous Ledger API commands in `boundaries-check` |
| Atomic domain work | Two whole-position transfer legs and the core continuation in one transaction; quantities remain exact | Six `transfer-*` goldens, including final-leg rollback and a fractional 7.125 position |
| Atomic composition | Acceptance creates up to four registered source actions and starts their core process in one transaction | Both composition goldens; `boundaries-check` executes four generated actions and rejects five |
| Proposal references | Eight distinct references per workspace, including cancelled proposals | `boundaries-check`: queries eight references and rejects a ninth |
| Continuation proof | Exact issuer, consumer, subject, decision and continuation; single use | Eight `purchase-*` goldens plus private handoff |
| Missing disclosure | Knowing a contract ID does not authorize fetching its interface view | `boundaries-check`: undisclosed source cannot instantiate a process |
| Competing commands | One winner for one consumed process; one definitive conflict, one source replacement | Concurrent authenticated submissions in `boundaries-check` |
| Retry identity | Same recorded request returns the current contract without repeating source work; reuse for another step fails | Sequence stories and `boundaries-check` |
| Recorded privacy | Separately queried participant views; missing observations abort verification | Private approval and purchase goldens |
| Live identity | Fixed restricted user/party per capability; party override cannot grant rights | `live-check`: direct Ledger API read/act bypasses, unauthenticated HTTP, and observer denial |
| Package identity | Live projections match compiled package ID and module, not module names alone | `LiveFailureSuite` namesake-template test and live catalog loading |
| Error origin | Failed observation is disconnected; only a definite submission response can establish ledger rejection | `LiveFailureSuite` injects permission/not-found/invalid-argument reads and unavailable submissions; live browser reconnect experiments |
| Live command payload | 16 KiB; IDs up to 64 ASCII characters; 100 requests per evaluation | HTTP input validation and existing stale/repeated request checks |
| Live observation | Up to 512 streamed records and 8 MiB protobuf payload per active-state/history query; 20-second API deadline | `ObservationBoundsSuite` checks record and byte admission; scoped gRPC cancellation closes reads, and exceeded reads fail closed |
| Composer plan | 1–4 ordered actions, unique step names, consistent bank/buyer role bindings; names/references 80 characters, step/role 40 | Strict Scala validation, on-ledger `validPlan`, composition goldens, and browser duplicate-name diagnostic |
| DAR input | 8 MiB archive, 32 MiB actual expansion, 2,048 unique entries; eight accepted builder inputs | `builder-check`: real upload/export, malformed and oversized rejection; archive streaming validation |
| Package source | Local DAR or committed HTTPS source at a full Git commit; exact SHA-256, package ID and LF 2.1/2.2 | `packages-check`, manifest tests, and builder metadata retrieval |
| Generated choice | Consuming replacement choice; Party/Text/Int/Bool/Decimal fields and arguments within the documented mapping bounds | Two generated references, typed negative tests, deterministic builds, and a compiled mutation caught by its golden |
| Portable project | Source/configuration plus pinned vendor DARs; library/example rebuilt with empty output directories | `portable-check`: rebuilds an archived project and executes the independent approval golden |
| Local processes | One owned ledger JVM per workspace; sequential checks; sbt exits before runtime work | Workspace lease rejection, successful capped transfer, and automatic cleanup in the memory proof |

## Trust and operational scope

The publisher and process owner are trusted to publish the definition used by
these examples. They can create contracts they are authorized to sign. Core
progress records are not a cryptographic guarantee against a malicious publisher
fabricating its own state. Source application choices still enforce their own
controllers and business checks; private handoffs additionally require the
appropriate signed result and consumer authority.

The live action catalog is compiled. Inspecting an arbitrary package does not
register an action or grant disclosure. The builder exposes two reviewed mapping
families, with legacy generated approval already registered for live composition.
Nested/optional/collection arguments, arbitrary return shapes, dynamic installation,
and general BPMN interpretation are outside this edition.

The atomic limits above describe measured reference operations. An externally
implemented `StepAction` can contain its own domain work; the core does not claim
a general transaction-cost or throughput bound for unknown application code.

This local topology uses one synchronizer and one JVM for up to four separate
participants. Credentials are synthetic, APIs bind to loopback, and state is
in-memory. Browser reconnection recovers observed state within a running service.
Durable restart, production credential custody, TLS, independent failure domains,
and production load testing require their own deployment design and evidence.

See [memory limits](verification/15a-memory.md), [runtime compatibility](compatibility.md),
and [the ordered verification record](progress.md).
