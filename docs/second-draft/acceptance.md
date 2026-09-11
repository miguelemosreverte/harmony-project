# Second-draft verification and handoff

Verified locally on 2026-09-11. All SD01–SD11 implementation work is complete on `second-draft`. The [principles](../../SECOND-DRAFT.md), [architecture](architecture.md), and [three reading traces](reading-traces.md) explain the resulting design. `main` and `first-draft` remain at `d58c1136b1b90180bb36bc5b69ac43131051003e`.

## Identified delivery

| Item | Evidence |
| --- | --- |
| Verified software revision | `b1ed52f38553888696fd512c9558a18f395a0ec5` |
| Clean release work directory | `.artifacts/release-3265764795549403592/` |
| Archive | `harmonia-b1ed52f38553.tar.gz` in that directory; 61,885,358 bytes |
| Archive SHA-256 | `c3ce4ca8aa3c4f685d882228409a8d0fffbf2b7c1facfb75738c1c22c01bb978` |
| Payload | 1,266 file hashes, 32 clean-revision recordings, nine chapters |
| Relocated bundle | `.artifacts/second release relocation/harmonia-b1ed52f38553/` |
| Retained book | `http://127.0.0.1:56007/`, served by the relocated `run-book` |

The archive identifies the software and evidence revision above. This handoff documentation is committed afterward and does not relabel the archive or its observations. Planning checkboxes inside the immutable source snapshot reflect the earlier packaging point; this record supplies final acceptance. The [first-draft acceptance](../release/first-draft-acceptance.md) remains historical.

## Complete clean-checkout gate

Command: `scripts/harmonia release-check`. The runner created a clean checkout of the identified commit and executed the following gates sequentially. Every stage passed; their recorded durations total 705 seconds, before final assembly and archive verification.

| Stage | Seconds | Deciding result |
| --- | ---: | --- |
| `build` | 75 | Formatting, JVM/browser compilation, browser linking, generated bindings, and all 18 ledger packages |
| `daml-test` | 5 | Four scripts pass, including core boundaries and malformed definitions |
| `scala-test` | 4 | 36 tests pass, including typed decoding, LF references, observation failures, and example preservation |
| `packages-check` | 44 | Pinned inputs run on two fresh networks; administrator retrieval preserves archive bytes |
| `bindings-check` | 81 | Both generated applications execute; repeated sources/DARs match; a compiled mutation produces two detected differences |
| `check` | 311 | All 27 regular ledger stories match; direct/generated parity passes |
| `live-check` | 23 | Authenticated handoff, API authority, stale views, repeated requests, and reconnect |
| `composer-check` | 45 | Direct and generated consented workflows match their independent expectations |
| `builder-check` | 34 | Package acquisition, bounded inspection, compilation/export, and negative inputs |
| `boundaries-check` | 63 | Actual execution bounds, disclosure, retry identity, and competing advances |
| `portable-check` | 20 | An archived project rebuilds with empty output directories and executes its approval golden |

The release directory's `checks/` contains complete logs; the bundle retains these logs and selected raw evidence. `.artifacts/second-release.log` records the overall run. Release verification recomputes file hashes, complete expected/actual comparisons, exact example/chapter membership, clean source identity, recording provenance, and local links.

The first clean attempt caught an obsolete fixture lookup in `CheckPackages`. Commit `b1ed52f` corrected it; the complete gate above was then rerun. The failed attempt remains at `.artifacts/release-5222804508568798497/`. No failed output was promoted into this release.

## Independent expectations survive the rewrite

All 67 files in the [move manifest](example-moves.json) match both their recorded SHA-256 and the preserved first-draft Git contents. Inputs and expectations were moved without changing their bytes. Both source DAR identities remain unchanged:

| Application archive | SHA-256 |
| --- | --- |
| Legacy financing | `b77e417dedc16733ef751b79b82df99b063819d514622b8b4e56d16f3ad8cf27` |
| Primitive approval fixture | `20be5fd4ec0de6fac4ae260c0e89a536c5f15dcc0e350165e6693c93b5132b2f` |

