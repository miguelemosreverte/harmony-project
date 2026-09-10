# ADR 004: versioned state, request receipts, and publisher authority

Status: implemented for the two-step approval/review process; acceptance evidence is recorded with this increment.

A published definition contains a name, positive version, and an ordered list of uniquely named steps. Each step has a role and prerequisites. The initial bound is sixteen steps; prerequisites must refer to preceding steps, which rules out cycles and forward references. A separate instance stores a snapshot, the published contract ID, role bindings, current application bindings, completed steps, and request receipts.

`PublishedDefinition.Start` validates the complete role and action mapping and checks each interface view's actor and subject. The publisher's signing authority is carried into an initially empty process. Instances require both publisher and owner signatures, so a business owner cannot create a forged completed instance on its own. The publisher is a trusted provisioning authority; it is not a business action's assignee and need not remain online to sign each transition. A deployment must protect its credentials and retain active published definitions.

`AdvanceStep` is a nonconsuming entry choice controlled by the submitted actor. It verifies the definition, role, enabled step, application actor, and subject. A new request exercises the source action, archives the old instance, and creates its replacement in one transaction. The application keeps its own choice controller and validation. The publisher's party has no special application authority.

A receipt binds a request identifier to a step and actor. A matching repeat returns the existing instance without another application effect. A reused identifier for a different action is rejected. A new request for a completed step is also rejected. This application-level idempotency is independent of the Ledger API's own command identifiers and deduplication retention.

The sequence runner starts a new Daml Script process for every attempt and retains its process ID. Each invocation discovers the current instance from the ledger, using the known publisher, published definition, and reference. It aborts on missing or ambiguous instances. Waiting and reconnection are observations, not timers or hidden transitions.

The creation-path review also strengthened `SharedProgress`: a bank-signed `ProgressProposal` permits the buyer to create only a waiting instance. Shared state requires both issuer and owner signatures. The normal continuation still requires the bank's scoped, single-use result. The private golden now includes a direct forged-completion attempt, closing the earlier owner-only creation path.

The publisher signature establishes the trust boundary for process state. It does not claim to prevent all authorized signers from jointly issuing another contract. Consumers must identify the intended publisher and definition, rather than trusting an arbitrary self-published workflow with the same display name.
