# Fourth-draft verification and handoff

Verified locally on 2026-09-11. FD01–FD07 are complete on `fourth-draft`. The [principles and ordered plan](../../FOURTH-DRAFT.md), [reading guide](reading-guide.md), and [measurements](measurements.md) explain the implementation and its limits.

## Identified delivery

| Item | Evidence |
| --- | --- |
| Verified software revision | `0014c0d7a3eb8a31f16a2ac4ee81b1ef320d5948` |
| Release work directory | `.artifacts/release-14660662735469547171/` |
| Archive | `harmonia-0014c0d7a3eb.tar.gz` in that directory; 63,097,459 bytes |
| Archive SHA-256 | `6c504d928974561c350cc69f5e0f2484198ce72ee5eb005dffb6a894fa9811dc` |
| Payload | 1,338 hashes, 32 clean-revision recordings, nine chapters |
| Retained relocated bundle | `.artifacts/fourth release relocation/harmonia-0014c0d7a3eb/` |
| Book preview | `http://127.0.0.1:56007/` |

This acceptance and measurement documentation follows the verified software commit. The archive and recordings retain that exact revision; the plan inside the immutable snapshot reflects its packaging point. Earlier branches remain unchanged: `main` and `first-draft` at `d58c113`, `second-draft` at `c971845`, and `third-draft` at `5345651`.

## Product boundary and simplification

The product owns its Daml contracts, Scala service, shared API, live browser, and reviewed package mappings. Book and harness have separate directories, compilation targets, and entry points. The service receives configured participants through `Connections`; demo network provisioning, synthetic input, credentials, golden validators, and release tools belong to the harness.

Compiler tests reject references to book, demo, story-runner, golden-result, and release types from the product. Inspection of the clean exported classpath found 44 JARs, with `harmonia-service` as its only internal application JAR. Its contents contain no book, demo, story, verification, or release packages. The independent live gate launches that product in a separate JVM. Evidence: `.artifacts/fourth-clean-classpath-boundaries.json` and the release `live-check.log`.

The live and recorded browsers have separate Scala.js entry points and assets. Shared response codecs preserve the established HTTP fields. Composition edits a typed plan and calls the same validation as HTTP decoding. The [measurements](measurements.md) distinguish the 63-line response-code reduction from relocation and new startup code: total Scala grew by 234 lines, while product Scala is now a clearly bounded 4,252 non-test lines.

## Complete clean-checkout gate

All eleven gates passed sequentially, totalling 758 seconds before final assembly and archive creation. The clean checkout reused installed SDK and dependency caches.

| Stage | Seconds |
| --- | ---: |
| `build` | 98 |
| `daml-test` | 5 |
| `scala-test` | 6 |
| `packages-check` | 49 |
| `bindings-check` | 74 |
| `check` | 317 |
| `live-check` | 27 |
| `composer-check` | 51 |
| `builder-check` | 36 |
| `boundaries-check` | 68 |
| `portable-check` | 27 |

The build compiled all 18 Daml packages and both browsers. Four Daml scripts and all 49 Scala tests passed. All 27 regular stories matched; both direct/generated parity comparisons passed. Focused gates covered authenticated authority, stale and repeated requests, reconnect, consented direct/generated composition, bounded package inspection and export, execution limits, competing advances, and portable rebuilding from empty outputs.

All 67 original golden files match their committed move-manifest hashes and first-draft Git bytes. All 47 Daml source/specimen files retain their exact third-draft bytes. Both pinned source DAR hashes remain unchanged: legacy financing `b77e417dedc16733ef751b79b82df99b063819d514622b8b4e56d16f3ad8cf27`; primitive approval `20be5fd4ec0de6fac4ae260c0e89a536c5f15dcc0e350165e6693c93b5132b2f`. Evidence: `.artifacts/fourth-example-preservation.json`, `fourth-daml-preservation.json`, and the release binding logs.

A preliminary export reused historical third-draft recordings and preserved all their input, expected/actual, and provenance fields. The accepted archive instead contains 32 fresh passing recordings at the fourth-draft revision. `.artifacts/fourth-historical-export-comparison.json` records that distinction.

## Relocation and real browser behavior