A separate real-ledger experiment at `fd79b03` passed the ordinary workflow and failed its deliberately wrong expectation at `$.actions[1].workflow`: `waiting` expected, `complete` observed. Both recordings identify clean source. Evidence is in `.artifacts/check-11440986728374864833/` and `.artifacts/second-regression-owned.log`. The reader showed the same mismatch visually; the release gate independently caught the compiled binding mutation at the final software revision.

## Relocation and reader checks

The archive hash was checked before extraction into the path containing spaces above. The relocated `run-verify` passed. Appending a deliberate comment to `book/book.css` caused verification to reject that exact file; restoring its original bytes made verification pass again. Logs: `.artifacts/second-relocated-verify.log`, `second-relocated-tamper.log`, and `second-relocated-restored.log` in `.artifacts/`.

The relocated `run-live` provisioned three authenticated sessions. A request without a session returned HTTP 401. Bank approval and Buyer continuation both committed; Bank, Buyer, and Reviewer observed completed shared progress. Buyer and Reviewer received no private details. The runtime owner and its Canton child exited after the check. Sanitized results are in `.artifacts/second-relocated-live-verification.json`; credentials remain in the private run directory. A further `run-verify` passed after live shutdown (`.artifacts/second-relocated-after-live.log`), confirming that execution preserved the packaged source and payload.

The relocated `run-book` replaced the preserved preview at the same URL. Browser checks opened all 32 fresh recordings, found selectable units, verified their clean source revision, and fetched every contextual operation link. Chapter and boundary-report layouts had no document overflow at 390 pixels; the boundary report retained its explicit “Phase 2 of 2” label. The book returns to Chapter 1. Results: `.artifacts/second-release-browser.json`; session captures: `second-release-book-desktop` and `second-release-book-mobile`.

The fuller [interaction walkthrough](interaction-checks.md) covers lost replies, reconnect, consent, reordered generated composition, package downloads, keyboard focus, preserved draft/selection during polling, and visible golden failures.

## Resource ownership and limits

The release sampler recorded 880 observations of the release process and its descendants. Peak aggregate Java RSS was 3,173,904 KiB, approximately **3.03 GiB**, across at most four Java processes: release coordinator, current check, Canton, and a transient Script client. Checks used one ledger environment at a time. The measurement excludes the retained first-draft book, native compiler processes, and non-Java memory; it is sampled RSS, not a total-system memory limit. Data: `.artifacts/second-release-resources.json` and its JSONL samples.

After relocation checks and preview replacement, process inspection found one book JVM, PID `88837`, at 97,360 KiB RSS (about 95 MiB), with a 128 MiB heap cap. No sbt, live runtime, or Canton process remained. The temporary Python preview and original book JVM were stopped. `.artifacts/second-final-processes.json` records that inspection. Heap caps do not cap all resident memory.

The verified environment uses Java 17, Scala 3.3.6, Cats Effect 3.6.3, and Daml SDK 3.4.11 on macOS ARM64. Clean builds reused installed tools and dependency caches. The [capability](../capabilities.md) and [compatibility](../compatibility.md) documents define the supported local topology, finite reviewed mappings, and bounded inputs. The work does not establish production availability, arbitrary runtime integration, or an independent human readability verdict.

## Review and reproduce

Open the book, follow one example into its expectation and operation, then use the three reading traces to review financing, composition, and package generation. The [repository map](../architecture/repository.md) and [Scala guide](../architecture/scala.md) explain where those owners live.

From the unpacked bundle, `./run-book` needs Java 17; `./run-verify` also uses Git. `./run-live` additionally needs Daml SDK 3.4.11. For a complete rebuild, follow [setup](../../book/setup.md) and run `source/scripts/check` with the documented toolchain. The source archive, generated projects, committed expectations, actual recordings, and release manifest provide separate reviewable artifacts.
