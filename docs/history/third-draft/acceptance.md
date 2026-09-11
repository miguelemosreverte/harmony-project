# Third-draft verification and handoff

Verified locally on 2026-09-11. TD01–TD06 are complete on `third-draft`. The [design](../THIRD-DRAFT.md) records the principles and ordered work; the [reading guide](reading-guide.md) follows the implemented financing, composition, package, and book paths.

## Identified delivery

| Item | Evidence |
| --- | --- |
| Verified software revision | `070f499f045b196cfab66c8bd34dd6c86c94dc8f` |
| Clean release directory | `.artifacts/release-7433717228274830633/` |
| Archive | `harmonia-070f499f045b.tar.gz` in that directory; 62,192,595 bytes |
| Archive SHA-256 | `694e69671fc329dd9e8b3047f133ac8c0c9ad6df859a39e678663c48875c933b` |
| Payload | 1,292 file hashes, 32 clean-revision recordings, nine chapters |
| Relocated bundle | `.artifacts/third release relocation/harmonia-070f499f045b/` |
| Retained book | `http://127.0.0.1:56007/`, served by the relocated `run-book` |

This acceptance documentation is committed after the verified software. The archive and observations retain their actual source revision; unfinished planning boxes inside that immutable snapshot reflect its packaging point. The second draft remains at `c971845`; `main` and `first-draft` remain at `d58c113`. Their [second](../second-draft/acceptance.md) and [first](../first-draft-acceptance.md) acceptance records remain historical.

## What changed

The second draft established feature ownership, structured LF inspection, and typed browser consumption. This pass extends named models through the producers: `LedgerExercise`, financing and composition observations, `WorkspaceSnapshot`, package workspace facts, and book recordings. HTTP and file writing encode those values at the boundary. Composition has its own command family, and package generation no longer reparses JSON state to select behavior.

Malformed visible observations fail with context. Missing disclosure remains optional. Nine additional Scala tests cover this distinction, transport-field preservation, feature command decoding, and honest rendering of a missing observation. Existing ledger authority, raw evidence, supported mappings, and independent expectations are preserved.

## Complete clean-checkout gate

`scripts/harmonia release-check` created a clean checkout of the identified revision. All eleven gates passed sequentially; their recorded durations total 717 seconds, before final assembly and archive verification.

| Stage | Seconds | Deciding result |
| --- | ---: | --- |
| `build` | 84 | Formatting, JVM/browser compilation and linking, generated bindings, all 18 ledger packages |
| `daml-test` | 6 | Four scripts pass, including boundaries and malformed definitions |
| `scala-test` | 4 | All 45 tests pass |
| `packages-check` | 46 | Pinned inputs on fresh networks; retrieved DAR bytes preserved |
| `bindings-check` | 77 | Both generated applications execute; determinism holds; compiled mutation is detected |
| `check` | 314 | All 27 regular stories match; two direct/generated parity scenarios pass |
| `live-check` | 23 | Authenticated handoff, API authority, stale/repeated requests, reconnect |
| `composer-check` | 45 | Direct and generated consented workflows match |
| `builder-check` | 34 | Package acquisition, bounded inspection, compilation/export, negative inputs |
| `boundaries-check` | 64 | Execution limits, disclosure, retry identity, competing advances |
| `portable-check` | 20 | Archived project rebuilds from empty outputs and executes its golden |

The release directory and bundle retain `checks/` logs. `.artifacts/third-release.log` records the complete run. The manifest verifier checks payload hashes, complete expected/actual comparisons, exact example/chapter membership, clean source identity, recording provenance, and local links.

All 67 example files match their move-manifest hashes and original first-draft Git bytes (`.artifacts/third-example-preservation.json`). Both pinned source DAR hashes remain unchanged: legacy financing `b77e417dedc16733ef751b79b82df99b063819d514622b8b4e56d16f3ad8cf27`; primitive approval `20be5fd4ec0de6fac4ae260c0e89a536c5f15dcc0e350165e6693c93b5132b2f`.

