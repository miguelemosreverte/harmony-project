# Runtime baseline verification

Verified locally on 2026-09-10 on macOS arm64 with Java 17.0.19, Daml/Canton 3.4.11, Scala 3.3.6, Cats Effect 3.6.3, and sbt 1.12.5.

## Executed checks

- Built the independent financing application DAR and the separate script DAR.
- Compiled the Scala JVM code and formatted it with the pinned Scalafmt configuration.
- Ran the two Daml script entry points in the SDK test runner; both passed.
- Ran `scripts/harmonia smoke` against a fresh, actual local Canton sandbox through its Ledger API; command exited successfully.

## Observed result

| Observation | Result |
| --- | --- |
| Initial application | `pending` |
| Buyer tries to approve | Rejected; original contract remained unchanged |
| Bank approves | Successful; a distinct contract ID was returned |
| Queried final application | `approved` |
| Bank can query the approved contract | `true` |
| Buyer can query the approved contract | `true` |

The live run's raw evidence is in `.artifacts/smoke-2447040685779718135/`, including `observation.json`, `script.log`, and `canton.log`. These local artifacts are ignored by Git; rerunning the documented command produces fresh evidence in a new directory.

## Boundaries

The example uses one local participant, two parties, and one source application. It validates the application choice and local runner, not multi-participant privacy or the reusable workflow core. The initial in-memory Daml checks and the subsequent actual Canton run are separate checks.

Canton bootstrap requires concrete internal ports; the runner discovers available ports before launching instead of passing port zero to every Canton component. Java 17 is selected by the macOS launcher unless an explicit `JAVA_HOME` is supplied.

The GitHub Actions configuration mirrors the local commands and pins the SDK download version. Hosted CI has not run because this repository has not been published.