The archive digest and every payload hash were checked before testing the extracted path containing spaces. `run-verify` passed. Appending a comment to `book/book.css` caused rejection of that exact file; restoring its bytes made verification pass. Logs: `.artifacts/fourth-relocated-verify.log`, `fourth-relocated-tamper.log`, and `fourth-relocated-restored.log`.

The packaged `run-live` provisioned a disposable ledger. A separate packaged `run-product serve CONFIG STATE` connected to it with the service-only classpath. Browser Bank approval and Buyer continuation completed the private handoff. A missing session returned HTTP 401; Buyer and Reviewer did not receive the private details.

The browser then proposed a generated approval followed by direct review. Buyer accepted the exact plan, Bank approved through the generated adapter, and Buyer completed review. Draft text, focus, and selection survived polling. Duplicate step names produced the expected diagnostic. The package panel retrieved the pinned DAR, compiled its adapter, and exercised the download control. An independent fetch verified the delivered ZIP: 2,199,607 bytes, 12 entries, no integrity errors. Desktop and 390-pixel product layouts had no document overflow. Evidence: `.artifacts/fourth-relocated-live-verification.json`, `fourth-relocated-browser-live.json`, and `fourth-relocated-download.json`.

Both service processes and their owned ledger exited. A subsequent `run-verify` passed without source or payload changes (`.artifacts/fourth-relocated-after-live.log`). The new packaged book was first proved on a temporary port, then replaced the third-draft preview at the same address. All 32 recordings and nine chapters rendered; every contextual operation source loaded; all recording provenance matched the clean revision. Desktop and 390-pixel chapter/boundary layouts had no document overflow, and the boundary view retained “Phase 2 of 2.” The browser returns to Chapter 1. Evidence: `.artifacts/fourth-release-browser.json`.

## Memory and local cleanup

The sampler captured 944 observations over 757.8 seconds. Peak aggregate Java RSS was 3,593,840 KiB, approximately **3.43 GiB**, across at most four owned JVMs. This excludes the retained preview, native tools, and other system memory. One ledger environment ran at a time. The third draft's measured peak was 3.07 GiB; this run does not establish a memory reduction. Data: `.artifacts/fourth-release-resources.json` and its JSONL samples.

Final independent inspection found only the packaged book JVM, PID `9981`, at 93,232 KiB RSS (about 91 MiB), with a 128 MiB heap cap. No live service, sbt, Script, or Canton JVM remained. PID and RSS are handoff observations. `.artifacts/fourth-final-processes.json` records the inspection.

Local disk was tight. Reproducible compiler caches and verified redundant unpacked bundles were reclaimed; all prior archives, raw recordings, source, logs, current root build outputs, and the active fourth-draft bundle remain. The duplicate newly assembled bundle was removed after archive and relocated-payload verification; its small post-assembly verification logs were retained separately. The release checkout still contains source and raw evidence, with rebuildable compiler caches removed. Cleanup records are `.artifacts/fourth-cache-reclamation.json`, `fourth-05-cache-reclamation.json`, and `fourth-final-cache-reclamation.json`.

## Commit sequence and reproduction

1. `e0ec4e1`: record principles, baseline, and ordered acceptance.
2. `a44509d`: separate product, book, and harness builds and connections.
3. `957bc67`: record compiler isolation and a working ledger/browser handoff against unchanged expectations.
4. `ac35d28`: simplify typed product data and isolate demo-only behavior; standalone product passes the live gate.
5. `0014c0d`: package separate runtime launchers and lead the documentation with the product.
6. Final documentation commit: record the full release, browser proof, measurements, cleanup, and completed plan.

The third actual commit therefore records demonstrated working behavior. The final documentation does not relabel earlier recordings as newer evidence.

From the extracted bundle, `./run-book` starts recorded playback with Java 17; `./run-verify` also uses Git. `./run-live` needs Daml SDK 3.4.11. `./run-product serve /absolute/configuration.json` uses the [configured local participants](../../product/README.md). See [setup](../../book/setup.md) and [packaging](../release/packaging.md) for a source rebuild and the complete gate.

Acceptance covers the demonstrated local [capabilities](../capabilities.md) and [compatibility](../compatibility.md). The reading guide supports a human review; it does not claim that an independent reviewer has approved the code. Production deployment, arbitrary integration, publication, and external adoption remain outside this verified delivery.
