# Clean source release and relocated runtime

Source revision: **`dd5cf02754241730dc6cfed71e0b92216d657da0`**. The complete release gate ran from a new local Git clone with empty project build directories. It reused Java 17.0.19, Daml SDK 3.4.11, installed sbt, and dependency caches on macOS arm64. This is clean-checkout reproduction, not a fresh operating-system installation.

## Required gate

| Stage | Result | Seconds |
| --- | --- | ---: |
| `build` | Passed | 100 |
| `daml-test` | Passed | 6 |
| `scala-test` | Passed | 5 |
| `packages-check` | Passed | 51 |
| `bindings-check` | Passed | 85 |
| `check` | Passed | 345 |
| `live-check` | Passed | 26 |
| `composer-check` | Passed | 55 |
| `builder-check` | Passed | 44 |
| `boundaries-check` | Passed | 68 |
| `portable-check` | Passed | 20 |

The Daml test command executed four scripts, including the bounds script (84 transactions). All 27 Scala tests passed. The regular suite produced 27 matching goldens and two direct/generated parity results. The five additional book recordings cover authenticated handoff, two composer stories, the package builder, and execution bounds: **32 recordings from one clean revision**, all matching. The binding gate also caught a deliberately compiled source-call omission through two business-result differences. That intentional failure remains identifiable in the retained evidence.

The package gate reproduced pinned imports on two fresh networks and identical DAR retrieval. The generation gate compared repeated source/configuration/DAR manifests. The portable gate rebuilt an exported project from empty build directories and executed its independent ledger golden. The boundary gate executed the declared maxima and observed one winner and one aborted competing advance.

## Bundle and provenance

- Build/evidence root: `.artifacts/release-13364464813494329764`.
- Bundle: `harmonia-dd5cf0275424/` under that root.
- Archive: `harmonia-dd5cf0275424.tar.gz`, 57,573,718 bytes.
- Archive SHA-256: `6a13bc5f2b033ef81afdff7a6350de666f0b8329b200311d8db6a402f21bc1a2`.
- Manifest: 1,171 declared payload hashes; nine chapters; 32 clean, passing recording revisions; all eleven completed gate stages.

The archive includes compiled runtime JARs, DARs, source history without a remote, pinned inputs, generated project manifests, selected raw evidence, stage logs, the exported book, launchers, compatibility/limit documentation, and changelog. A scan checked 45 actual runtime secret/capability values against 1,174 non-Git files and found no matches. Private runtime filenames were absent. This scan supplements the packager's explicit selection; it is not a general secret-detection claim.

## Relocated delivery

Extracted the actual archive into `.artifacts/release relocation/harmonia-dd5cf0275424`, including a space in its parent path. `run-verify` passed. Altering the book CSS caused the verifier to fail for that exact file; restoring the original bytes made it pass again. Evidence: `.artifacts/verification/19-relocation.json`.

`run-book` serves the packaged edition at `http://127.0.0.1:56007/`. Browser verification selected every recording and its final attempt at 390 pixels wide: all matched, all carried the same clean source revision, and none caused document overflow. Every chapter had matching demo buttons, correct focus transfer, and no document overflow. Evidence: `.artifacts/verification/19-book.json` and `19-release-mobile.png`.

`run-live` started the packaged three-participant network from the relocated path. Bank, Buyer, and Reviewer authenticated state requests returned HTTP 200; missing credentials returned 401. The process and its ledger were then stopped. This is a packaging/startup check; the full authenticated behavior is covered by the preceding clean-revision gate. Evidence: `.artifacts/verification/19-live-runtime.json`.

## Memory and lifecycle

During 678 one-second samples, peak Java count was 5: the retained book, small release coordinator, active check tool, one Canton JVM, and one Daml Script JVM. Peak aggregate Java resident memory was 3286.3 MiB (about 3.21 GiB). Canton peaked at 2,417.6 MiB RSS with a 2 GiB Java heap cap; native/metaspace memory is additional. System memory pressure remained low. After the release gate, only the original book JVM remained; it was replaced by the packaged book. The separate packaged-live smoke check also shut down cleanly.

Evidence: `.artifacts/verification/19-memory.json`. Sampling began during the build and covers the later checks; it does not measure non-Java compiler processes. No sbt or Canton process is intentionally retained. The one book JVM has a 128 MiB heap cap.

Later commits record acceptance and handoff documentation. They do not relabel or replace this verified software/book revision. Public licensing, publication, outside evaluation, and adoption remain pending.
