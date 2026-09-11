# Fifth-draft acceptance

The fifth draft is complete on `fifth-draft`. The [principles](../../FIFTH-DRAFT.md), [file-level plan](implementation-plan.md), [reading guide](reading-guide.md), and [measurements](measurements.md) describe the implementation and its review path. Earlier draft branches remain preserved.

## Verified delivery

| Item | Evidence |
| --- | --- |
| Verified software | `75ff1c0a524ae69b5df29eb93c4a59c0b70a0f74` |
| Third actual branch commit | `2524ced` — package acquisition and inspection proved by tests and real ledgers before committing |
| Archive | `.artifacts/release-6910669127351732605/harmonia-75ff1c0a524a.tar.gz` |
| Archive bytes | 63,296,542 |
| SHA-256 | `7ef992fa1f5f7d5a07a2183ce58cee9ac01d1934e4cdbe871c6f15c7e9deac98` |
| Relocated bundle | `.artifacts/fifth release relocation/harmonia-75ff1c0a524a` |
| Payload | 1,360 verified hashes, 32 fresh matching recordings, nine chapters |
| Current local book | `http://127.0.0.1:56007/` |

Final acceptance documentation follows the verified software commit. The immutable archive records the exact clean source that produced its evidence; its source copy therefore describes final acceptance as pending. This handoff records the subsequently completed checks without relabeling their source revision.

## Clean checkout gates

All eleven gates passed in a fresh checkout of the verified software. Installed SDK and dependency caches were reused. No expectations were regenerated.

| Gate | Seconds | Result |
| --- | ---: | --- |
| `build` | 100 | Pass |
| `daml-test` | 6 | Pass |
| `scala-test` | 6 | Pass |
| `packages-check` | 51 | Pass |
| `bindings-check` | 88 | Pass |
| `check` | 318 | Pass |
| `live-check` | 27 | Pass |
| `composer-check` | 47 | Pass |
| `builder-check` | 37 | Pass |
| `boundaries-check` | 67 | Pass |
| `portable-check` | 30 | Pass |

Stage durations total **777 seconds**; the resource sampler observed **786.9 seconds** including coordination and packaging. Scala tests total **57**: 11 product, 41 harness, and five book tests. All four Daml scripts passed.

The 27 regular stories and five evaluation recordings match their committed expectations. Both generated adapters retain their source DAR identities. Repeated generation produces identical sources and DARs; the deliberately faulty compiled adapter is caught by its golden. Portable rebuilding starts with empty outputs, reproduces the DARs, and passes its real ledger expectation.

All **67 original golden files** match the first-draft bytes and committed digests. All **47 Daml source/specimen files** match draft four byte for byte. The legacy source DAR remains `b77e417dedc16733ef751b79b82df99b063819d514622b8b4e56d16f3ad8cf27`; primitive approval remains `20be5fd4ec0de6fac4ae260c0e89a536c5f15dcc0e350165e6693c93b5132b2f`.

## Relocated product and book

- Extracted the archive into a path containing spaces. `run-verify` accepted the intact payload, rejected an altered `book.css`, and accepted its exact restoration. It passed again after the live exercise.
- `run-live` provisioned one disposable network. A separate `run-product serve` connected using its supplied configuration. Its 44-JAR service classpath contains the product entry point and no book, network harness, verification, or tools classes.
- In the real browser, Bank approved and Buyer continued the handoff. All three participant views reached completion; unauthenticated state returned HTTP 401, and private financing details remained hidden from Buyer and Reviewer.
- The editor retained focus and text selection through polling. It rejected duplicate step names. Bank proposed a generated-approval/direct-review plan, Buyer accepted, and the assigned parties completed both actions. Independent API reads confirmed the resulting process.
- Browser package retrieval, compilation, and download succeeded. The independently fetched ZIP is 2,199,619 bytes with 12 valid entries; SHA-256 `d0ced8c00dc54082f12dc10a0ff897449a61087baface85fa93ef9d7a2ea0265`.
- On the relocated book, all 32 stories and nine chapters passed at widths 1,440 and 390 pixels. Selection, comparison, provenance, source loading, and the second execution-boundary phase were checked. The source inspector opened and closed; the new shared request source and fifth-draft design were available. There was no document-level horizontal overflow.
- The book was verified on a temporary port before replacing the old preview. The final address opens Chapter 1 at desktop width and serves the verified fifth-draft recordings.

## Memory and retained evidence

The same heap limits remain: sbt 1 GiB, application tools 512 MiB, Canton 2 GiB, Daml Script 512 MiB, and book/release coordinator 128 MiB. One ledger environment ran at a time.

The sequential release peaked at **3,167,296 KiB (3.02 GiB) aggregate Java RSS**, with at most four owned JVMs: coordinator, runner, Canton, and Script. Sampling occurred approximately every 0.75 seconds and excludes the prior book, browser, and native tools. This is an observed run, not a claim that the refactor reduced memory by a fixed amount. The fourth-draft observed peak was 3.43 GiB under the same stated sampling scope.

The release left no owned JVMs running. The later browser exercise stopped both service owners and their ledger. At handoff, only the fifth-draft book remains: PID `24443`, heap cap **128 MiB**, sampled RSS **95,488 KiB (93.25 MiB)**. Process evidence is a dated local snapshot.

Evidence under `.artifacts/`:

- `release-6910669127351732605/checks/` contains every clean stage log; its `recordings/` links to the fresh raw observations in the retained checkout.
- `fifth-release-resources.json` and `.jsonl` contain ownership and RSS samples.
- `fifth-contract-preservation.json`, `fifth-pinned-dars.json`, and `fifth-runtime-isolation.json` record the independent preservation and runtime checks.
- `fifth-relocation.json` and `fifth-relocated-*.log` record archive, tamper, restoration, and post-live payload verification.
- `fifth-browser-live.json`, `fifth-browser-book.json`, and `fifth-browser-final-preview.json` retain browser assertions. `fifth-relocated-live-verification.json`, `fifth-relocated-composition.json`, and `fifth-relocated-download.json` retain independent HTTP results.
- `fifth-product.png`, `fifth-package.png`, `fifth-book-desktop.png`, `fifth-book-mobile.png`, and `fifth-book-chapter.png` retain the inspected screenshots.
- `fifth-final-processes.json` records the final book process. Cache-reclamation reports document removal of reproducible intermediates and verified duplicate payloads. All source, DARs, raw observations, stage logs, and prior release archives remain. Older generated book previews are preserved in `prior-generated-book-previews.tar.gz`.

The redundant original extraction and fresh-checkout compiler caches were reclaimed after verification. Run the retained archive or relocated bundle; source build commands regenerate those caches. The running preview is separate from the Git worktree.

This remains the documented local evaluation edition. Existing [capability limits](../capabilities.md), [compatibility](../compatibility.md), and publication/licensing status are unchanged. Human readability scores in the measurements are judgments, not developer study results.