An earlier comparison exported the preserved second-release recordings through the new typed book producer and found exact JSON equality for all 32 recording payloads and nine chapters. Those historical recordings retained their original provenance. The accepted archive instead contains fresh executions at the third-draft revision above.

## Relocated application and browser

The archive hash was checked before extraction into the path containing spaces above. `run-verify` passed. A deliberate comment appended to `book/book.css` caused rejection of that exact file; restoring its bytes made verification pass again. Evidence: `.artifacts/third-relocated-verify.log`, `third-relocated-tamper.log`, and `third-relocated-restored.log`.

The relocated `run-live` provisioned Bank, Buyer, and Reviewer sessions. A missing session returned HTTP 401. Bank approval and Buyer continuation committed, with completed shared progress and no private details exposed to Buyer or Reviewer. Sanitized results: `.artifacts/third-relocated-live-verification.json`.

The browser then proposed a workflow containing generated approval and direct review. Buyer accepted the exact proposal; Bank executed approval; Buyer completed review. Draft text, keyboard focus, and selection survived background polling. The package panel retrieved the pinned participant DAR and compiled its reviewed adapter. The resulting project download passed ZIP integrity checks: 2,199,604 bytes, 12 entries. Evidence: `.artifacts/third-relocated-browser-live.json`.

The live owner and its Canton child exited. A subsequent `run-verify` passed, confirming execution preserved the packaged source and payload (`.artifacts/third-relocated-after-live.log`). The packaged book replaced the second-draft preview at the same URL. Browser checks opened all 32 recordings and nine chapters, verified clean provenance, and fetched every contextual operation source. Desktop and 390-pixel chapter/boundary layouts had no document overflow; the boundary report retained “Phase 2 of 2.” The reader returns to Chapter 1. Results: `.artifacts/third-release-browser.json`; captures: `third-release-book-desktop`, `third-release-book-mobile`, and `third-release-book-final`.

## Resource use and local cleanup

The release sampler captured 901 observations over 726.1 seconds. Peak aggregate Java RSS was 3,220,480 KiB, approximately **3.07 GiB**, across at most four JVMs: release coordinator, current check, Canton, and a transient Script client. One ledger environment ran at a time. This measurement excludes the retained book, native tools, and other system memory. Data: `.artifacts/third-release-resources.json` and its JSONL samples.

Before the successful run, the release preflight refused to build with less than 900 MiB free disk. Approximately 915 MiB of reproducible compiler caches and verified duplicate unpacked bundles were reclaimed. Source, raw recordings/logs, both prior hashed archives, and the active second-draft bundle were preserved. The archive and payload hashes were checked before removing duplicates. Evidence: `.artifacts/third-release-preflight.log` and `.artifacts/third-cache-reclamation.json`. Root DARs and browser output were restored from the verified build afterward (`.artifacts/third-restored-build-outputs.json`), so cleanup does not remove the normal demo's required outputs.

Final independent process inspection found one book JVM, PID `96856`, at 98,784 KiB RSS (about 96 MiB), with a 128 MiB heap cap. No live runtime, Canton, sbt, or Script JVM remained. `.artifacts/third-final-processes.json` records that inspection. Heap limits do not bound all resident memory, and PID/RSS are observations at handoff.

## Review and reproduce

Read one golden story in the book, then follow its operation using the [four reading traces](reading-guide.md). They also identify the remaining intentional raw metadata/evidence, SDK field decoding, and Daml choice adapters. Feature packages express ownership within separate JVM and browser build targets.

The verified environment uses Java 17, Scala 3.3.6, Cats Effect 3.6.3, and Daml SDK 3.4.11 on macOS ARM64. The clean build reused installed tools and dependency caches. From the bundle, `./run-book` needs Java 17; `./run-verify` also uses Git; `./run-live` additionally needs the pinned Daml SDK. Follow [setup](../../../book/setup.md) for rebuilding with `source/scripts/check`.

Acceptance covers the local behavior in the [capability](../../capabilities.md) and [compatibility](../../compatibility.md) documents. The reading guide supports human review; compiler and test success do not establish a human readability verdict. Production availability, arbitrary integration, publication, and external adoption are not claimed.
