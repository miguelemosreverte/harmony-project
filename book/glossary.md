# Glossary

| Term | Meaning here |
| --- | --- |
| On ledger | Daml contracts and choices executed by Canton. They decide authority and business effects. |
| Off ledger | Scala programs that submit requests, read observations, compare goldens, and present the book/UI. |
| Contract | An immutable ledger value with signatories and optional observers. A consuming choice archives its input contract; a replacement is a new contract ID. |
| Choice | A ledger operation with controllers and code. Its transaction either commits all effects or leaves none of them. |
| Party | A ledger identity used for authority and disclosure. A party is distinct from an HTTP session or display name. |
| Participant | A Canton node hosting parties and exposing their permitted ledger views. Several participants can share a synchronizer. |
| Interface | A common contract operation and view. `StepAction` lets the core use an application action without embedding that application's business rules. |
| Binding | An association between a workflow step and an application action. An adapter contract can provide that association for an unchanged application. |
| Generated adapter | Typed Daml code emitted from a reviewed mapping and the actual inspected source types. Generation is not a grant of authority. |
| Definition | A named, versioned plan with steps, roles, and prerequisites. It is published separately from a running instance. |
| Workflow / process instance | The persisted progress of one definition for one subject, including bindings, completed steps, selections, and request receipts. |
| Role | A meaningful responsibility, such as lender, bound to a concrete party for this instance. |
| Continuation | The exact next operation authorized by a signed result. A result for one continuation cannot authorize a different one. |
| Golden | A committed expected result, authored independently of execution. A changed observation fails comparison unless the expectation is deliberately reviewed and changed. |
| Normalization | Converting runtime observations into stable business facts while preserving meaningful values, order, identities, authority, and visibility. |
| DAR | A ZIP archive containing compiled Daml packages and metadata/dependencies. Its byte digest differs from its main compiled package ID. |
| Daml-LF | The compiled language format carried by packages. Compatible format versions do not imply that every application shape is supported. |
| Recorded demo | A view of retained execution evidence. Switching perspective changes the displayed recording, not ledger credentials. |
| Live session | A provisioned capability bound to one restricted participant user. Its controls submit actual commands and read that participant's actual state. |
| Request receipt | A persisted record identifying a previously completed core action. The client reuses an uncertain request ID to reconcile its outcome. |
| Provenance | The source revision/worktree state, inputs, artifact hashes, runtime topology, and observations identifying what was actually checked. |
