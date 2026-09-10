# ADR 003: private operations produce signed public results

Status: implemented for the three-party financing handoff.

A shared workflow exercise can make nested application actions visible to additional witnesses. A shared parent contract is therefore not used as a privacy barrier around private application operations.

The bank first signs a proposal authorizing an initially waiting shared instance. The buyer accepts it; shared state requires both issuer and owner signatures. The bank then approves its private application and creates a narrowly scoped `VerifiedResult`. The buyer's `SharedProgress.Continue` consumes that result in a later transaction. The core checks the issuer, consumer, subject, approved decision, and waiting state on the ledger. The reviewer observes shared progress and the result without being a controller.

The application and result issuance are atomic. Result consumption and shared continuation are atomic. The interval between these transactions is a persisted wait, and the two transactions are not described as one atomic operation.

The three-participant harness uses the installed Canton 3.4.11 configuration and bootstrap APIs, with separate participant identities, ports, stores, and hosted parties on one synchronizer. It owns the nodes through a Cats Effect resource and retains their generated configuration and identifiers. The nodes share a local JVM; production authentication and host isolation are separate concerns.

The proof combines party-specific active-contract queries with Ledger API `LEDGER_EFFECTS` history through a fixed ending offset. Requests have deadlines. The bank must observe the private payload and private creation events; every party must observe shared progress creation events. The buyer and reviewer must observe no private payload or private application creation events. Raw responses remain in the run's hashed `observation.json`.

These APIs follow the pinned SDK's protocol definitions and the official [transaction stream migration guide](https://archived.docs.digitalasset.com/build/3.5/reference/console-commands-migration-guide.html). The local configuration follows the SDK's bundled `sandbox/sandbox.conf` and `sandbox/bootstrap.canton`, extended to three participant nodes. The [Daml Script API](https://docs.digitalasset.com/build/3.4/reference/daml-script/api/Daml-Script.html) defines participant-specific allocation and query behavior.

The golden suite remains an operator-level evaluation tool. Its combined artifact contains all collected perspectives; the recorded viewer does not enforce live identity separation. The later live-viewer increment must enforce credentials independently of this playback selector.
