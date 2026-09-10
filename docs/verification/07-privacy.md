# Multi-participant authority and privacy proof

Verified locally on 2026-09-10. The first successful private story is retained at `.artifacts/check-12901611492231492926/`. Its three allocated parties have distinct participant namespaces. Generated node configuration, connections, and identifiers remain under the run's `network/` directory.

## Observed behavior

The private story contains five real submissions:

- Alice's attempt to approve the private application fails with `not-visible`.
- Alice's continuation without a signed approval fails with `application-rejected`; raw diagnostics identify the missing approval result.
- Northbank approves the private application and creates the result. Shared progress remains waiting.
- Olivia's attempt to continue Alice's workflow fails with `unauthorized`.
- Alice consumes the approval result and completes shared progress.

The bank observes two private application creation events and the synthetic private payload. Alice and Olivia observe zero such creation events and no private payload in their complete party-filtered `LEDGER_EFFECTS` streams through the captured ending offsets. All three observe two shared progress creation events. Their final active-contract queries agree with this boundary.

These positive controls are required. A failed event request or a stream without shared progress aborts evidence collection. The hashed `observation.json` retains raw submissions, party queries, and the participant event responses used to derive the golden result.

## Reader verification

The exported book was exercised in a browser. Selecting Alice, Northbank, and Olivia showed each corresponding recorded query/event result against the baseline. Northbank's private payload flag was true; Alice's and Olivia's were false. Chapter 3 rendered and all eight chapter links returned HTTP 200. A screenshot is retained at `.artifacts/verification/07-participant.png`.

The [architecture decision](../architecture/003-private-progress.md) describes the two transaction stages, role ownership, result checks, and the local topology. The [chapter](../../book/03-participant-views.md) explains the same behavior to a reader.

## Aggregate checks

`scripts/check` passed locally: Daml packages, JVM tools, browser build, Daml smoke checks, twelve Scala tests, and all seven live goldens. The aggregate run's evidence is `.artifacts/check-7611596544852022046/`.

The nodes have separate identities, stores, and endpoints within one local JVM and one synchronizer. This report does not claim separate administrator trust domains, live UI authentication, or a production deployment.
